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

import org.entur.gbfs.v2_2.system_information.GBFSData;
import org.entur.gbfs.v2_2.system_information.GBFSSystemInformation;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemInformationFeedMapper extends AbstractFeedMapper<GBFSSystemInformation> {
    @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
    private String targetGbfsVersion;

    @Value("${org.entur.lamassu.targetLanguageCode:nb}")
    private String targetLanguageCode;

    @Override
    public GBFSSystemInformation map(GBFSSystemInformation source, FeedProvider feedProvider) {
        var mapped = new GBFSSystemInformation();
        mapped.setVersion(GBFSSystemInformation.Version.fromValue(targetGbfsVersion));
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setTtl(source.getTtl());
        mapped.setData(mapData(source.getData(), feedProvider));
        return mapped;
    }

    private GBFSData mapData(GBFSData source, FeedProvider feedProvider) {
        if (source ==  null) {
            return null;
        }

        var mapped = new GBFSData();
        mapped.setSystemId(feedProvider.getSystemId());
        mapped.setLanguage(targetLanguageCode);
        mapped.setName(source.getName());
        mapped.setShortName(source.getShortName());
        mapped.setOperator(source.getOperator() != null ? source.getOperator() : feedProvider.getOperatorName());
        mapped.setUrl(source.getUrl());
        mapped.setPurchaseUrl(source.getPurchaseUrl());
        mapped.setStartDate(source.getStartDate());
        mapped.setPhoneNumber(source.getPhoneNumber());
        mapped.setEmail(source.getEmail());
        mapped.setFeedContactEmail(source.getFeedContactEmail());
        mapped.setTimezone(source.getTimezone());
        mapped.setLicenseUrl(source.getLicenseUrl());
        mapped.setRentalApps(source.getRentalApps());
        return mapped;
    }
}
