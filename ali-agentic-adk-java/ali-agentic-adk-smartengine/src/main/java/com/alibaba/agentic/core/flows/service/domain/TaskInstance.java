/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.flows.service.domain;

import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.*;
import java.util.Base64;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/25 08:58
 */
@Data
@Accessors(chain = true)
public class TaskInstance implements Serializable {

    private static final long serialVersionUID = 2367895097260284023L;

    private String id;

    private ProcessInstance processInstance;

    private SystemContext systemContext;

    private Request request;

    private String activityId;

    public static void main(String[] args) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setRequest(new Request().setInvokeMode(InvokeMode.SYNC));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        String taskStr;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(taskInstance);
            byte[] data = bos.toByteArray();
            taskStr = Base64.getEncoder().encodeToString(data);
            System.out.println("taskStr" + taskStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode((taskStr)));
            ObjectInputStream in = new ObjectInputStream(bis);
            taskInstance =  (TaskInstance) in.readObject();
            System.out.println("taskInstance" + taskInstance);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
