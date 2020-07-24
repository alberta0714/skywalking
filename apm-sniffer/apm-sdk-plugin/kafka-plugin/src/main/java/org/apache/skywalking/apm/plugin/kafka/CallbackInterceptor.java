package org.apache.skywalking.apm.plugin.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 *
 **/
public class CallbackInterceptor implements InstanceMethodsAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    TraceUtils traceUtils = new TraceUtils(logger, "kafka-producer-callback");

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
        CallbackCache cache = (CallbackCache) objInst.getSkyWalkingDynamicField();
        if (null != cache) {
            ContextSnapshot snapshot = getSnapshot(cache);
            RecordMetadata metadata = (RecordMetadata) allArguments[0];
            String topic = "-";
            if (metadata != null && metadata.topic() != null) {
                topic = metadata.topic();
            }
            AbstractSpan activeSpan = ContextManager.createLocalSpan("Kafka/ProducerCb/" + topic);
            activeSpan.setComponent(ComponentsDefine.KAFKA_PRODUCER);
            if (metadata != null) {
                // Null if an error occurred during processing of this record
                Tags.MQ_TOPIC.set(activeSpan, metadata.topic());
            }
            ContextManager.continued(snapshot);
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret) throws Throwable {
        CallbackCache cache = (CallbackCache) objInst.getSkyWalkingDynamicField();
        if (null != cache) {
            ContextSnapshot snapshot = getSnapshot(cache);
            if (null != snapshot) {
                Exception exceptions = (Exception) allArguments[1];
                if (exceptions != null) {
                    ContextManager.activeSpan().errorOccurred().log(exceptions);
                }
                ContextManager.stopSpan();
            }
        }
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().errorOccurred().log(t);
    }

    private ContextSnapshot getSnapshot(CallbackCache cache) {
        ContextSnapshot snapshot = cache.getSnapshot();
        if (snapshot == null) {
            snapshot = ((CallbackCache) ((EnhancedInstance) cache.getCallback()).getSkyWalkingDynamicField()).getSnapshot();
        }
        return snapshot;
    }
}
