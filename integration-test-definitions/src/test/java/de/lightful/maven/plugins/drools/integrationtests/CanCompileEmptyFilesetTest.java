/*******************************************************************************
 * Copyright (c) 2009-2011 Ansgar Konermann
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
package de.lightful.maven.plugins.drools.integrationtests;

import de.lightful.maven.drools.plugin.naming.WellKnownNames;
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFile;
import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.MavenVerifierTest;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.fest.assertions.Assertions.assertThat;

@Test
@DefaultSettingsFile
@ExecuteGoals(WellKnownNames.GOAL_CLEAN)
@VerifyUsingProject("can_compile_empty_fileset")
public class CanCompileEmptyFilesetTest extends MavenVerifierTest {

  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;

  @Inject
  private Verifier verifier;

  @Test
  @ExecuteGoals("compile")
  public void testPackageFileContainsAllTheExpectedRules() throws Exception {
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);

    KnowledgePackageFile knowledgePackageFile = new KnowledgePackageFile(expectedOutputFile(verifier, EXPECTED_OUTPUT_FILE));
    assertThat(knowledgePackageFile).as("KnowledgePackageFile").isNotNull();
    assertThat(knowledgePackageFile.getKnowledgePackages()).as("Knowledge Packages").isEmpty();
  }
}
