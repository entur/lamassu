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

package org.entur.lamassu.mapper.entitymapper;

import org.entur.lamassu.model.entities.Region;
import org.mobilitydata.gbfs.v3_0.system_regions.GBFSRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegionMapper {

  private final TranslationMapper translationMapper;

  @Autowired
  public RegionMapper(TranslationMapper translationMapper) {
    this.translationMapper = translationMapper;
  }

  public Region mapRegion(GBFSRegion sourceRegion, String language) {
    var region = new Region();
    region.setId(sourceRegion.getRegionId());
    region.setName(
      translationMapper.mapSingleTranslation(
        language,
        sourceRegion
          .getName()
          .stream()
          .filter(name -> name.getLanguage().equals(language))
          .map(org.mobilitydata.gbfs.v3_0.system_regions.GBFSName::getText)
          .findFirst()
          .orElse(null)
      )
    );
    return region;
  }
}
