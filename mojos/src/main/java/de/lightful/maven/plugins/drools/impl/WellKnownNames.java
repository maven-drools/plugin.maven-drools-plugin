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

/** Various constant values used both in production and test code of the plugin. */
public interface WellKnownNames {

  /**
   * Maven packaging identifier to be used in pom.xml if drools packaging is desired. <p/> Example:<br/>
   * <pre>
   * &lt;project ...&gt;
   *   ...
   *   &lt;packaging&gt;knowledge-module&lt;/packaging&gt;
   *   ...
   * &lt;/project&gt;
   * </pre>
   */
  String DROOLS_KNOWLEDGE_MODULE_PACKAGING_IDENTIFIER = "knowledge-module";

  /** File extension used by plugin to create final target file name. */
  String FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE = "dkm";

  String ARTIFACT_TYPE_DROOLS_KNOWLEDGE_MODULE = "knowledge-module";
  String ARTIFACT_TYPE_JAR = "jar";

  String GOAL_CLEAN = "clean";
  String GOAL_COMPILE = "compile";
  String GOAL_TEST_COMPILE = "test-compile";
  String GOAL_DEPLOY = "deploy";

  String SCOPE_COMPILE = GOAL_COMPILE;
}
