package org.apache.skywalking.apm.plugin.kafka.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.HierarchyMatch;

public class SpringKafkaInstrumentation
        extends ClassInstanceMethodsEnhancePluginDefine {
    public static final String INTERCEPTOR_CLASS = "org.apache.skywalking.apm.plugin.kafka.SpringKafkaInterceptor";

    public static final String ENHANCE_METHOD = "onMessage";
    public static final String ENHANCE_CLASS = "org.springframework.kafka.listener.GenericMessageListener";
//    public static final String ENHANCE_CLASS = "org.springframework.kafka.listener.MessageListener"; // 不通用，因此换为下面这个类

//    public static final String ENHANCE_METHOD = "listen";
//    public static final String ENHANCE_CLASS = "com.noriental.module.es.listener.UserListener"; // 哎! 直接未连接到!

    @Override
    protected ClassMatch enhanceClass() {
        return HierarchyMatch.byHierarchyMatch(new String[]{ENHANCE_CLASS});
    }

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        InstanceMethodsInterceptPoint onMessagePoint = new InstanceMethodsInterceptPoint() {
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
        InstanceMethodsInterceptPoint[] methodPoints = new InstanceMethodsInterceptPoint[]{onMessagePoint};
        return methodPoints;
    }
}
