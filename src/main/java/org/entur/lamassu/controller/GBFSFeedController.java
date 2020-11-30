package org.entur.lamassu.controller;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GBFSFeedController {

    @Autowired
    GBFSFeedCache feedCache;

    @GetMapping("/gbfs/{codespace}/{city}/{feed}")
    public GBFSBase getGbfsFeedForProvider(@PathVariable String codespace, @PathVariable String city, @PathVariable String feed) {
        GBFSFeedName feedName = GBFSFeedName.valueOf(feed.toUpperCase());
        return feedCache.find(feedName, codespace, city);
    }
}
