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
import org.fest.assertions.Assertions;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;

@Test
@VerifyUsingProject("can_deploy_artifact_with_classifier")
@DefaultSettingsFile
public class CanDeployArtifactWithClassifierTest extends MavenDroolsPluginIntegrationTest {

  public static final String EXPECTED_ARTIFACT_NAME = "de/lightful/maven/plugins/plugintest/drools/plugintest.artifact.can_deploy_artifact_with_classifier/1.0.0/plugintest.artifact.can_deploy_artifact_with_classifier-1.0.0" + "-MyTestClassifierABC" + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;

  @Inject
  private Verifier verifier;

  @Test
  @Parameters("repository.url.deploymenttest")
  @ExecuteGoals(WellKnownNames.GOAL_CLEAN)
  public void testFileGetsDeployedWithClassifier(String deploymentRepositoryUrl) throws Exception {
    verifier.executeGoal(WellKnownNames.GOAL_DEPLOY);
    verifier.verifyErrorFreeLog();

    URL repositoryUrl = new URL(deploymentRepositoryUrl);
    File repositoryDirectory = new File(repositoryUrl.getFile());

    Assertions.assertThat(repositoryDirectory).exists().isDirectory();
    File expectedDeployedArtifact = new File(repositoryDirectory, EXPECTED_ARTIFACT_NAME);
    Assertions.assertThat(expectedDeployedArtifact).exists().isFile();
  }
}