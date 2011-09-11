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
package de.lightful.maven.plugins.drools.impl;

import org.drools.builder.ResourceType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

@Test
public class ResourceTypeDetectorTest {

  private ResourceTypeDetector resourceTypeDetector;

  @BeforeMethod
  public void setUp() {
    resourceTypeDetector = new ResourceTypeDetector();
  }

  @Test(dataProvider = "getTestDataForTypeDetection")
  public void testDetectTypeOf(File resourceFile, ResourceType expectedResourceType) throws Exception {
    assertThat(resourceTypeDetector.detectTypeOf(resourceFile)).as("Resource Type").isEqualTo(expectedResourceType);
  }

  @DataProvider
  private Object[][] getTestDataForTypeDetection() {
    return new Object[][] {
        {new File("rule.drl"), ResourceType.DRL},
        {new File("rule.DRL"), ResourceType.DRL},
        {new File("rule.DrL"), ResourceType.DRL},
        {new File("rule.dRL"), ResourceType.DRL},
    };
  }
}
