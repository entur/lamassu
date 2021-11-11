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

package org.entur.lamassu.model.provider;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.entur.gbfs.authentication.RequestAuthenticationException;
import org.entur.gbfs.authentication.RequestAuthenticator;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BoltRequestAuthenticator implements RequestAuthenticator {
    private final String url;
    private final String userName;
    private final String userPass;

    private final HttpClient client = HttpClientBuilder.create()
            .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(5000).build())
            .setConnectionTimeToLive(5000, TimeUnit.MILLISECONDS)
            .build();

    public BoltRequestAuthenticator(
            String url,
            String userName,
            String userPass
    ) {
        this.url = url;
        this.userName = userName;
        this.userPass = userPass;
    }

    @Override
    public void authenticateRequest(Map<String, String> map) throws RequestAuthenticationException {
        try {
            HttpPost httpPost = new HttpPost(url);
            String json = "{\"user_name\":\"" + userName + "\",\"user_pass\":\"" + userPass + "\"}";
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(httpPost);
            String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(responseJson);
            map.put("Authorization", "Bearer " + jsonObject.get("access_token"));
        } catch (Exception e) {
            throw new RequestAuthenticationException(e.getCause());
        }
    }
}
