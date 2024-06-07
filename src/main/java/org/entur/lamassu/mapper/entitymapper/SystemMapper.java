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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.entur.lamassu.model.entities.BrandAssets;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.model.entities.RentalApp;
import org.entur.lamassu.model.entities.RentalApps;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.TranslatedString;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSAndroid;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSAttributionOrganizationName;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSBrandAssets;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSData;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSIos;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSOperator;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSPrivacyUrl;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSRentalApps;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSTermsUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemMapper {

  private final TranslationMapper translationMapper;

  @Autowired
  public SystemMapper(TranslationMapper translationMapper) {
    this.translationMapper = translationMapper;
  }

  private Operator mapOperator(
    List<GBFSOperator> operatorName,
    FeedProvider feedProvider
  ) {
    var operator = new Operator();
    operator.setId(feedProvider.getOperatorId());
    operator.setName(
      translationMapper.mapTranslatedString(
        operatorName
          .stream()
          .map(name ->
            translationMapper.mapTranslation(name.getLanguage(), name.getText())
          )
          .toList()
      )
    );
    return operator;
  }

  public System mapSystem(GBFSData systemInformation, FeedProvider feedProvider) {
    var system = new System();
    system.setId(systemInformation.getSystemId());
    system.setLanguage(feedProvider.getLanguage());
    system.setLanguages(systemInformation.getLanguages());
    system.setName(
      translationMapper.mapTranslatedString(
        systemInformation
          .getName()
          .stream()
          .map(name ->
            translationMapper.mapTranslation(name.getLanguage(), name.getText())
          )
          .toList()
      )
    );
    system.setShortName(
      translationMapper.mapTranslatedString(
        Optional
          .ofNullable(systemInformation.getShortName())
          .orElse(Collections.emptyList())
          .stream()
          .map(shortName ->
            translationMapper.mapTranslation(shortName.getLanguage(), shortName.getText())
          )
          .toList()
      )
    );
    system.setOpeningHours(systemInformation.getOpeningHours());
    system.setOperator(mapOperator(systemInformation.getOperator(), feedProvider));
    system.setUrl(systemInformation.getUrl());
    system.setPurchaseUrl(systemInformation.getPurchaseUrl());
    system.setStartDate(systemInformation.getStartDate());
    system.setPhoneNumber(systemInformation.getPhoneNumber());
    system.setEmail(systemInformation.getEmail());
    system.setFeedContactEmail(system.getFeedContactEmail());
    system.setTimezone(
      systemInformation.getTimezone() != null
        ? systemInformation.getTimezone().value()
        : null
    );
    system.setLicenseUrl(systemInformation.getLicenseUrl());
    system.setBrandAssets(mapBrandAssets(systemInformation.getBrandAssets()));
    system.setTermsUrl(
      Optional
        .ofNullable(systemInformation.getTermsUrl())
        .orElse(Collections.emptyList())
        .stream()
        .filter(termsUrl -> termsUrl.getLanguage().equals(feedProvider.getLanguage()))
        .map(GBFSTermsUrl::getText)
        .findFirst()
        .orElse(null)
    );
    system.setTermsLastUpdated(systemInformation.getTermsLastUpdated());
    system.setPrivacyUrl(
      Optional
        .ofNullable(systemInformation.getPrivacyUrl())
        .orElse(Collections.emptyList())
        .stream()
        .filter(privacyUrl -> privacyUrl.getLanguage().equals(feedProvider.getLanguage()))
        .map(GBFSPrivacyUrl::getText)
        .findFirst()
        .orElse(null)
    );
    system.setPrivacyLastUpdated(systemInformation.getPrivacyLastUpdated());
    system.setAttributionOrganizationName(
      mapAttributionOrganizationName(systemInformation.getAttributionOrganizationName())
    );
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

  private TranslatedString mapAttributionOrganizationName(
    List<GBFSAttributionOrganizationName> attributionOrganizationName
  ) {
    if (attributionOrganizationName == null) {
      return null;
    }

    var mapped = new TranslatedString();
    mapped.setTranslation(
      attributionOrganizationName
        .stream()
        .map(name -> translationMapper.mapTranslation(name.getLanguage(), name.getText()))
        .toList()
    );
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
