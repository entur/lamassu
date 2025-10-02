package org.entur.lamassu.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSData;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs;
import org.springframework.web.server.ResponseStatusException;

public class GBFSV3FeedControllerTest {

  public static final String KNOWN_SYSTEM_ID = "knownSystem";
  private GBFSV3FeedController feedController;
  private FeedProviderService mockedFeedProviderService;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private GBFSV3FeedCache mockedFeedCache;

  @Before
  public void before() {
    SystemDiscoveryService systemDiscoveryService = mock(SystemDiscoveryService.class);
    mockedFeedCache = mock(GBFSV3FeedCache.class);
    mockedFeedProviderService = mock(FeedProviderService.class);

    feedController =
      new GBFSV3FeedController(
        systemDiscoveryService,
        mockedFeedCache,
        mockedFeedProviderService
      );
  }

  @Test
  public void throws400OnNonGBFSFeedRequest() {
    expectedException.expect(ResponseStatusException.class);
    expectedException.expectMessage("400 BAD_REQUEST");

    feedController.getV3Feed("anySystem", "no-gbfs-feed", null);
  }

  @Test
  public void throws404OnNonConfiguredSystemRequest() {
    expectedException.expect(ResponseStatusException.class);
    expectedException.expectMessage("404 NOT_FOUND");
    feedController.getV3Feed("unknownSystem", "gbfs", null);
  }

  @Test
  public void throws502OnConfiguredSystemButUnavailableFeedRequest() {
    FeedProvider feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);

    expectedException.expect(UpstreamFeedNotYetAvailableException.class);
    feedController.getV3Feed(KNOWN_SYSTEM_ID, "gbfs", null);
  }

  @Test
  public void throws404OnConfiguredSystemButUndeclaredFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    var gbfs = createDiscoveryFileWithFeed(GBFSFeed.Name.GBFS);

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeed.Name.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeed.Name.GEOFENCING_ZONES, feedProvider))
      .thenReturn(null);
    expectedException.expect(ResponseStatusException.class);
    expectedException.expectMessage("404 NOT_FOUND");
    feedController.getV3Feed(KNOWN_SYSTEM_ID, "geofencing_zones", null);
  }

  @Test
  public void throws502OnConfiguredSystemAndDeclaredFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    var gbfs = createDiscoveryFileWithFeed(GBFSFeed.Name.GEOFENCING_ZONES);

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeed.Name.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeed.Name.GEOFENCING_ZONES, feedProvider))
      .thenReturn(null);
    expectedException.expect(UpstreamFeedNotYetAvailableException.class);
    feedController.getV3Feed(KNOWN_SYSTEM_ID, "geofencing_zones", null);
  }

  @Test
  public void throws502OnConfiguredSystemAndMalformedDiscoveryFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    // GBFS is malformed, as it has no feeds defined
    var gbfs = new GBFSGbfs();

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeed.Name.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeed.Name.GEOFENCING_ZONES, feedProvider))
      .thenReturn(null);
    expectedException.expect(UpstreamFeedNotYetAvailableException.class);
    feedController.getV3Feed(KNOWN_SYSTEM_ID, "geofencing_zones", null);
  }

  public GBFSGbfs createDiscoveryFileWithFeed(GBFSFeed.Name feedName) {
    var gbfs = new GBFSGbfs();
    var data = new GBFSData();
    var feed = new GBFSFeed().withName(feedName);
    data.setFeeds(List.of(feed));
    gbfs.setData(data);
    return gbfs;
  }
}
