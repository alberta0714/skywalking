package org.apache.skywalking.apm.plugin.kafka;

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

public class KafkaProducerInterceptor implements InstanceMethodsAroundInterceptor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    TraceUtils traceUtils = new TraceUtils(logger, "kafka-producer");

    public static final String OPERATE_NAME_PREFIX = "Kafka/";
    public static final String PRODUCER_OPERATE_NAME_SUFFIX = "/Producer";


    /**
     * 通常为跨服务生产、消费，因此咱们通过header传递链路信息
     *
     * @param objInst        org.apache.kafka.clients.producer.KafkaProducer
     * @param method         private Future<RecordMetadata> doSend(ProducerRecord<K, V> record, Callback callback) {
     * @param allArguments   ProducerRecord<K, V> record, Callback callback
     * @param argumentsTypes ProducerRecord<K, V> record, Callback callback
     * @param result         change this result, if you want to truncate the method.
     * @throws Throwable
     */
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
        // 初始化一个起点
        ContextCarrier contextCarrier = new ContextCarrier();
        ProducerRecord record = (ProducerRecord) allArguments[0];
        String topicName = record.topic();

        //  上下文创建一个出口，并注入traceID
        AbstractSpan activeSpan = ContextManager.createExitSpan(OPERATE_NAME_PREFIX + topicName + PRODUCER_OPERATE_NAME_SUFFIX, contextCarrier, (String) objInst
                .getSkyWalkingDynamicField());

        Tags.MQ_BROKER.set(activeSpan, (String) objInst.getSkyWalkingDynamicField());
        Tags.MQ_TOPIC.set(activeSpan, topicName);
        SpanLayer.asMQ(activeSpan);
        activeSpan.setComponent(ComponentsDefine.KAFKA_PRODUCER);

        // 把当前上下文写入每个记录的header头信息中
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            String key = next.getHeadKey();
            String value = next.getHeadValue();
            record.headers().add(key, value.getBytes());
        }

        // when use lambda expression, not to generate inner class,
        //    and not to trigger kafka CallBack class define, so allArguments[1] can't to cast EnhancedInstance
        // 当使用lambda表达式时， 无法生成内部类并且不能触发callback类定义。因此第一个参数无法成为加强实例。
        /* 当服务结束时，将上下文存储到callback一份 */
        Object shouldCallbackInstance = allArguments[1];
        if (null != shouldCallbackInstance) {
            if (shouldCallbackInstance instanceof EnhancedInstance) {
                EnhancedInstance callbackInstance = (EnhancedInstance) shouldCallbackInstance;
                ContextSnapshot snapshot = ContextManager.capture();
                if (null != snapshot) {
                    CallbackCache cache = new CallbackCache();
                    cache.setSnapshot(snapshot);
                    callbackInstance.setSkyWalkingDynamicField(cache);
                }
//                traceUtils.showTrace("enhance instance");
            } else if (shouldCallbackInstance instanceof Callback) {
                Callback callback = (Callback) shouldCallbackInstance;
                ContextSnapshot snapshot = ContextManager.capture();
                if (null != snapshot) {
                    CallbackCache cache = new CallbackCache();
                    cache.setSnapshot(snapshot);
                    cache.setCallback(callback);
                    allArguments[1] = new CallbackAdapterInterceptor(cache);
                }
//                traceUtils.showTrace("call back");
            }
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret) throws Throwable {
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
    }
}
