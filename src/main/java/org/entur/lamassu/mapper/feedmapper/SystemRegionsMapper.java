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

package org.entur.lamassu.mapper.feedmapper;

import org.entur.gbfs.v2_2.system_regions.GBFSData;
import org.entur.gbfs.v2_2.system_regions.GBFSRegion;
import org.entur.gbfs.v2_2.system_regions.GBFSSystemRegions;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SystemRegionsMapper implements FeedMapper<GBFSSystemRegions> {
    @Override
    public GBFSSystemRegions map(GBFSSystemRegions source, FeedProvider feedProvider) {
        var mapped = new GBFSSystemRegions();
        mapped.setVersion(source.getVersion());
        mapped.setTtl(source.getTtl());
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setData(mapData(source.getData(), feedProvider.getCodespace()));
        return mapped;
    }

    private GBFSData mapData(GBFSData data, String codespace) {
        var mapped = new GBFSData();
        mapped.setRegions(mapRegions(data.getRegions(), codespace).orElse(null));
        return mapped;
    }

    private Optional<List<GBFSRegion>> mapRegions(List<GBFSRegion> regions, String codespace) {
        return Optional.ofNullable(regions)
                .map(r -> r.stream().map(region -> mapRegion(region, codespace)).collect(Collectors.toList())
        );
    }

    private GBFSRegion mapRegion(GBFSRegion region, String codespace) {
        var mapped = new GBFSRegion();
        mapped.setRegionId(IdMappers.mapId(codespace, IdMappers.REGION_ID_TYPE, region.getRegionId()));
        mapped.setName(region.getName());
        return mapped;
    }
}
