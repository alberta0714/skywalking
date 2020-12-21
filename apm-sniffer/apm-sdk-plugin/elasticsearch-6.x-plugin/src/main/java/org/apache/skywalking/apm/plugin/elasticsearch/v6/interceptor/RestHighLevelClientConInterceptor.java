/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.elasticsearch.v6.interceptor;

import org.apache.http.HttpHost;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.plugin.elasticsearch.v6.RestClientEnhanceInfo;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;

//import org.elasticsearch.client.Node;

public class RestHighLevelClientConInterceptor implements InstanceConstructorInterceptor {

    private static final ILog logger = LogManager.getLogger(RestHighLevelClientConInterceptor.class);

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        RestClientBuilder restClientBuilder = (RestClientBuilder) (allArguments[0]);
        RestClient restClient = restClientBuilder.build();

        int port = -1;
        String hostname = "无法获取host";
        try {
            HttpHost httpHost = restClient.performRequest("GET", "").getHost();
            hostname = httpHost.getHostName();
            port = httpHost.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RestClientEnhanceInfo restClientEnhanceInfo = new RestClientEnhanceInfo();
        restClientEnhanceInfo.addHttpHost(new HttpHost(hostname, port));
//        List<Node> nodeList = restClient.getNodes();
//        for (Node node : nodeList) {
//            restClientEnhanceInfo.addHttpHost(node.getHost());
//        }
        objInst.setSkyWalkingDynamicField(restClientEnhanceInfo);
    }
}
