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

package org.apache.skywalking.apm.plugin.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;

/**
 * cache Callback and ContextSnapshot
 * 缓存住Callback和上下文的镜像
 */
public class CallbackCache {
    private Callback callback;
    private ContextSnapshot snapshot;

    /* get set toString methods */
    public Callback getCallback() {
        return callback;
    }
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    public ContextSnapshot getSnapshot() {
        return snapshot;
    }
    public void setSnapshot(ContextSnapshot snapshot) {
        this.snapshot = snapshot;
    }
    @Override
    public String toString() {
        return "CallbackCache{" + "callback=" + callback + ", snapshot=" + snapshot + '}';
    }
}