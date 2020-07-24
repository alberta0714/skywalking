package org.apache.skywalking.apm.plugin.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
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

public class SpringKafkaInterceptor implements InstanceMethodsAroundInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    TraceUtils traceUtils = new TraceUtils(logger, "spring-kafka-consumer");

    /**
     * GenericMessageListener.onMessage(T data, Acknowledgment acknowledgment);
     *
     * @param objInst        GenericMessageListener BatchMessagingMessageListenerAdapter
     * @param method         onMessage(T data, Acknowledgment acknowledgment); T → List<ConsumerRecord> records
     * @param allArguments   T data, Acknowledgment acknowledgment
     * @param argumentsTypes T, Acknowledgment
     */
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        if (allArguments[0] == null) {
            return;
        }
        List<ConsumerRecord> records = (List<ConsumerRecord>) allArguments[0];
        if (records.size() <= 0) {
            return;
        }

        AbstractSpan a = ContextManager.createEntrySpan("SpringKafka/Consumer", null);
        a.setComponent(ComponentsDefine.KAFKA_CONSUMER);
        SpanLayer.asMQ(a);
//        traceUtils.showTrace("activeSpan");

        for (ConsumerRecord record : records) {
            ContextCarrier parent = new ContextCarrier();
//            AbstractSpan a = ContextManager.createEntrySpan("SpringKafka/" + record.topic() + "/Consumer", parent);
//            a.setComponent(ComponentsDefine.KAFKA_CONSUMER);
//            SpanLayer.asMQ(a);

            a.setOperationName("SpringKafka/" + record.topic() + "/Consumer");
            a.start(record.timestamp());
            Tags.MQ_TOPIC.set(a, record.topic());

            CarrierItem next = parent.items();
            while (next.hasNext()) {
                next = next.next();
                Iterator<Header> iterator = record.headers().headers(next.getHeadKey()).iterator();
                if (iterator.hasNext()) {
                    next.setHeadValue(new String(iterator.next().value()));
                }
            }
            ContextManager.extract(parent);
        }
//        traceUtils.showTrace("extract stop");
//        ContextManager.stopSpan();
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
//        traceUtils.showTrace("after method - before stop");
//        if (ret == null) {
//            return ret;
//        }
        ContextManager.stopSpan();
//        traceUtils.showTrace("after method");
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        logger.error("", t);
    }
}
