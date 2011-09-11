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

import java.io.File;
import java.util.Arrays;

/** Since the Drools packaging APIs do not support forward references, we allow specification of any number of compiler passes. */
public class Pass {

  public static final int FIRST_SEQUENCE_NUMBER = 1;
  private int sequenceNumber;

  private String name;

  private File ruleSourceRoot;

  private String[] includes;

  private String[] excludes;

  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getRuleSourceRoot() {
    return ruleSourceRoot;
  }

  public void setRuleSourceRoot(File ruleSourceRoot) {
    this.ruleSourceRoot = ruleSourceRoot;
  }

  public String[] getIncludes() {
    return includes;
  }

  public void setIncludes(String[] includes) {
    this.includes = includes;
  }

  public String[] getExcludes() {
    return excludes;
  }

  public void setExcludes(String[] excludes) {
    this.excludes = excludes;
  }

  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Pass");
    sb.append("{name='").append(name).append('\'');
    sb.append(", ruleSourceRoot=").append(ruleSourceRoot);
    sb.append(", includes=").append(includes == null ? "null" : Arrays.asList(includes).toString());
    sb.append(", excludes=").append(excludes == null ? "null" : Arrays.asList(excludes).toString());
    sb.append('}');
    return sb.toString();
  }
}
