package org.entur.lamassu.leader.entityupdater;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.delta.DeltaType;
import org.entur.lamassu.delta.GBFSEntityDelta;
import org.entur.lamassu.delta.GBFSFileDelta;
import org.entur.lamassu.mapper.entitymapper.RentalUrisMapper;
import org.entur.lamassu.mapper.entitymapper.StationMapper;
import org.entur.lamassu.mapper.entitymapper.StationMergeMapper;
import org.entur.lamassu.mapper.entitymapper.StationMergeMapperImpl;
import org.entur.lamassu.mapper.entitymapper.TranslationMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.TranslatedString;
import org.entur.lamassu.model.entities.Translation;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SpatialIndexIdGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSName;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StationsUpdaterTest {

  @Mock
  private EntityCache<Station> stationCache;

  @Mock
  private StationSpatialIndex spatialIndex;

  @Mock
  private MetricsService metricsService;

  @Mock
  private SpatialIndexIdGeneratorService spatialIndexService;

  private StationMapper stationMapper;
  private StationMergeMapper stationMergeMapper;
  private StationsUpdater stationsUpdater;

  private StationSpatialIndexId createStationIndexId(String id, String systemId) {
    var indexId = new StationSpatialIndexId();
    indexId.setId(id);
    indexId.setSystemId(systemId);
    return indexId;
  }

  @BeforeEach
  void setUp() {
    // Initialize real mappers
    TranslationMapper translationMapper = new TranslationMapper();
    RentalUrisMapper rentalUrisMapper = new RentalUrisMapper();
    stationMapper = new StationMapper(translationMapper, rentalUrisMapper);
    stationMergeMapper = new StationMergeMapperImpl();

    stationsUpdater =
      new StationsUpdater(
        stationCache,
        spatialIndex,
        stationMapper,
        stationMergeMapper,
        metricsService,
        spatialIndexService
      );
    // Set default values for cache TTLs
    ReflectionTestUtils.setField(stationsUpdater, "stationEntityCacheMinimumTtl", 30);
    ReflectionTestUtils.setField(stationsUpdater, "stationEntityCacheMaximumTtl", 300);
  }

  @Test
  void shouldHandleStationUpdate() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");
    feedProvider.setLanguage("en");

    var stationId = "station-1";
    var currentStation = new Station();
    currentStation.setId(stationId);
    var oldName = new TranslatedString();
    var oldTranslation = new Translation();
    oldTranslation.setLanguage("en");
    oldTranslation.setValue("Old Station Name");
    oldName.setTranslation(List.of(oldTranslation));
    currentStation.setName(oldName);

    var updatedStation = new Station();
    updatedStation.setId(stationId);
    var newName = new TranslatedString();
    var newTranslation = new Translation();
    newTranslation.setLanguage("en");
    newTranslation.setValue("New Station Name");
    newName.setTranslation(List.of(newTranslation));
    updatedStation.setName(newName);

    var stationInfo = new GBFSStation();
    stationInfo.setStationId(stationId);
    var gbfsName = new GBFSName();
    gbfsName.setText("New Station Name");
    gbfsName.setLanguage("en");
    stationInfo.setName(List.of(gbfsName));

    var stationStatus = new org.mobilitydata.gbfs.v3_0.station_status.GBFSStation();
    stationStatus.setStationId(stationId);
    stationStatus.setNumVehiclesAvailable(5);

    var stationInformationFeed = new GBFSStationInformation();
    var data = new GBFSData();
    data.setStations(List.of(stationInfo));
    stationInformationFeed.setData(data);

    var spatialIndexId = createStationIndexId(stationId, "test-system");

    // Mock behavior
    when(stationCache.get(stationId)).thenReturn(currentStation);
    when(spatialIndexService.createStationIndexId(any(), eq(feedProvider)))
      .thenReturn(spatialIndexId);

    var delta = new GBFSFileDelta<org.mobilitydata.gbfs.v3_0.station_status.GBFSStation>(
      1000L,
      2000L,
      60L,
      "station_status",
      List.of(new GBFSEntityDelta<>(stationId, DeltaType.UPDATE, stationStatus))
    );

    // When
    stationsUpdater.addOrUpdateStations(feedProvider, delta, stationInformationFeed);

    // Then
    verify(spatialIndex).addAll(any());
    verify(stationCache).updateAll(any(), eq(60), eq(TimeUnit.SECONDS));
    verify(spatialIndex, never()).removeAll(anySet());
    verify(stationCache, never()).removeAll(anySet());
  }

  @Test
  void shouldHandleStationDelete() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");

    var stationId = "station-1";
    var currentStation = new Station();
    currentStation.setId(stationId);

    var spatialIndexId = createStationIndexId(stationId, "test-system");

    // Mock behavior
    when(stationCache.get(stationId)).thenReturn(currentStation);
    when(spatialIndexService.createStationIndexId(currentStation, feedProvider))
      .thenReturn(spatialIndexId);

    var delta = new GBFSFileDelta<org.mobilitydata.gbfs.v3_0.station_status.GBFSStation>(
      1000L,
      2000L,
      60L,
      "station_status",
      List.of(new GBFSEntityDelta<>(stationId, DeltaType.DELETE, null))
    );

    // When
    stationsUpdater.addOrUpdateStations(feedProvider, delta, null);

    // Then
    verify(spatialIndex).removeAll(Set.of(spatialIndexId));
    verify(stationCache).removeAll(Set.of(stationId));
    verify(spatialIndex, never()).addAll(anyMap());
    verify(stationCache, never()).updateAll(anyMap(), anyInt(), any(TimeUnit.class));
  }

  @Test
  void shouldHandleStationCreate() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");
    feedProvider.setLanguage("en");

    var stationId = "station-1";
    var newStation = new Station();
    newStation.setId(stationId);
    var name = new TranslatedString();
    var translation = new Translation();
    translation.setLanguage("en");
    translation.setValue("New Station");
    name.setTranslation(List.of(translation));
    newStation.setName(name);

    var stationInfo = new GBFSStation();
    stationInfo.setStationId(stationId);
    var gbfsName = new GBFSName();
    gbfsName.setText("New Station");
    gbfsName.setLanguage("en");
    stationInfo.setName(List.of(gbfsName));

    var stationStatus = new org.mobilitydata.gbfs.v3_0.station_status.GBFSStation();
    stationStatus.setStationId(stationId);
    stationStatus.setNumVehiclesAvailable(5);

    var stationInformationFeed = new GBFSStationInformation();
    var data = new GBFSData();
    data.setStations(List.of(stationInfo));
    stationInformationFeed.setData(data);

    var spatialIndexId = createStationIndexId(stationId, "test-system");

    // Mock behavior
    when(stationCache.get(stationId)).thenReturn(null);
    when(spatialIndexService.createStationIndexId(any(), eq(feedProvider)))
      .thenReturn(spatialIndexId);

    var delta = new GBFSFileDelta<org.mobilitydata.gbfs.v3_0.station_status.GBFSStation>(
      1000L,
      2000L,
      60L,
      "station_status",
      List.of(new GBFSEntityDelta<>(stationId, DeltaType.CREATE, stationStatus))
    );

    // When
    stationsUpdater.addOrUpdateStations(feedProvider, delta, stationInformationFeed);

    // Then
    verify(spatialIndex).addAll(any());
    verify(stationCache).updateAll(any(), eq(60), eq(TimeUnit.SECONDS));
    verify(spatialIndex, never()).removeAll(anySet());
    verify(stationCache, never()).removeAll(anySet());
  }

  @Test
  void shouldSkipUpdateWhenMissingStationInformation() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");

    var stationId = "station-1";
    var currentStation = new Station();
    currentStation.setId(stationId);

    var stationStatus = new org.mobilitydata.gbfs.v3_0.station_status.GBFSStation();
    stationStatus.setStationId(stationId);
    stationStatus.setNumVehiclesAvailable(5);

    // Mock behavior
    when(stationCache.get(stationId)).thenReturn(currentStation);

    var delta = new GBFSFileDelta<org.mobilitydata.gbfs.v3_0.station_status.GBFSStation>(
      1000L,
      2000L,
      60L,
      "station_status",
      List.of(new GBFSEntityDelta<>(stationId, DeltaType.UPDATE, stationStatus))
    );

    // When
    stationsUpdater.addOrUpdateStations(feedProvider, delta, null);

    // Then
    verify(spatialIndex, never()).addAll(anyMap());
    verify(spatialIndex, never()).removeAll(anySet());
    verify(stationCache, never()).updateAll(anyMap(), anyInt(), any(TimeUnit.class));
    verify(stationCache, never()).removeAll(anySet());
  }
}
