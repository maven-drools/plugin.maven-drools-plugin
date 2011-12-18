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

import de.lightful.maven.plugins.drools.knowledgeio.KnowledgeModuleReader;
import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static de.lightful.maven.drools.plugin.naming.WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;
import static org.fest.assertions.Assertions.assertThat;

@Test
@DefaultSettingsFile
@VerifyUsingProject("can_compile_100_files_in_one_go")
@ExecuteGoals("clean")
public class CanCompile100FilesInOneGoTest extends MavenDroolsPluginIntegrationTest {

  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + "." + FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;
  private static final int EXPECTED_NUMBER_OF_PACKAGES = 100;

  @Inject
  private Verifier verifier;

  @Test
  @ExecuteGoals("compile")
  public void testPackageFileContainsAllTheExpectedRules() throws Exception {
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);

    KnowledgeModuleReader knowledgeModuleReader = knowledgeIoFactory.createKnowledgeModuleReader(new FileInputStream(expectedOutputFile(verifier, EXPECTED_OUTPUT_FILE)), contextClassLoader());
    final Iterable<KnowledgePackage> knowledgePackages = knowledgeModuleReader.readKnowledgePackages();

    assertThat(knowledgePackages).as("Knowledge packages").hasSize(EXPECTED_NUMBER_OF_PACKAGES);
    Map<String, KnowledgePackage> allKnowledgePackagesByName = new HashMap<String, KnowledgePackage>();
    for (KnowledgePackage knowledgePackage : knowledgePackages) {
      allKnowledgePackagesByName.put(knowledgePackage.getName(), knowledgePackage);
    }
    for (int packageNumber = 1; packageNumber <= EXPECTED_NUMBER_OF_PACKAGES; packageNumber++) {
      final String expectedPackageName = expectedName(packageNumber);
      assertThat(allKnowledgePackagesByName.containsKey(expectedPackageName)).as("All knowledge packages contain one with name "
                                                                                 + expectedPackageName + ".").isTrue();
      KnowledgePackage currentPackage = allKnowledgePackagesByName.get(expectedPackageName);
      final Collection<Rule> rules = currentPackage.getRules();
      assertThat(rules).as("Number of rules in package #" + packageNumber).hasSize(1);
      final Rule rule = rules.iterator().next();
      assertThat(rule.getName()).as("Name of rule in package #" + packageNumber).isEqualTo(expectedRuleName(packageNumber));
    }
  }

  private String expectedRuleName(int packageNumber) {
    return "test rule " + packageNumber;
  }

  private String expectedName(int packageNumber) {
    return "rules.pkg" + packageNumber + ".test";
  }
}
