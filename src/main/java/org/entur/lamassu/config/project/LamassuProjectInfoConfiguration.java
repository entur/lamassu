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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LamassuProjectInfoConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(
    LamassuProjectInfoConfiguration.class
  );

  private final LamassuProjectInfo projectInfo;

  public LamassuProjectInfoConfiguration() throws IOException {
    this.projectInfo = LamassuProjectInfo.loadFromProperties();
    LOG.info("Project version: {}", getProjectVersion());
    LOG.info("Serialization version : {}", getSerializationVersion());
  }

  public String getSerializationVersion() {
    return projectInfo.getSerializationVersion();
  }

  public String getProjectVersion() {
    return projectInfo.getProjectVersion();
  }
}
