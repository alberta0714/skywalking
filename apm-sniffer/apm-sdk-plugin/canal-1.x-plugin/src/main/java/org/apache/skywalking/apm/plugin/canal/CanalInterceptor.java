/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.canal;

import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import java.util.Objects;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
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
import java.net.InetSocketAddress;
import java.util.List;

public class CanalInterceptor implements InstanceMethodsAroundInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    TraceUtils traceUtils = new TraceUtils(logger, "canal-getWithoutAck");
    /* objInst(ClusterCanalConnector), method=getWithoutAck, allArguments=1000, int */
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        MethodInterceptResult result) throws Throwable {
    }
    /* clustercanalconector, getWithoutAck,  1000, int , */
    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
        Object ret) throws Throwable {
        if (ret == null) {
            return null;
        }
        if (ret instanceof Message == false) {
            return ret;
        }
        Message message = (Message) ret;
        long batchId = message.getId();
        int size = message.getEntries().size();
        if (batchId == -1 || size == 0) {
            return ret;
        }
        /* start 过滤非必要事件 */
        int num = 0;
        for (CanalEntry.Entry entry : message.getEntries()) {
//                        log.info("消息类型：{} - {}", batchId, entry.getEntryType());
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
                continue;
            }
//            printEntry(entry);
            num++;
        }
        if (num == 0) {
            return ret;
        }
        /* end */

        /* 生成链路消息 */
        CanalEnhanceInfo canalEnhanceInfo = (CanalEnhanceInfo) objInst.getSkyWalkingDynamicField();
        SimpleCanalConnector connector = (SimpleCanalConnector) objInst;

        String url = canalEnhanceInfo.getUrl();
        if (Objects.equals(url, "") || url == null) {
            InetSocketAddress address = (InetSocketAddress) connector.getNextAddress();
            String runningAddress = address.getAddress().toString() + ":" + address.getPort();
            runningAddress = runningAddress.replace('/', ' ');
            url = runningAddress;
            List<InetSocketAddress> socketAddressList = (List<InetSocketAddress>) ContextManager.getRuntimeContext()
                    .get("currentAddress");
            if (socketAddressList != null && socketAddressList.size() > 0) {
                for (InetSocketAddress socketAddress : socketAddressList) {
                    String currentAddress = socketAddress.getAddress().toString() + ":" + socketAddress.getPort();
                    currentAddress = currentAddress.replace('/', ' ');
                    if (!currentAddress.equals(runningAddress)) {
                        url = url + "," + currentAddress;
                    }
                }
            }
        }
        String batchSize = allArguments[0].toString();
        String destination = canalEnhanceInfo.getDestination();

        ContextCarrier contextCarrier = new ContextCarrier();
        AbstractSpan activeSpan = ContextManager.createExitSpan("Canal/" + destination, contextCarrier, url)
                .start(System.currentTimeMillis());
        SpanLayer.asDB(activeSpan);
        activeSpan.setComponent(ComponentsDefine.CANAL);
        activeSpan.tag(Tags.ofKey("batchSize"), batchSize);
        activeSpan.tag(Tags.ofKey("destination"), destination);

        traceUtils.showTrace("后A");
        ContextManager.stopSpan();
        traceUtils.showTrace("后B");
        return ret;

    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
        Class<?>[] argumentsTypes, Throwable t) {
        ContextManager.activeSpan().errorOccurred().log(t);
    }

    private void printEntry(CanalEntry.Entry entry) {
        if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
                || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
            return;
        }

        CanalEntry.RowChange rowChage = null;
        try {
            rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        } catch (Exception e) {
            throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
        }

        CanalEntry.EventType eventType = rowChage.getEventType();
        System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                entry.getHeader().getSchemaName(), entry.getHeader().getTableName(), eventType));

        for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
            if (eventType == CanalEntry.EventType.DELETE) {
                printColumn(rowData.getBeforeColumnsList());
            } else if (eventType == CanalEntry.EventType.INSERT) {
                printColumn(rowData.getAfterColumnsList());
            } else if (eventType == CanalEntry.EventType.UPDATE) {
                printColumn(rowData.getAfterColumnsList());
            } else {
                System.out.println("-------&gt; before");
                printColumn(rowData.getBeforeColumnsList());
                System.out.println("-------&gt; after");
                printColumn(rowData.getAfterColumnsList());
            }
        }
    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}