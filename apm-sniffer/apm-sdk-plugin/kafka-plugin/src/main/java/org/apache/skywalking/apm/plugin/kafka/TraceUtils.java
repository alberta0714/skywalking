package org.apache.skywalking.apm.plugin.kafka;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.slf4j.Logger;

/***
 * *   功能描述：	请在此处填写文件的功能
 * *   Author:Sun Zhanchao  Date:2020-07-23
 ***/
public class TraceUtils {
    Logger logger;
    String name;

    public TraceUtils(Logger logger, String name){
        this.logger = logger;
        this.name = name;
    }

    public void showTrace(String title) {
        if (title == null) {
            title = "";
        }
        long threadId = Thread.currentThread().getId();
        if (ContextManager.getGlobalTraceId().equalsIgnoreCase("N/A")) {
            logger.info("{}【{}】 global:{} t:{}", name, title, ContextManager.getGlobalTraceId(), threadId);
            return;
        }
        ContextSnapshot snapshot = ContextManager.capture();
        logger.info("{}【{}】 seg:{} dis:{} t:{}", name, title, snapshot.getTraceSegmentId(), snapshot.getDistributedTraceId(), threadId);
    }
}
