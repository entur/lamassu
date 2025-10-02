package org.entur.lamassu.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.entur.lamassu.cache.GBFSV2FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFS;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeeds;
import org.springframework.web.server.ResponseStatusException;

public class GBFSV2FeedControllerTest {

  public static final String KNOWN_SYSTEM_ID = "knownSystem";
  private GBFSV2FeedController feedController;
  private FeedProviderService mockedFeedProviderService;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private GBFSV2FeedCache mockedFeedCache;

  @Before
  public void before() {
    SystemDiscoveryService systemDiscoveryService = mock(SystemDiscoveryService.class);
    mockedFeedCache = mock(GBFSV2FeedCache.class);
    mockedFeedProviderService = mock(FeedProviderService.class);

    feedController =
      new GBFSV2FeedController(
        systemDiscoveryService,
        mockedFeedCache,
        mockedFeedProviderService
      );
  }

  @Test
  public void throws400OnNonGBFSFeedRequest() {
    expectedException.expect(ResponseStatusException.class);
    expectedException.expectMessage("400 BAD_REQUEST");

    feedController.getGbfsFeedForProvider("anySystem", "no-gbfs-feed", null);
  }

  @Test
  public void throws404OnNonConfiguredSystemRequest() {
    expectedException.expect(ResponseStatusException.class);
    expectedException.expectMessage("404 NOT_FOUND");
    feedController.getGbfsFeedForProvider("unknownSystem", "gbfs", null);
  }

  @Test
  public void throws502OnConfiguredSystemButUnavailableFeedRequest() {
    FeedProvider feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);

    expectedException.expect(UpstreamFeedNotYetAvailableException.class);
    feedController.getGbfsFeedForProvider(KNOWN_SYSTEM_ID, "gbfs", null);
  }

  @Test
  public void throws404OnConfiguredSystemButUndeclaredFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    var gbfs = createDiscoveryFileWithFeed(GBFSFeedName.GBFS);

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeedName.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeedName.GeofencingZones, feedProvider))
      .thenReturn(null);
    expectedException.expect(ResponseStatusException.class);
    expectedException.expectMessage("404 NOT_FOUND");
    feedController.getGbfsFeedForProvider(KNOWN_SYSTEM_ID, "geofencing_zones", null);
  }

  @Test
  public void throws502OnConfiguredSystemAndDeclaredFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    var gbfs = createDiscoveryFileWithFeed(GBFSFeedName.GeofencingZones);

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeedName.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeedName.GeofencingZones, feedProvider))
      .thenReturn(null);
    expectedException.expect(UpstreamFeedNotYetAvailableException.class);
    feedController.getGbfsFeedForProvider(KNOWN_SYSTEM_ID, "geofencing_zones", null);
  }

  @Test
  public void throws502OnConfiguredSystemAndMalformedDiscoveryFeedRequest() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId(KNOWN_SYSTEM_ID);
    // GBFS is malformed, as it has no feeds defined
    var gbfs = new GBFS();

    when(mockedFeedProviderService.getFeedProviderBySystemId(KNOWN_SYSTEM_ID))
      .thenReturn(feedProvider);
    when(mockedFeedCache.find(GBFSFeedName.GBFS, feedProvider)).thenReturn(gbfs);
    when(mockedFeedCache.find(GBFSFeedName.GeofencingZones, feedProvider))
      .thenReturn(null);
    expectedException.expect(UpstreamFeedNotYetAvailableException.class);
    feedController.getGbfsFeedForProvider(KNOWN_SYSTEM_ID, "geofencing_zones", null);
  }

  public GBFS createDiscoveryFileWithFeed(GBFSFeedName feedName) {
    var gbfs = new GBFS();
    var feeds = new GBFSFeeds();
    var feed = new GBFSFeed();
    feed.setName(feedName);
    feeds.setFeeds(List.of(feed));
    gbfs.setFeedsData(Map.of("en", feeds));
    return gbfs;
  }
}
