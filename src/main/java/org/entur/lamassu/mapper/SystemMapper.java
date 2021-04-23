package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.RentalApp;
import org.entur.lamassu.model.entities.RentalApps;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.TranslatedString;
import org.entur.lamassu.model.entities.Translation;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemMapper {
    public System mapSystem(SystemInformation.Data systemInformation, String language) {
        var system =  new System();
        system.setId(systemInformation.getSystemId());
        system.setLanguage(systemInformation.getLanguage());
        system.setName(mapTranslation(systemInformation.getName(), language));
        system.setShortName(mapTranslation(systemInformation.getShortName(), language));
        system.setOperator(mapTranslation(systemInformation.getOperator(), language));
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

    private Translation mapTranslation(String value, String language) {
        var translation = new Translation();
        var translatedString = new TranslatedString();
        translatedString.setLanguage(language);
        translatedString.setValue(value);
        translation.setTranslation(List.of(translatedString));
        return translation;
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
