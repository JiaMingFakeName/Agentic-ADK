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
package com.alibaba.agentic.core.runner.pipeline.impl;

import com.alibaba.agentic.core.runner.pipe.PipeInterface;
import com.alibaba.agentic.core.runner.pipeline.AbstractPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 管道实现类。
 * <p>
 * 继承 {@link AbstractPipeline}，负责收集所有 {@link PipeInterface} 实现并组装成管道链。
 * 在 Spring 容器初始化后，自动注入所有管道实现并添加到执行链中。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/10 20:37
 */
@Component
public class PipelineImpl extends AbstractPipeline {

    /**
     * 所有管道接口实现的列表，由 Spring 自动注入。
     */
    @Autowired
    private List<PipeInterface> agentExecutePipeList;

    /**
     * 初始化方法，在 Bean 创建后自动执行。
     * <p>
     * 将所有注入的管道实现添加到父类的管道链中。
     * </p>
     */
    @PostConstruct
    public void init() {
        addPipe(agentExecutePipeList);
    }


}
