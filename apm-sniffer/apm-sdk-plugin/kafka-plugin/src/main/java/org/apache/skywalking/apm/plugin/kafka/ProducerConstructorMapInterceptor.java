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

package org.apache.skywalking.apm.plugin.kafka;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class ProducerConstructorMapInterceptor implements InstanceConstructorInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    TraceUtils traceUtils = new TraceUtils(logger, "spring-kafka-consumer");

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        Map<String, Object> config = (Map<String, Object>) allArguments[0];
        // prevent errors caused by secondary interception in kafkaTemplate
        ArrayList<String> configs = (ArrayList<String>) config.get("bootstrap.servers");
        if (objInst.getSkyWalkingDynamicField() == null) {
            objInst.setSkyWalkingDynamicField(StringUtil.join(';', configs.toArray(new String[0])));
        }
        traceUtils.showTrace("触发构造 - 生产map器");
    }
}