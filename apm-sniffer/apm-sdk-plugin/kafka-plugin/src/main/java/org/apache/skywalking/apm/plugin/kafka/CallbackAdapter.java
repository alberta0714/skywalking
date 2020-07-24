package org.apache.skywalking.apm.plugin.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;

/**
 * implements Callback and EnhancedInstance, for transformation kafkaTemplate.buildCallback
 */
public class CallbackAdapter implements Callback, EnhancedInstance {

    private Object instance;

    private Callback userCallback;

    public CallbackAdapter(Callback userCallback, Object instance) {
        this.userCallback = userCallback;
        this.instance = instance;
    }

    public CallbackAdapter() {
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (userCallback != null) {
            userCallback.onCompletion(metadata, exception);
        }
    }

    @Override
    public Object getSkyWalkingDynamicField() {
        return instance;
    }

    @Override
    public void setSkyWalkingDynamicField(Object value) {
        this.instance = value;
    }

    public Callback getUserCallback() {
        return userCallback;
    }
}