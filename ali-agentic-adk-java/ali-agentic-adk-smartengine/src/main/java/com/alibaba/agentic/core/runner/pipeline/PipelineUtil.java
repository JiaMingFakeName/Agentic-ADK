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
package com.alibaba.agentic.core.runner.pipeline;

import com.alibaba.agentic.core.executor.Result;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 管道工具类。
 * <p>
 * 提供静态方法访问管道执行能力，简化外部调用。
 * 通过 Spring 容器初始化时注入具体的管道实现。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/10 17:10
 */
@Component
public class PipelineUtil {

    /**
     * 静态管道实例，在容器初始化后设置。
     */
    private static AbstractPipeline pipeline;

    /**
     * 通过依赖注入获取的管道实现。
     */
    @Autowired
    private AbstractPipeline abstractPipeline;

    /**
     * 执行管道处理的静态入口。
     *
     * @param request 管道请求
     * @return 执行结果流
     */
    public static Flowable<Result> doPipe(PipelineRequest request) {
        return PipelineUtil.pipeline.doPipes(request);
    }

    /**
     * 初始化方法，设置静态管道实例。
     */
    @PostConstruct
    public void init() {
        PipelineUtil.pipeline = abstractPipeline;
    }

}
