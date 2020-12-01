package org.entur.lamassu.controller;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GBFSFeedController {

    @Autowired
    GBFSFeedCache feedCache;

    @GetMapping("/gbfs/{codespace}/{feed}")
    public GBFSBase getGbfsFeedForProvider(@PathVariable String codespace,  @PathVariable String feed) {
        return getGbfsFeedForProvider(codespace, null, null, feed);
    }

    @GetMapping("/gbfs/{codespace}/{city}/{feed}")
    public GBFSBase getGbfsFeedForProvider(@PathVariable String codespace, @PathVariable String city, @PathVariable String feed) {
        return getGbfsFeedForProvider(codespace, city, null, feed);
    }

    @GetMapping("/gbfs/{codespace}/{city}/{vehicleType}/{feed}")
    public GBFSBase getGbfsFeedForProvider(@PathVariable String codespace, @PathVariable String city, @PathVariable String vehicleType, @PathVariable String feed) {
        try {
            GBFSFeedName feedName = GBFSFeedName.valueOf(feed.toUpperCase());
            return feedCache.find(feedName, codespace, city, vehicleType);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
