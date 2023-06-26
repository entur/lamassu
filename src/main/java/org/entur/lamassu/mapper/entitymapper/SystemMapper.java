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

import org.entur.gbfs.v2_3.system_information.GBFSAndroid;
import org.entur.gbfs.v2_3.system_information.GBFSBrandAssets;
import org.entur.gbfs.v2_3.system_information.GBFSData;
import org.entur.gbfs.v2_3.system_information.GBFSIos;
import org.entur.gbfs.v2_3.system_information.GBFSRentalApps;
import org.entur.lamassu.model.entities.BrandAssets;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.model.entities.RentalApp;
import org.entur.lamassu.model.entities.RentalApps;
import org.entur.lamassu.model.entities.System;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemMapper {

    private final TranslationMapper translationMapper;

    @Autowired
    public SystemMapper(TranslationMapper translationMapper) {
        this.translationMapper = translationMapper;
    }

    private Operator mapOperator(String operatorName, FeedProvider feedProvider, String language) {
        var operator = new Operator();
        operator.setId(feedProvider.getOperatorId());
        operator.setName(
                translationMapper.mapSingleTranslation(
                        language,
                        operatorName != null ? operatorName : feedProvider.getOperatorName()
                )
        );
        return operator;
    }

    public System mapSystem(GBFSData systemInformation, FeedProvider feedProvider) {
        var system =  new System();
        system.setId(systemInformation.getSystemId());
        system.setLanguage(systemInformation.getLanguage());
        system.setName(translationMapper.mapSingleTranslation(systemInformation.getLanguage(), systemInformation.getName()));
        system.setShortName(translationMapper.mapSingleTranslation(systemInformation.getLanguage(), systemInformation.getShortName()));
        system.setOperator(mapOperator(systemInformation.getOperator(), feedProvider, systemInformation.getLanguage()));
        system.setUrl(systemInformation.getUrl());
        system.setPurchaseUrl(systemInformation.getPurchaseUrl());
        system.setStartDate(systemInformation.getStartDate());
        system.setPhoneNumber(systemInformation.getPhoneNumber());
        system.setEmail(systemInformation.getEmail());
        system.setFeedContactEmail(system.getFeedContactEmail());
        system.setTimezone(systemInformation.getTimezone().value());
        system.setLicenseUrl(systemInformation.getLicenseUrl());
        system.setBrandAssets(mapBrandAssets(systemInformation.getBrandAssets()));
        system.setTermsUrl(systemInformation.getTermsUrl());
        system.setTermsLastUpdated(systemInformation.getTermsLastUpdated());
        system.setPrivacyUrl(systemInformation.getPrivacyUrl());
        system.setPrivacyLastUpdated(systemInformation.getPrivacyLastUpdated());
        system.setRentalApps(mapRentalApps(systemInformation.getRentalApps()));
        return system;
    }

    private BrandAssets mapBrandAssets(GBFSBrandAssets brandAssets) {
        if (brandAssets == null) {
            return null;
        }

        var mapped = new BrandAssets();
        mapped.setBrandImageUrl(brandAssets.getBrandImageUrl());
        mapped.setBrandLastModified(brandAssets.getBrandLastModified());
        mapped.setBrandTermsUrl(brandAssets.getBrandTermsUrl());
        mapped.setBrandImageUrlDark(brandAssets.getBrandImageUrlDark());
        mapped.setColor(brandAssets.getColor());

        return mapped;
    }

    private RentalApps mapRentalApps(GBFSRentalApps sourceRentalApps) {
        if (sourceRentalApps == null) {
            return null;
        }

        var rentalApps = new RentalApps();

        if (sourceRentalApps.getAndroid() != null) {
            rentalApps.setAndroid(mapRentalApp(sourceRentalApps.getAndroid()));
        }

        if (sourceRentalApps.getIos() != null) {
            rentalApps.setIos(mapRentalApp(sourceRentalApps.getIos()));
        }

        return rentalApps;
    }

    private RentalApp mapRentalApp(GBFSAndroid sourceRentalApp) {
        var rentalApp = new RentalApp();
        rentalApp.setStoreUri(sourceRentalApp.getStoreUri());
        rentalApp.setDiscoveryUri(sourceRentalApp.getDiscoveryUri());
        return rentalApp;
    }

    private RentalApp mapRentalApp(GBFSIos sourceRentalApp) {
        var rentalApp = new RentalApp();
        rentalApp.setStoreUri(sourceRentalApp.getStoreUri());
        rentalApp.setDiscoveryUri(sourceRentalApp.getDiscoveryUri());
        return rentalApp;
    }
}
