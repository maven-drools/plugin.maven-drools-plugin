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
package de.lightful.maven.plugins.drools.impl.config;

public class ConfigurationValidator {

  public void validateConfiguration(Pass[] passes) {
    int currentPassNumber = Pass.FIRST_SEQUENCE_NUMBER;

    for (Pass pass : passes) {
      pass.setSequenceNumber(currentPassNumber);
      if (pass.getName() == null || "".equals(pass.getName())) {
        pass.setName("Pass #" + currentPassNumber);
      }
      if (pass.getIncludes() == null || pass.getIncludes().length == 0) {
        pass.setIncludes(new String[] {"**/*.drl"});
      }

      currentPassNumber++;
    }
  }
}
