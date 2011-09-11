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

package de.lightful.maven.plugins.drools.impl.dependencies;

import de.lightful.maven.plugins.drools.impl.WellKnownNames;
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFile;
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFormatter;
import de.lightful.maven.plugins.drools.knowledgeio.LogStream;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.definition.KnowledgePackage;
import org.fest.util.Arrays;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.filter.AndDependencyFilter;
import org.sonatype.aether.util.graph.FilteringDependencyVisitor;
import org.sonatype.aether.util.graph.PostorderNodeListGenerator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class DependencyLoader {

  public static final String EMPTY_CLASSIFIER = "";
  private LogStream<?> debug;
  private LogStream<?> info;
  private LogStream<?> warn;
  private LogStream<?> error;

  private ArtifactHandlerManager artifactHandlerManager;
  private RepositorySystem repositorySystem;

  public KnowledgeBuilder createKnowledgeBuilderForRuleCompilation(MavenProject project, RepositorySystemSession session, List<RemoteRepository> repositories) throws MojoFailureException {
    try {
      final CollectResult collectResult = collectAllDependencies(project, session, repositories);
      final List<Artifact> knowledgeModulesInLoadOrder = resolveKnowledgeModuleArtifacts(collectResult, session, repositories);
      final List<Artifact> jarArtifactsInLoadOrder = resolveJarArtifacts(collectResult, session, repositories);

      URLClassLoader classLoader = createCompileClassLoader(jarArtifactsInLoadOrder);
      info.write("\n\nUsing class loader with these URLs:").nl();
      int i = 1;
      for (URL url : classLoader.getURLs()) {
        info.write("URL in use (#" + i + "): ").write(url.toString()).nl();
      }
      KnowledgeBuilderConfiguration configuration = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(new Properties(), classLoader);
      KnowledgeBase existingKnowledge = createKnowledgeBaseFromDependencies(classLoader, knowledgeModulesInLoadOrder);
      return KnowledgeBuilderFactory.newKnowledgeBuilder(existingKnowledge, configuration);
    }
    catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException("Internal error: declared resolution of compile-scoped dependencies, but got exception!", e);
    }
    catch (MalformedURLException e) {
      throw new MojoFailureException("Got malformed URL for compile classpath element.", e);
    }
    catch (IOException e) {
      throw new MojoFailureException("Error while creating drools KnowledgeBuilder.", e);
    }
    catch (RepositoryException e) {
      throw new MojoFailureException("Repository backend failed.", e);
    }
  }

  private List<Artifact> resolveJarArtifacts(CollectResult collectResult, RepositorySystemSession session, List<RemoteRepository> repositories) throws RepositoryException {
    final List<DependencyNode> droolsDependencies = getJavaDependencies(collectResult.getRequest().getRoot(), collectResult);
    return resolveArtifacts(droolsDependencies, session, repositories);
  }

  private List<Artifact> resolveKnowledgeModuleArtifacts(CollectResult collectResult, RepositorySystemSession session, List<RemoteRepository> repositories) throws RepositoryException {
    final List<DependencyNode> droolsDependencies = getDroolsDependencies(collectResult.getRequest().getRoot(), collectResult);
    return resolveArtifacts(droolsDependencies, session, repositories);
  }

  private List<Artifact> resolveArtifacts(List<DependencyNode> dependencyNodes, RepositorySystemSession repositorySession, List<RemoteRepository> remoteProjectRepositories) throws RepositoryException {
    List<Artifact> artifacts = new ArrayList<Artifact>(dependencyNodes.size());
    for (DependencyNode dependencyNode : dependencyNodes) {
      ArtifactRequest request = new ArtifactRequest();
      request.setDependencyNode(dependencyNode);
      request.setRepositories(remoteProjectRepositories);
      final ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySession, request);
      artifacts.add(artifactResult.getArtifact());
    }
    return artifacts;
  }

  private CollectResult collectAllDependencies(MavenProject mavenProject, RepositorySystemSession repositorySession, List<RemoteRepository> remoteProjectRepositories) throws MojoFailureException {
    CollectRequest collectRequest = new CollectRequest();
    collectRequest.setRoot(createDependencyFrom(mavenProject));
    collectRequest.setRepositories(remoteProjectRepositories);

    CollectResult collectResult;
    try {
      collectResult = repositorySystem.collectDependencies(repositorySession, collectRequest);
      dumpDependencyTree(collectResult);
    }
    catch (DependencyCollectionException e) {
      throw new MojoFailureException("Unable to collect dependencies", e);
    }
    return collectResult;
  }

  private List<DependencyNode> getJavaDependencies(Dependency rootDependency, CollectResult collectResult) {
    return getDependenciesInPostorder(collectResult, createFilterForJarDependencies(rootDependency));
  }

  private DependencyFilter createFilterForJarDependencies(Dependency rootDependency) {
    DependencyFilter excludeRootNode = new FilterRootNode(rootDependency);
    DependencyFilter acceptOnlyDroolsModules = new AcceptOnlyJars(artifactHandlerManager);
    return new AndDependencyFilter(excludeRootNode, acceptOnlyDroolsModules);
  }

  private List<DependencyNode> getDroolsDependencies(Dependency rootDependency, CollectResult collectResult) {
    return getDependenciesInPostorder(collectResult, createFilterForDroolsDependencies(rootDependency));
  }

  private List<DependencyNode> getDependenciesInPostorder(CollectResult collectResult, DependencyFilter filterForDroolsDependencies) {
    PostorderNodeListGenerator nodeListGenerator = new PostorderNodeListGenerator();
    FilteringDependencyVisitor filterForDroolsNodes = new FilteringDependencyVisitor(nodeListGenerator, filterForDroolsDependencies);
    DependencyNode rootNode = collectResult.getRoot();
    rootNode.accept(filterForDroolsNodes);
    return nodeListGenerator.getNodes();
  }

  private AndDependencyFilter createFilterForDroolsDependencies(Dependency rootDependency) {
    DependencyFilter excludeRootNode = new FilterRootNode(rootDependency);
    DependencyFilter acceptOnlyDroolsModules = new AcceptOnlyDroolsModules(artifactHandlerManager);
    return new AndDependencyFilter(excludeRootNode, acceptOnlyDroolsModules);
  }

  private KnowledgeBase createKnowledgeBaseFromDependencies(URLClassLoader classLoader, List<Artifact> knowledgeModules) throws MojoFailureException {
    final KnowledgeBaseConfiguration configuration = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, classLoader);
    final KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(configuration);

    for (Artifact knowledgeModule : knowledgeModules) {
      addKnowledgeModule(knowledgeBase, knowledgeModule);
    }

    return knowledgeBase;
  }

  private Dependency createDependencyFrom(MavenProject project) {
    final String artifactType = project.getPackaging();
    org.sonatype.aether.artifact.Artifact artifact = new DefaultArtifact(
        project.getGroupId(), project.getArtifactId(), EMPTY_CLASSIFIER, fileExtensionOf(artifactType), project.getVersion()
    );
    return new Dependency(artifact, WellKnownNames.SCOPE_COMPILE);
  }

  private String fileExtensionOf(String artifactType) {
    return artifactHandlerManager.getArtifactHandler(artifactType).getExtension();
  }

  private void dumpDependencyTree(CollectResult collectResult) {
    info.write("Resolved dependencies of " + collectResult.getRoot().toString() + ".").nl();
  }

  private void addKnowledgeModule(KnowledgeBase knowledgeBase, Artifact compileArtifact) throws MojoFailureException {
    final Collection<KnowledgePackage> knowledgePackages = loadKnowledgePackages(compileArtifact);
    knowledgeBase.addKnowledgePackages(knowledgePackages);
    info.write("Loaded drools dependency " + coordinatesOf(compileArtifact)).nl();
    info.write("  Contains packages:").nl();
    KnowledgePackageFormatter.dumpKnowledgePackages(info, knowledgePackages);
  }

  private Collection<KnowledgePackage> loadKnowledgePackages(Artifact compileArtifact) throws MojoFailureException {
    KnowledgePackageFile knowledgePackageFile = new KnowledgePackageFile(compileArtifact.getFile());
    final Collection<KnowledgePackage> knowledgePackages;
    try {
      knowledgePackages = knowledgePackageFile.getKnowledgePackages();
    }
    catch (IOException e) {
      throw new MojoFailureException("Unable to load compile-scoped dependency " + coordinatesOf(compileArtifact), e);
    }
    catch (ClassNotFoundException e) {
      throw new MojoFailureException("Unable to load compile-scoped dependency " + coordinatesOf(compileArtifact), e);
    }
    return knowledgePackages;
  }

  private String coordinatesOf(Artifact artifact) {
    return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getExtension() + ":" + artifact.getVersion();
  }

  private URLClassLoader createCompileClassLoader(List<Artifact> artifacts) throws DependencyResolutionRequiredException, IOException {

    ArrayList<URL> classpathUrls = new ArrayList<URL>();
    for (Artifact artifact : artifacts) {
      URL classpathElementUrl = artifact.getFile().toURI().toURL();
      classpathUrls.add(classpathElementUrl);
    }
    final URL[] urls = classpathUrls.toArray(new URL[classpathUrls.size()]);
    debug.write("Passing URLs to new URLClassLoader instance: ").write(Arrays.format(urls)).nl();
    URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader()) {
      @Override
      public Class<?> loadClass(String name) throws ClassNotFoundException {
        debug.write("Loading class '" + name + "'").nl();
        final Class<?> loadedClass = super.loadClass(name);
        debug.write("Loading class '" + name + "': loaded class [" + loadedClass + "]").nl();
        return loadedClass;
      }

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        debug.write("Finding class '" + name + "'").nl();
        final Class<?> foundClass = super.findClass(name);
        debug.write("Finding class '" + name + "': found class [" + foundClass + "]").nl();
        return foundClass;
      }
    };

    debug.write("Adding classpath URLs to classloader:").nl();
    for (URL classpathUrl : classpathUrls) {
      debug.write("   | " + classpathUrl).nl();
    }
    debug.write("   #").nl();
    return classLoader;
  }

  private void dumpArtifactInfo(int i, Artifact artifact) {
    debug.write("Dependency Artifact #" + i + ": GroupId=" + artifact.getGroupId()).nl();
    debug.write("Dependency Artifact #" + i + ": ArtifactId=" + artifact.getArtifactId()).nl();
    debug.write("Dependency Artifact #" + i + ": Type=" + artifact.getExtension()).nl();
    debug.write("Dependency Artifact #" + i + ": Classifier=" + artifact.getClassifier()).nl();
    debug.write("Dependency Artifact #" + i + ": BaseVersion=" + artifact.getBaseVersion()).nl();
    debug.write("Dependency Artifact #" + i + ": Version=" + artifact.getVersion()).nl();
    final File artifactFile = artifact.getFile();
    if (artifactFile != null) {
      debug.write("Dependency Artifact #" + i + ": File=" + artifactFile.getAbsolutePath()).nl();
    }
    else {
      debug.write("Dependency Artifact #" + i + ": File={null}").nl();
    }
  }

  private static class FilterRootNode implements DependencyFilter {

    private final Dependency rootDependency;

    public FilterRootNode(Dependency rootDependency) {
      this.rootDependency = rootDependency;
    }

    public boolean accept(DependencyNode node, List<DependencyNode> parents) {
      return node.getDependency() != rootDependency;
    }
  }

  private static class AcceptOnlyDroolsModules implements DependencyFilter {

    private ArtifactHandlerManager artifactHandlerManager;

    public AcceptOnlyDroolsModules(ArtifactHandlerManager artifactHandlerManager) {
      this.artifactHandlerManager = artifactHandlerManager;
    }

    public boolean accept(DependencyNode node, List<DependencyNode> parents) {
      final ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler(WellKnownNames.ARTIFACT_TYPE_DROOLS_KNOWLEDGE_MODULE);
      final String knowledgeModuleExtension = artifactHandler.getExtension();
      return node.getDependency().getArtifact().getExtension().equals(knowledgeModuleExtension);
    }
  }

  private class AcceptOnlyJars implements DependencyFilter {

    private ArtifactHandlerManager artifactHandlerManager;

    public AcceptOnlyJars(ArtifactHandlerManager artifactHandlerManager) {
      this.artifactHandlerManager = artifactHandlerManager;
    }

    public boolean accept(DependencyNode node, List<DependencyNode> parents) {
      final ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler(WellKnownNames.ARTIFACT_TYPE_JAR);
      final String jarFileExtension = artifactHandler.getExtension();
      return node.getDependency().getArtifact().getExtension().equals(jarFileExtension);
    }
  }
}