package org.apache.skywalking.apm.plugin.kafka.v2.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.NameMatch;

public class KafkaConsumerInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    public static final String INTERCEPTOR_CLASS = "org.apache.skywalking.apm.plugin.kafka.v2.KafkaConsumerInterceptor";
    public static final String ENHANCE_METHOD = "poll";
    public static final String ENHANCE_CLASS = "org.apache.kafka.clients.consumer.KafkaConsumer";

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
//        return new ConstructorInterceptPoint[] {
//                new ConstructorInterceptPoint() {
//                    @Override public ElementMatcher<MethodDescription> getConstructorMatcher() {
//                        return takesArgumentWithType(0, CONSTRUCTOR_INTERCEPT_TYPE);
//                    }
//
//                    @Override public String getConstructorInterceptor() {
//                        return CONSTRUCTOR_INTERCEPTOR_CLASS;
//                    }
//                }
//        };
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        InstanceMethodsInterceptPoint pollPoint = new InstanceMethodsInterceptPoint() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return ElementMatchers.isMethod().and(ElementMatchers.named(ENHANCE_METHOD));
            }

            @Override
            public String getMethodsInterceptor() {
                return INTERCEPTOR_CLASS;
            }

            @Override
            public boolean isOverrideArgs() {
                return false;
            }
        };
        InstanceMethodsInterceptPoint[] methodPoints = new InstanceMethodsInterceptPoint[]{pollPoint};
        return methodPoints;
    }

    @Override
    protected ClassMatch enhanceClass() {
        return NameMatch.byName(ENHANCE_CLASS);
    }
}
