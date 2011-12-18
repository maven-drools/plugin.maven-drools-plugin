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
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.drools.definition.KnowledgePackage;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

@Test
@DefaultSettingsFile
@ExecuteGoals("clean")
@VerifyUsingProject("does_respect_includes_and_excludes_test")
public class DoesRespectIncludesAndExcludesTest extends MavenDroolsPluginIntegrationTest {

  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;
  private static final List<String> EXPECTED_PACKAGE_NAMES = Arrays.asList("included_by_default", "included_higher_level", "included_lowest_level");

  @Inject
  private Verifier verifier;

  @Test
  @ExecuteGoals("compile")
  public void testPackageFileContainsAllTheExpectedRules() throws Exception {
    verifier.executeGoal(WellKnownNames.GOAL_COMPILE);
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);

    KnowledgeModuleReader knowledgeModuleReader = knowledgeIoFactory.createKnowledgeModuleReader(new FileInputStream(expectedOutputFile(verifier, EXPECTED_OUTPUT_FILE)), contextClassLoader());
    final Iterable<KnowledgePackage> knowledgePackages = knowledgeModuleReader.readKnowledgePackages();

    Map<String, KnowledgePackage> allKnowledgePackagesByName = new HashMap<String, KnowledgePackage>();
    for (KnowledgePackage knowledgePackage : knowledgePackages) {
      allKnowledgePackagesByName.put(knowledgePackage.getName(), knowledgePackage);
    }
    assertThat(allKnowledgePackagesByName.keySet()).containsOnly(EXPECTED_PACKAGE_NAMES.toArray());
  }
}
