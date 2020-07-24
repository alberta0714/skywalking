package org.apache.skywalking.apm.plugin.kafka.v2;

import org.apache.kafka.clients.consumer.ConsumerRecord;
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

import java.lang.reflect.Method;
import java.util.Iterator;

public class SpringKafkaInterceptor
        implements InstanceMethodsAroundInterceptor {
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * GenericMessageListener.onMessage(T data, Acknowledgment acknowledgment);
     *
     * @param objInst        GenericMessageListener
     * @param method         onMessage(T data, Acknowledgment acknowledgment); T → List<ConsumerRecord> records
     * @param allArguments   T data, Acknowledgment acknowledgment
     * @param argumentsTypes T, Acknowledgment
     */
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
//        if ((allArguments[0] != null) && ((allArguments[0] instanceof ConsumerRecord))) {
        if (allArguments[0] != null) {
            ConsumerRecord<?, ?> record = (ConsumerRecord) allArguments[0];
            AbstractSpan activeSpan = ContextManager.createEntrySpan("Spring/" + record.topic() + "/Kafka", null).start(record.timestamp());
            activeSpan.setComponent(ComponentsDefine.KAFKA_CONSUMER);
            SpanLayer.asMQ(activeSpan);
            Tags.MQ_TOPIC.set(activeSpan, record.topic());
            ContextCarrier contextCarrier = new ContextCarrier();
            CarrierItem next = contextCarrier.items();
            while (next.hasNext()) {
                next = next.next();
                Iterator<Header> iterator = record.headers().headers(next.getHeadKey()).iterator();
                if (iterator.hasNext()) {
                    next.setHeadValue(new String(((Header) iterator.next()).value()));
                }
            }
            ContextManager.extract(contextCarrier);
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
    }
}
