/*
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
 */
package de.lightful.maven.plugins.drools.integrationtests;

import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFile;
import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.MavenVerifierTest;
import de.lightful.maven.plugins.testing.SettingsFile;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.drools.definition.KnowledgePackage;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static de.lightful.maven.drools.plugin.naming.WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;
import static org.fest.assertions.Assertions.assertThat;

@Test
@SettingsFile("/de/lightful/maven/plugins/drools/integrationtests/integration-settings.xml")
@VerifyUsingProject("can_execute_multiple_passes")
@ExecuteGoals("clean")
public class CanPerformMultiplePassesTest extends MavenVerifierTest {

  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + "." + FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;

  @Inject
  private Verifier verifier;

  @Test
  @ExecuteGoals("compile")
  public void testOutputFileContainsExpectedDroolsKnowledgePackages() throws Exception {
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);

    KnowledgePackageFile knowledgePackageFile = new KnowledgePackageFile(expectedOutputFile(verifier, EXPECTED_OUTPUT_FILE));
    assertThat(knowledgePackageFile.getFile()).exists();
    final Iterable<KnowledgePackage> knowledgePackages = knowledgePackageFile.getKnowledgePackages();
    assertThat(knowledgePackages).as("collection of knowledge packages").hasSize(2);

    Map<String, KnowledgePackage> knowledgePackageMap = new HashMap<String, KnowledgePackage>();

    for (KnowledgePackage knowledgePackage : knowledgePackages) {
      knowledgePackageMap.put(knowledgePackage.getName(), knowledgePackage);
    }

    assertThat(knowledgePackageMap.containsKey("model")).overridingErrorMessage("Resulting target artifact must contain package named 'model'.");
    assertThat(knowledgePackageMap.containsKey("rules")).overridingErrorMessage("Resulting target artifact must contain package named 'rules'.");

    KnowledgePackage modelPackage = knowledgePackageMap.get("model");
    assertThat(modelPackage.getRules()).as("Collection of rules in package 'model'").hasSize(0);
    KnowledgePackage rulesPackage = knowledgePackageMap.get("rules");
    assertThat(rulesPackage.getRules()).as("Collection of rules in package 'rules'").hasSize(1);
  }
}
