package org.apache.skywalking.apm.plugin.kafka.v2;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
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

public class KafkaConsumerInterceptor
        implements InstanceMethodsAroundInterceptor {
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String OPERATE_NAME_PREFIX = "Kafka/";
    private static final String CONSUMER_OPERATE_NAME = "/Consumer";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
//        ConsumerEnhanceRequiredInfo requiredInfo = (ConsumerEnhanceRequiredInfo)objInst.getSkyWalkingDynamicField();
//        requiredInfo.setStartTime(System.currentTimeMillis());
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret) throws Throwable {
        if (ret == null) {
            return ret;
        }
        ConsumerRecords<?, ?> records = (ConsumerRecords) ret;
        if (!records.isEmpty()) {
            for (ConsumerRecord<?, ?> record : records) {
                AbstractSpan activeSpan = ContextManager //
                        .createEntrySpan(OPERATE_NAME_PREFIX + record.topic() + CONSUMER_OPERATE_NAME, null).start(record.timestamp());
                activeSpan.setComponent(ComponentsDefine.KAFKA_CONSUMER);
                SpanLayer.asMQ(activeSpan);
//                Tags.MQ_BROKER.set(activeSpan, requiredInfo.getBrokerServers());
                Tags.MQ_TOPIC.set(activeSpan, record.topic());
                ContextCarrier contextCarrier = new ContextCarrier();
                CarrierItem next = contextCarrier.items();
                while (next.hasNext()) {
                    next = next.next();
                    Iterator<Header> iterator = record.headers().headers(next.getHeadKey()).iterator();
                    if (iterator.hasNext()) {
                        next.setHeadValue(new String(iterator.next().value()));
                    }
                }
                ContextManager.extract(contextCarrier);
            }
            ContextManager.stopSpan();
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().errorOccurred().log(t);
    }
}
