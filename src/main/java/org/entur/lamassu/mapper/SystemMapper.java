package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.RentalApp;
import org.entur.lamassu.model.entities.RentalApps;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Translation;
import org.entur.lamassu.model.entities.TranslatedString;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemMapper {

    private final TranslationMapper translationMapper;

    @Autowired
    public SystemMapper(TranslationMapper translationMapper) {
        this.translationMapper = translationMapper;
    }

    public System mapSystem(SystemInformation.Data systemInformation, String language) {
        var system =  new System();
        system.setId(systemInformation.getSystemId());
        system.setLanguage(systemInformation.getLanguage());
        system.setName(translationMapper.mapSingleTranslation(language, systemInformation.getName()));
        system.setShortName(translationMapper.mapSingleTranslation(language, systemInformation.getShortName()));
        system.setOperator(translationMapper.mapSingleTranslation(language, systemInformation.getOperator()));
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

    private RentalApps mapRentalApps(SystemInformation.RentalApps sourceRentalApps) {
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

    private RentalApp mapRentalApp(SystemInformation.RentalApp sourceRentalApp) {
        var rentalApp = new RentalApp();
        rentalApp.setStoreUri(sourceRentalApp.getStoreURI());
        rentalApp.setDiscoveryUri(sourceRentalApp.getDiscoveryURI());
        return rentalApp;
    }
}
