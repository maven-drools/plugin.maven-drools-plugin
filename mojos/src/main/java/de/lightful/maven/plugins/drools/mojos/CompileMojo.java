/*******************************************************************************
 * Copyright (c) 2009-2011 Ansgar Konermann
 *
 * This file is part of the Maven 3 Drools Plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.lightful.maven.plugins.drools.mojos;

import de.lightful.maven.plugins.drools.impl.OutputFileWriter;
import de.lightful.maven.plugins.drools.impl.ResourceTypeDetector;
import de.lightful.maven.plugins.drools.impl.WellKnownNames;
import de.lightful.maven.plugins.drools.impl.config.ConfigurationValidator;
import de.lightful.maven.plugins.drools.impl.config.Pass;
import de.lightful.maven.plugins.drools.impl.dependencies.DependencyLoader;
import de.lightful.maven.plugins.drools.impl.logging.*;
import de.lightful.maven.plugins.drools.knowledgeio.LogStream;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.DirectoryScanner;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.*;
import java.util.List;

@MojoGoal(WellKnownNames.GOAL_COMPILE)
@MojoRequiresDependencyResolution("runtime")
public class CompileMojo extends AbstractMojo {

  @MojoParameter(
      description = "Define what the compiler should compile in each of its passes. " +
                    "You need at least one pass to create useful output.",
      readonly = false,
      required = true
  )
  private Pass[] passes;

  @MojoParameter(
      description = "The Maven classifier which should be used to define the Maven coordinates of " +
                    "the created artifact.",
      readonly = false,
      required = false,
      defaultValue = ""
  )
  private String classifier;

  @MojoParameter(defaultValue = "${project}")
  private MavenProject project;

  @MojoComponent
  private RepositorySystem repositorySystem;

  @MojoComponent
  private ResourceTypeDetector resourceTypeDetector;

  @MojoComponent
  private ConfigurationValidator configurationValidator;

  @MojoComponent
  private OutputFileWriter outputFileWriter;

  @MojoComponent
  private ArtifactHandlerManager artifactHandlerManager;

  @MojoParameter(defaultValue = "${repositorySystemSession}", readonly = true)
  private RepositorySystemSession repositorySession;

  @MojoParameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
  private List<RemoteRepository> remoteProjectRepositories;

  private KnowledgeBuilder knowledgeBuilder;

  @MojoComponent
  private PluginLogger logger;

  @MojoComponent
  private MavenProjectHelper projectHelper;

  @MojoComponent(role = LogStream.ROLE, roleHint = "debug")
  private MavenLogStream<MavenDebugLogStream> debug;

  @MojoComponent(role = LogStream.ROLE, roleHint = "info")
  private MavenLogStream<MavenInfoLogStream> info;

  @MojoComponent(role = LogStream.ROLE, roleHint = "warn")
  private MavenLogStream<MavenWarnLogStream> warn;

  @MojoComponent(role = LogStream.ROLE, roleHint = "error")
  private MavenLogStream<MavenErrorLogStream> error;

  @MojoComponent
  private DependencyLoader dependencyLoader;

  private String sourceEncoding;

  public void execute() throws MojoFailureException {
    initializeLogging();
    sourceEncoding = defineSourceEncoding();

    logger.dumpOverallPluginConfiguration(passes, project.getName());

    configurationValidator.validateConfiguration(passes);
    logger.dumpPassesConfiguration(passes);

    logger.dumpDroolsRuntimeInfo(KnowledgeBuilder.class);
    runAllPasses();
    outputFileWriter.writeOutputFile(knowledgeBuilder.getKnowledgePackages(), logger, project, projectHelper, classifier);
  }

  private String defineSourceEncoding() {
    String sourceEncodingFromProperty = project.getProperties().getProperty("project.build.sourceEncoding");
    if ((sourceEncodingFromProperty == null) || (sourceEncodingFromProperty.length() == 0)) {
      warn.write("No property with name 'project.build.sourceEncoding' found, assuming default source encoding 'UTF-8'.").nl();
      return "UTF-8";
    }
    else {
      return sourceEncodingFromProperty;
    }
  }

  private void initializeLogging() {
    debug.setMavenLog(getLog());
    info.setMavenLog(getLog());
    warn.setMavenLog(getLog());
    error.setMavenLog(getLog());
  }

  private void runAllPasses() throws MojoFailureException {
    knowledgeBuilder = dependencyLoader.createKnowledgeBuilderForRuleCompilation(project, repositorySession, remoteProjectRepositories);
    info.write("Using source encoding '" + sourceEncoding + "'.").nl();
    for (Pass pass : passes) {
      executePass(pass);
    }
  }

  private void executePass(Pass pass) throws MojoFailureException {
    info.write("Executing compiler pass '" + pass.getName() + "' in directory " + relativeToBasedir(pass.getRuleSourceRoot()) + "...").nl();
    final String[] filesToCompile = determineFilesToCompile(pass);
    for (String currentFile : filesToCompile) {
      compileRuleFile(pass.getRuleSourceRoot(), currentFile);
    }
    info.write("Done with compiler pass '" + pass.getName() + "'.").nl();
  }

  private String[] determineFilesToCompile(Pass pass) {
    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(pass.getRuleSourceRoot());
    scanner.setIncludes(pass.getIncludes());
    scanner.setExcludes(pass.getExcludes());
    scanner.setCaseSensitive(true);
    scanner.scan();

    return scanner.getIncludedFiles();
  }

  private void compileRuleFile(File ruleSourceRoot, String nameOfFileToCompile) throws MojoFailureException {
    File fileToCompile = new File(ruleSourceRoot, nameOfFileToCompile);
    info.write("  Compiling rule file '" + relativeToBasedir(fileToCompile) + "' ...").nl();
    try {
      Resource resource = ResourceFactory.newReaderResource(
          new InputStreamReader(new FileInputStream(fileToCompile), sourceEncoding), sourceEncoding
      );
      ResourceType type = resourceTypeDetector.detectTypeOf(fileToCompile);
      knowledgeBuilder.add(resource, type);
    }
    catch (FileNotFoundException e) {
      throw new MojoFailureException("Cannot open file " + fileToCompile.getAbsolutePath() + " for compilation", e);
    }
    catch (UnsupportedEncodingException e) {
      throw new MojoFailureException("Cannot perform character conversion from source encoding '" + sourceEncoding + "' for file " + fileToCompile.getAbsolutePath(), e);
    }
    logger.reportCompilationErrors(knowledgeBuilder.getErrors(), fileToCompile);
  }

  private String relativeToBasedir(File fileToCompile) {
    final String basedir = project.getBasedir().getAbsolutePath();
    final String filePath = fileToCompile.getAbsolutePath();
    if (filePath.startsWith(basedir + "/")) {
      return filePath.substring(basedir.length() + "/".length());
    }
    else {
      return filePath;
    }
  }
}
