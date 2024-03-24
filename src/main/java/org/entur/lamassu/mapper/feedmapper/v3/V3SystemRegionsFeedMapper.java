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

package org.entur.lamassu.mapper.feedmapper.v3;

import java.util.List;
import java.util.stream.Collectors;
import org.entur.gbfs.v3_0_RC2.system_regions.GBFSData;
import org.entur.gbfs.v3_0_RC2.system_regions.GBFSRegion;
import org.entur.gbfs.v3_0_RC2.system_regions.GBFSSystemRegions;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.mapper.feedmapper.IdMappers;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.stereotype.Component;

@Component
public class V3SystemRegionsFeedMapper extends AbstractFeedMapper<GBFSSystemRegions> {

  private static final GBFSSystemRegions.Version VERSION =
    GBFSSystemRegions.Version._3_0_RC_2;

  @Override
  public GBFSSystemRegions map(GBFSSystemRegions source, FeedProvider feedProvider) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSSystemRegions();
    mapped.setVersion(VERSION);
    mapped.setTtl(source.getTtl());
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setData(mapData(source.getData(), feedProvider.getCodespace()));
    return mapped;
  }

  private GBFSData mapData(GBFSData data, String codespace) {
    var mapped = new GBFSData();
    mapped.setRegions(mapRegions(data.getRegions(), codespace));
    return mapped;
  }

  private List<GBFSRegion> mapRegions(List<GBFSRegion> regions, String codespace) {
    return regions
      .stream()
      .map(region -> mapRegion(region, codespace))
      .collect(Collectors.toList());
  }

  private GBFSRegion mapRegion(GBFSRegion region, String codespace) {
    var mapped = new GBFSRegion();
    mapped.setRegionId(
      IdMappers.mapId(codespace, IdMappers.REGION_ID_TYPE, region.getRegionId())
    );
    mapped.setName(region.getName());
    return mapped;
  }
}
