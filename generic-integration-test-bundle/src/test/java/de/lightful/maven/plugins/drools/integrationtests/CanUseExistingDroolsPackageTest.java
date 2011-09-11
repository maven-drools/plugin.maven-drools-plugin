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

import de.lightful.maven.drools.plugin.naming.WellKnownNames;
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFile;
import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.MavenVerifierTest;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.definition.type.FactType;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

@Test
@VerifyUsingProject("can_use_existing_drools_binary")
@ExecuteGoals("clean")
public class CanUseExistingDroolsPackageTest extends MavenVerifierTest {

  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;
  private static final String EXPECTED_RULE_NAME = "Accept only heavy melons";
  private static final String EXPECTED_PACKAGE_NAME = "rules";

  @Inject
  private Verifier verifier;

  @Test
  @DefaultSettingsFile
  public void testPackageFileContainsPackagedRule() throws Exception {
    verifier.executeGoal("compile");
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);

    KnowledgePackageFile knowledgePackageFile = new KnowledgePackageFile(expectedOutputFile(verifier, EXPECTED_OUTPUT_FILE));
    final Iterable<KnowledgePackage> knowledgePackages = knowledgePackageFile.getKnowledgePackages();

    assertThat(knowledgePackages).as("Knowledge packages").hasSize(1);
    final KnowledgePackage knowledgePackage = knowledgePackages.iterator().next();
    assertThat(knowledgePackage.getName()).as("Knowledge package name").isEqualTo(EXPECTED_PACKAGE_NAME);
    final Collection<Rule> rules = knowledgePackage.getRules();
    assertThat(rules).as("Rules in loaded package").hasSize(2);

    final Rule rule = rules.iterator().next();
    assertThat(rule.getName()).as("Rule Name").isEqualTo(EXPECTED_RULE_NAME);
  }

  @Test
  @DefaultSettingsFile
  @ExecuteGoals("compile")
  @Parameters("project.version")
  public void testGeneratedRuleFiresForLightMelon(String projectVersion) throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException {
    final String fruitsModelKnowledgePackage = verifier.getArtifactPath("de.lightful.maven.plugins.drools", "drools-fruit-types", projectVersion, WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE);
    KnowledgePackageFile fruitsModelKnowledgeFile = new KnowledgePackageFile(new File(fruitsModelKnowledgePackage));
    final Collection<KnowledgePackage> fruitsModelKnowledgePackages = fruitsModelKnowledgeFile.getKnowledgePackages();
    final String vehiclesModelKnowledgePackage = verifier.getArtifactPath("de.lightful.maven.plugins.drools", "drools-vehicle-types", projectVersion, WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE);
    KnowledgePackageFile vehiclesModelKnowledgeFile = new KnowledgePackageFile(new File(vehiclesModelKnowledgePackage));
    final Collection<KnowledgePackage> vehiclesModelKnowledgePackages = vehiclesModelKnowledgeFile.getKnowledgePackages();

    KnowledgePackageFile rulesKnowledgeFile = new KnowledgePackageFile(expectedOutputFile(verifier, EXPECTED_OUTPUT_FILE));
    final Collection<KnowledgePackage> ruleKnowledgePackages = rulesKnowledgeFile.getKnowledgePackages();

    final KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
    knowledgeBase.addKnowledgePackages(fruitsModelKnowledgePackages);   // order of adding packages is crucial!
    knowledgeBase.addKnowledgePackages(vehiclesModelKnowledgePackages); // first: model, then: rules using the model(s)
    knowledgeBase.addKnowledgePackages(ruleKnowledgePackages);
    final StatefulKnowledgeSession session = knowledgeBase.newStatefulKnowledgeSession();

    final FactType fruitType = knowledgeBase.getFactType("model.fruit", "Fruit");
    final FactType weightType = knowledgeBase.getFactType("model.fruit", "WeightOfFruit");

    final Object fruit = fruitType.newInstance();
    fruitType.set(fruit, "name", "MELON");
    session.insert(fruit);

    final Object weight = weightType.newInstance();
    weightType.set(weight, "fruit", fruit);
    weightType.set(weight, "weight", 1);
    session.insert(weight);

    final FactType vehicleType = knowledgeBase.getFactType("model.vehicle", "Vehicle");
    final FactType manufacturerType = knowledgeBase.getFactType("model.vehicle", "Manufacturer");

    final Object manufacturer = manufacturerType.newInstance();
    manufacturerType.set(manufacturer, "name", "SloBuCars");
    session.insert(manufacturer);

    final Object vehicle = vehicleType.newInstance();
    vehicleType.set(vehicle, "manufacturer", manufacturer);
    session.insert(vehicle);

    session.fireAllRules();

    final Collection<Object> resultObjects = session.getObjects(new ObjectFilter() {
      public boolean accept(Object o) {
        return o instanceof String;
      }
    });

    assertThat(resultObjects).containsOnly(
        "TOO_LIGHT",
        "NO_SLOW_BULKY_CARS_PLEASE"
    );
  }
}