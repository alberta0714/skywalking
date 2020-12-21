package org.apache.skywalking.apm.plugin.customize.util;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;

/***
 * *   功能描述：	请在此处填写文件的功能
 * *   Author:Sun Zhanchao  Date:2020-07-23
 ***/
public class TraceUtils {
    ILog logger;
    String name;

    public TraceUtils(ILog logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    public void showTrace(String title) {
        if (title == null) {
            title = "";
        }
        if (ContextManager.getGlobalTraceId().equalsIgnoreCase("N/A")) {
            logger.info("→→→→trace:【{}】seg:【{}】 {}-{} ", "N/A", "N/A", name, title);
            return;
        }
        ContextSnapshot snapshot = ContextManager.capture();
        logger.info("→→→→trace:【{}】seg:【{}】 {}-{} ", snapshot.getTraceId().getId(), snapshot.getTraceSegmentId(), name, title);
    }
}
