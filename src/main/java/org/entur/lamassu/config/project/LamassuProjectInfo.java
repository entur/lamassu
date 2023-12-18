/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.config.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LamassuProjectInfo {

  private static final String FILENAME = "lamassu-project-info.properties";

  static LamassuProjectInfo loadFromProperties() throws IOException {
    InputStream in =
      LamassuProjectInfo.class.getClassLoader().getResourceAsStream(FILENAME);
    Properties props = new java.util.Properties();
    props.load(in);

    return new LamassuProjectInfo(
      props.getProperty("project.version"),
      props.getProperty("lamassu.serialization.version.id")
    );
  }

  private final String projectVersion;
  private final String serializationVersion;

  LamassuProjectInfo(String projectVersion, String serializationVersion) {
    this.projectVersion = projectVersion;
    this.serializationVersion = serializationVersion;
  }

  public String getProjectVersion() {
    return projectVersion;
  }

  public String getSerializationVersion() {
    return serializationVersion;
  }
}
