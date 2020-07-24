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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * producer是出口， consumer是入口。
 * org.apache.kafka.clients.consumer.KafkaConsumer
 * pollOnce/pollForFetches
 */
public class KafkaConsumerInterceptor implements InstanceMethodsAroundInterceptor {
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    TraceUtils traceUtils = new TraceUtils(logger, "kafka-consumer");

    public static final String OPERATE_NAME_PREFIX = "Kafka/";
    public static final String CONSUMER_OPERATE_NAME = "/Consumer/";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
        ConsumerEnhanceRequiredInfo requiredInfo = (ConsumerEnhanceRequiredInfo) objInst.getSkyWalkingDynamicField();
        requiredInfo.setStartTime(System.currentTimeMillis());
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret) throws Throwable {
        if (ret == null) {  return ret;  }
        Map<TopicPartition, List<ConsumerRecord<?, ?>>> records = (Map<TopicPartition, List<ConsumerRecord<?, ?>>>) ret;
        if (records.size() <= 0) {  return ret;  }

        ConsumerEnhanceRequiredInfo requiredInfo = (ConsumerEnhanceRequiredInfo) objInst.getSkyWalkingDynamicField();
        AbstractSpan activeSpan = ContextManager.createEntrySpan(OPERATE_NAME_PREFIX + requiredInfo.getTopics() + CONSUMER_OPERATE_NAME + requiredInfo
                .getGroupId(), null).start(requiredInfo.getStartTime());

        activeSpan.setComponent(ComponentsDefine.KAFKA_CONSUMER);
        activeSpan.tag(new StringTag("szc"),"kafka-consumer打标签测试");
        SpanLayer.asMQ(activeSpan);
        Tags.MQ_BROKER.set(activeSpan, requiredInfo.getBrokerServers());
        Tags.MQ_TOPIC.set(activeSpan, requiredInfo.getTopics());
//        this.showTrace("active span");

        for (List<ConsumerRecord<?, ?>> consumerRecords : records.values()) {
            for (ConsumerRecord<?, ?> record : consumerRecords) {
                ContextCarrier contextCarrier = new ContextCarrier();
                CarrierItem swNext = contextCarrier.items();
                while (swNext.hasNext()) {
                    swNext = swNext.next();
                    String swKey = swNext.getHeadKey();
                    Iterator<Header> iterator = record.headers().headers(swKey).iterator();
                    if (iterator.hasNext()) {
                        byte[] byteValue = iterator.next().value();
                        String valueStr = new String(byteValue);
//                        logger.info("kafka-consumer: valueStr:[{} {}]", valueStr, byteValue);
//                        String[] parts = valueStr.split("\\-");
//                        logger.info("head_length: {}", parts.length);
//                        for (int i = 0; i < parts.length; i++) { // 第0个只是代表header是否存在
//                            String swItem = parts[i];
//                            String swValue = swItem;
//                            if (i == 1 || i == 2 || i == 6 || i == 7 || i == 8) {
//                                try {
//                                    swValue = Base64.decode2UTFString(swItem);
//                                } catch (Exception e) {
//                                    logger.error("", e);
//                                }
//                            }
////                            String globaleTraceId = Base64.decode2UTFString(parts[1]);
////                            String segId = Base64.decode2UTFString(parts[2]);
////                            String spanId = Integer.parseInt(parts[3]);
////                            this.parentServiceInstanceId = Integer.parseInt(parts[4]);
////                            this.entryServiceInstanceId = Integer.parseInt(parts[5]);
////                            this.peerHost = Base64.decode2UTFString(parts[6]);
////                            this.entryEndpointName = Base64.decode2UTFString(parts[7]);
////                            this.parentEndpointName = Base64.decode2UTFString(parts[8]);
////                            logger.info("kafka-consumer {}_[{}]", i, swValue);
//                        }
                        /** @See {@link org.apache.skywalking.apm.agent.core.context.SW6CarrierItem#setHeadValue(String)} 等同直接反序列化，而未设置head value*/
                        swNext.setHeadValue(valueStr);
                    }
                }
                ContextManager.extract(contextCarrier);
            }
        }
//        traceUtils.showTrace("after-method-end");
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
        /*
         * The entry span is created in {@link #afterMethod}, but {@link #handleMethodException} is called before
         * {@link #afterMethod}, before the creation of entry span, we can not ensure there is an active span
         */
        if (ContextManager.isActive()) {
            ContextManager.activeSpan().errorOccurred().log(t);
        }
    }
}
