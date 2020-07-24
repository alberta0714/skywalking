package org.apache.skywalking.apm.plugin.kafka;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * transformation kafkaTemplate.buildCallback
 */
public class KafkaTemplateCallbackInterceptor implements InstanceMethodsAroundInterceptor {
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    TraceUtils traceUtils = new TraceUtils(logger, "spring-kafka-producer-callback");

    /**
     * org.springframework.kafka.core.KafkaTemplate #
     *  private Callback buildCallback(final ProducerRecord<K, V> producerRecord, final Producer<K, V> producer,
     *         final SettableListenableFuture<SendResult<K, V>> future) {
     * @param objInst
     * @param method
     * @param allArguments
     * @param argumentsTypes
     * @param result change this result, if you want to truncate the method.
     * @throws Throwable
     */
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        MethodInterceptResult result) throws Throwable {
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        Object ret) throws Throwable {
        CallbackAdapter back = new CallbackAdapter((org.apache.kafka.clients.producer.Callback) ret, objInst);
        return back;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
        Class<?>[] argumentsTypes, Throwable t) {
    }
}