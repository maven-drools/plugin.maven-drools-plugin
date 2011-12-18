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
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgeModuleReader;
import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.SettingsFile;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

@Test
@VerifyUsingProject("can_use_existing_java_dependency")
@ExecuteGoals("clean")
public class CanUseExistingJavaDependencyTest extends MavenDroolsPluginIntegrationTest {

  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;
  private static final String EXPECTED_PACKAGE_NAME = "rules.test";

  @Inject
  private Verifier verifier;

  @Test
  @SettingsFile("/de/lightful/maven/plugins/drools/integrationtests/integration-settings.xml")
  public void testDoesCreateOutputFile() throws Exception {
    verifier.executeGoal(WellKnownNames.GOAL_COMPILE);
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);
  }

  @Test
  @DefaultSettingsFile
  public void testPackageFileContainsPackagedRule() throws Exception {
    verifier.executeGoal(WellKnownNames.GOAL_COMPILE);
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);

    KnowledgeModuleReader knowledgeModuleReader = knowledgeIoFactory.createKnowledgeModuleReader(new FileInputStream(expectedOutputFile(verifier, EXPECTED_OUTPUT_FILE)), contextClassLoader());
    final Iterable<KnowledgePackage> knowledgePackages = knowledgeModuleReader.readKnowledgePackages();

    assertThat(knowledgePackages).as("Knowledge packages").hasSize(1);
    final KnowledgePackage knowledgePackage = knowledgePackages.iterator().next();
    assertThat(knowledgePackage.getName()).as("Knowledge package name").isEqualTo(EXPECTED_PACKAGE_NAME);
    final Collection<Rule> rules = knowledgePackage.getRules();
    assertThat(rules).as("Rules in loaded package").hasSize(2);
    Collection<String> ruleNames = new ArrayList<String>();
    for (Rule rule : rules) {
      ruleNames.add(rule.getName());
    }
    assertThat(ruleNames).containsOnly("Check if Peter is at least 18 years old",
                                       "Cities on different continents have huge distance");
  }
}
