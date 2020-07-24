package org.apache.skywalking.apm.plugin.kafka.v2;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
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

public class KafkaProducerInterceptor
        implements InstanceMethodsAroundInterceptor {
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String OPERATION_PREFIX = "Kafka/";
    private static final String OPERATION_SUFFIX = "/Producer";

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        if (allArguments[0] == null) {
            return;
        }
        ContextCarrier contextCarrier = new ContextCarrier();

        ProducerRecord record = (ProducerRecord) allArguments[0];
        String topicName = record.topic();
        AbstractSpan activeSpan = ContextManager //
                .createExitSpan(OPERATION_PREFIX + topicName + OPERATION_SUFFIX, contextCarrier
//                        , (String) objInst.getSkyWalkingDynamicField()); // add
                        , "");
        Tags.MQ_BROKER.set(activeSpan, (String) objInst.getSkyWalkingDynamicField());// add
        Tags.MQ_TOPIC.set(activeSpan, topicName);
        SpanLayer.asMQ(activeSpan);
        activeSpan.setComponent(ComponentsDefine.KAFKA_PRODUCER);
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            record.headers().add(next.getHeadKey(), next.getHeadValue().getBytes());
        }

        /* added! */
//        Object shouldCallbackInstance = allArguments[1];
//        if (null != shouldCallbackInstance) {
//            if (shouldCallbackInstance instanceof EnhancedInstance) {
//                EnhancedInstance callbackInstance = (EnhancedInstance) shouldCallbackInstance;
//                ContextSnapshot snapshot = ContextManager.capture();
//                if (null != snapshot) {
//                    CallbackCache cache = new CallbackCache();
//                    cache.setSnapshot(snapshot);
//                    callbackInstance.setSkyWalkingDynamicField(cache);
//                }
//            } else if (shouldCallbackInstance instanceof Callback) {
//                Callback callback = (Callback) shouldCallbackInstance;
//                ContextSnapshot snapshot = ContextManager.capture();
//                if (null != snapshot) {
//                    CallbackCache cache = new CallbackCache();
//                    cache.setSnapshot(snapshot);
//                    cache.setCallback(callback);
//                    allArguments[1] = new CallbackAdapterInterceptor(cache);
//                }
//            }
//        }
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
