//package org.apache.skywalking.apm.plugin.kafka.v2.define;
//
//import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
//import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
//import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
//import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
//import org.apache.skywalking.apm.agent.core.plugin.match.HierarchyMatch;
//import net.bytebuddy.description.method.MethodDescription;
//import net.bytebuddy.matcher.ElementMatcher;
//import net.bytebuddy.matcher.ElementMatchers;
//
//public class SpringKafkaInstrumentation
//        extends ClassInstanceMethodsEnhancePluginDefine {
//    public static final String INTERCEPTOR_CLASS = "org.apache.skywalking.apm.plugin.kafka.v2.SpringKafkaInterceptor";
//    public static final String ENHANCE_METHOD = "onMessage";
////    public static final String ENHANCE_CLASS = "org.springframework.kafka.listener.MessageListener";
//    public static final String ENHANCE_CLASS = "org.springframework.kafka.listener.GenericMessageListener";
//    // org.springframework.kafka.listener.adapter.BatchMessagingMessageListenerAdapter
//    /*
//     * org.springframework.kafka.listener.MessageListener
//     * org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter
//     * org.springframework.kafka.config.MethodKafkaListenerEndpoint
//     *
//     */
//
//    @Override
//    protected ClassMatch enhanceClass() {
//        return HierarchyMatch.byHierarchyMatch(new String[]{ENHANCE_CLASS});
//    }
//
//    @Override
//    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
//        return new ConstructorInterceptPoint[0];
//    }
//
//    @Override
//    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
//        InstanceMethodsInterceptPoint onMessagePoint = new InstanceMethodsInterceptPoint() {
//            @Override
//            public ElementMatcher<MethodDescription> getMethodsMatcher() {
//                return ElementMatchers.isMethod().and(ElementMatchers.named(ENHANCE_METHOD));
//            }
//
//            @Override
//            public String getMethodsInterceptor() {
//                return INTERCEPTOR_CLASS;
//            }
//
//            @Override
//            public boolean isOverrideArgs() {
//                return false;
//            }
//        };
//        InstanceMethodsInterceptPoint[] methodPoints = new InstanceMethodsInterceptPoint[]{onMessagePoint};
//        return methodPoints;
//    }
//}
