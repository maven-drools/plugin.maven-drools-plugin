/*******************************************************************************
 * Copyright (c) 2009-2012 Ansgar Konermann
 *
 * This file is part of the "Maven 3 Drools Support" Package.
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

package de.lightful.maven.plugins.drools.impl;

import org.apache.maven.project.MavenProject;

import java.io.File;

public class MavenProjectDecorator {

  private MavenProject project;

  public MavenProjectDecorator(MavenProject project) {
    this.project = project;
  }

  public MavenProject getProject() {
    return project;
  }

  public String relativeToBasedir(File fileToCompile) {
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
