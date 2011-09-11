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
package de.lightful.maven.plugins.drools.impl.logging;

import de.lightful.maven.plugins.drools.knowledgeio.LogStream;
import org.apache.maven.plugin.logging.Log;

public abstract class MavenLogStream<SELF_TYPE extends MavenLogStream<SELF_TYPE>> implements LogStream<SELF_TYPE> {

  private static final String NEWLINE = System.getProperty("line.separator");
  protected StringBuilder stringBuilder = new StringBuilder();
  protected Log mavenLog;

  public void setMavenLog(Log mavenLog) {
    this.mavenLog = mavenLog;
  }

  public abstract void writeToStream();

  @SuppressWarnings("unchecked")
  protected SELF_TYPE self_type() {
    return (SELF_TYPE) this;
  }

  public SELF_TYPE write(String message) {
    stringBuilder.append(message);
    if (message.endsWith(NEWLINE)) {
      return nl();
    }
    else {
      return self_type();
    }
  }

  public SELF_TYPE nl() {
    writeToStream();
    stringBuilder.setLength(0);
    return self_type();
  }
}
