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


package org.apache.skywalking.apm.plugin.canal;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;

import java.net.InetSocketAddress;


/**
 * @author withlin
 */
public class CanalConstructorInterceptor implements InstanceConstructorInterceptor {

    /**
     * 构建连接时，记录连接的地址和实例到  enhancedInstance
     * @param objInst      com.alibaba.otter.canal.client.impl.SimpleCanalConnector
     * @param allArguments public SimpleCanalConnector(SocketAddress address, String username, String password, String destination){
     */
    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        InetSocketAddress address = (InetSocketAddress) allArguments[0];
        String destination = allArguments[3].toString();
        CanalEnhanceInfo canalEnhanceInfo = new CanalEnhanceInfo();
        if (address != null) {
            String url = address.getAddress().toString() + ":" + address.getPort();
            canalEnhanceInfo.setUrl(url.replace('/', ' '));
        }
        canalEnhanceInfo.setDestination(destination);
        objInst.setSkyWalkingDynamicField(canalEnhanceInfo);

    }
}