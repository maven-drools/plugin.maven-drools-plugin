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
import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.testng.annotations.Test;

import javax.inject.Inject;

@Test
@DefaultSettingsFile
@ExecuteGoals(WellKnownNames.GOAL_CLEAN)
@VerifyUsingProject("can_resolve_transitive_knowledge_modules")
public class CanResolveTransitiveKnowledgeModulesTest extends MavenDroolsPluginIntegrationTest {

  private static final String EXPECTED_OUTPUT_FILE = "modules/user_of_a_and_b/target/plugintest.artifact.user_of_a_and_b-1.0.0" + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;

  @Inject
  private Verifier verifier;

  @Test
  public void testOutputFileContainsExpectedDroolsKnowledgePackages() throws Exception {
    verifier.executeGoal(WellKnownNames.GOAL_COMPILE);
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);
  }
}
