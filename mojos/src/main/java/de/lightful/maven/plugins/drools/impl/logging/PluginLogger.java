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
package de.lightful.maven.plugins.drools.impl.logging;

import de.lightful.maven.plugins.drools.impl.config.Pass;
import de.lightful.maven.plugins.drools.knowledgeio.LogStream;
import org.apache.maven.plugin.MojoFailureException;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.fest.util.Arrays;

import java.io.File;

public class PluginLogger {

  private LogStream<?> debug;

  private LogStream<?> info;

  private LogStream<?> warn;

  private LogStream<?> error;

  public LogStream<?> error() {
    return error;
  }

  public LogStream<?> warn() {
    return warn;
  }

  public LogStream<?> info() {
    return info;
  }

  public LogStream<?> debug() {
    return debug;
  }

  public void setInfoStream(MavenLogStream<?> infoStream) {
    this.info = infoStream;
  }

  public void setDebugStream(MavenLogStream<?> debugStream) {
    this.debug = debugStream;
  }

  public void setErrorStream(MavenLogStream<?> errorStream) {
    this.error = errorStream;
  }

  public void setWarnStream(MavenLogStream<?> warnStream) {
    this.warn = warnStream;
  }

  public void dumpPassesConfiguration(Pass[] passes) {
    for (Pass pass : passes) {
      info.write("Pass #" + pass.getSequenceNumber() + ":").nl();
      info.write("    Name:             '" + pass.getName() + "'").nl();
      info.write("    Rule Source Root: " + pass.getRuleSourceRoot()).nl();
      info.write("    Includes:         " + Arrays.format(pass.getIncludes())).nl();
      info.write("    Excludes:         " + Arrays.format(pass.getExcludes())).nl();
    }
  }

  public void dumpOverallPluginConfiguration(Pass[] passes, String name) {
    info.write("This is the compiler plugin").nl();
    info.write("Passes: " + Arrays.format(passes)).nl();
    info.write("Project: " + name).nl();
  }

  public void reportCompilationErrors(KnowledgeBuilderErrors errors, File fileToCompile) throws MojoFailureException {
    if (errors.isEmpty()) {
      debug.write("Compilation of " + fileToCompile.getAbsolutePath() + " completed successfully.").nl();
      return;
    }
    error.write("Error(s) occurred while compiling " + fileToCompile + ":");
    formatCompilerErrors(errors);
    throw new MojoFailureException("Compilation errors occurred.");
  }

  private void formatCompilerErrors(KnowledgeBuilderErrors errors) {
    int i = 0;
    for (KnowledgeBuilderError builderError : errors) {
      i++;
      error.write("Error #" + i);
      final int[] errorLines = builderError.getErrorLines();
      if (errorLines.length > 0) {
        error.write(" [occurred in line(s) ");
        for (int errorLineIndex = 0; errorLineIndex < errorLines.length; errorLineIndex++) {
          error.write("" + errorLines[errorLineIndex]);
          if (errorLineIndex + 1 < errorLines.length) {
            error.write(", ");
          }
        }
        error().write("]");
      }
      error.write(": ");
      error.write(builderError.getMessage());
      error.nl();
    }
  }
}
