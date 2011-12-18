package de.lightful.maven.plugins.drools.integrationtests;

import de.lightful.maven.plugins.drools.knowledgeio.KnowledgeIoFactory;
import de.lightful.maven.plugins.testing.MavenVerifierTest;
import org.testng.annotations.BeforeMethod;

public class MavenDroolsPluginIntegrationTest extends MavenVerifierTest {

  protected KnowledgeIoFactory knowledgeIoFactory;

  protected ClassLoader contextClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  @BeforeMethod
  public void setUpKnowledgeIoFactory() {
    knowledgeIoFactory = new KnowledgeIoFactory();
  }
}
