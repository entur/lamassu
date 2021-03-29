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

package org.entur.lamassu.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseRoute extends RouteBuilder {

    private GBFSFeedCache feedCache;
    private FeedProviderConfig feedProviderConfig;

    @Autowired
    public BaseRoute(GBFSFeedCache feedCache, FeedProviderConfig feedProviderConfig) {
        this.feedCache = feedCache;
        this.feedProviderConfig = feedProviderConfig;
    }

    @Override
    public void configure() throws Exception {

        restConfiguration()
                .component("servlet")
                .contextPath("services")
                .host("0.0.0.0")
                .port(8080)
                .bindingMode(RestBindingMode.json);

        rest("/admin")
                .post("/update")
                .to("direct:updateAll");

        from("direct:updateAll")
                .process(e -> feedProviderConfig.getProviders())
                .split(body())
                .to("direct:updateProvider");

        from("direct:updateProvider")
                .convertBodyTo(FeedProvider.class)
                .setProperty("feedProvider", body())
                .toD("${body.getUrl()}")
                .convertBodyTo(GBFS.class)
                .to("direct:cacheDiscoveryFeed");

        from("direct:cacheDiscoveryFeed")
                .bean("discoveryFeedMapper", "mapDiscoveryFeed")
                .process(e ->
                        feedCache.update(
                                GBFSFeedName.GBFS,
                                propertyInject("feedProvider", FeedProvider.class),
                                body().evaluate(e, GBFSBase.class)
                        )
                );

    }
}
