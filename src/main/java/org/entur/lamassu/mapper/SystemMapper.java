package org.entur.lamassu.mapper;

import org.entur.gbfs.v2_2.system_information.GBFSAndroid;
import org.entur.gbfs.v2_2.system_information.GBFSData;
import org.entur.gbfs.v2_2.system_information.GBFSIos;
import org.entur.gbfs.v2_2.system_information.GBFSRentalApps;
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

    private Operator mapOperator(String operatorName, FeedProvider feedProvider) {
        var operator = new Operator();
        operator.setId(feedProvider.getOperatorId());
        operator.setName(
                translationMapper.mapSingleTranslation(
                        feedProvider.getLanguage(),
                        operatorName != null ? operatorName : feedProvider.getOperatorName()
                )
        );
        return operator;
    }

    public System mapSystem(GBFSData systemInformation, FeedProvider feedProvider) {
        var system =  new System();
        system.setId(systemInformation.getSystemId());
        system.setLanguage(systemInformation.getLanguage());
        system.setName(translationMapper.mapSingleTranslation(feedProvider.getLanguage(), systemInformation.getName()));
        system.setShortName(translationMapper.mapSingleTranslation(feedProvider.getLanguage(), systemInformation.getShortName()));
        system.setOperator(mapOperator(systemInformation.getOperator(), feedProvider));
        system.setUrl(systemInformation.getUrl());
        system.setPurchaseUrl(systemInformation.getPurchaseUrl());
        system.setStartDate(systemInformation.getStartDate());
        system.setPhoneNumber(systemInformation.getPhoneNumber());
        system.setEmail(systemInformation.getEmail());
        system.setFeedContactEmail(system.getFeedContactEmail());
        system.setTimezone(systemInformation.getTimezone());
        system.setLicenseUrl(systemInformation.getLicenseUrl());
        system.setRentalApps(mapRentalApps(systemInformation.getRentalApps()));
        return system;
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
