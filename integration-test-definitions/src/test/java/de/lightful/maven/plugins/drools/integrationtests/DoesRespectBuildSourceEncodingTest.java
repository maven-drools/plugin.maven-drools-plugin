package de.lightful.maven.plugins.drools.integrationtests;

import de.lightful.maven.drools.plugin.naming.WellKnownNames;
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

import static org.fest.assertions.Assertions.assertThat;

@Test
@DefaultSettingsFile
@ExecuteGoals("clean")
@VerifyUsingProject("does_respect_build_source_encoding")
public class DoesRespectBuildSourceEncodingTest extends MavenDroolsPluginIntegrationTest {

  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;

  @Inject
  private Verifier verifier;

  @Test
  @ExecuteGoals("compile")
  public void testPackagedRuleContainsCorrectName() throws Exception {
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);

    KnowledgeModuleReader knowledgeModuleReader = knowledgeIoFactory.createKnowledgeModuleReader(new FileInputStream(expectedOutputFile(verifier, EXPECTED_OUTPUT_FILE)), contextClassLoader());
    final Collection<KnowledgePackage> knowledgePackages = knowledgeModuleReader.readKnowledgePackages();
    assertThat(knowledgePackages).as("Number of rule packages").hasSize(1);
    KnowledgePackage knowledgePackage = knowledgePackages.iterator().next();
    Iterable<Rule> rules = knowledgePackage.getRules();
    assertThat(rules).as("Number of rules").hasSize(1);
    Rule rule = rules.iterator().next();
    assertThat(rule.getName()).isEqualTo("rule ђ CODEPAGE-855");
  }
}
