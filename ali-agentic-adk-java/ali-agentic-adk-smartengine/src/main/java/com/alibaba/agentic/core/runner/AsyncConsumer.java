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
package com.alibaba.agentic.core.runner;

import com.alibaba.agentic.core.executor.Result;

import java.util.function.Consumer;

/**
 * 异步结果消费器。
 * <p>
 * 继承 {@link Consumer}，专门用于处理异步执行产生的 {@link Result}。
 * 框架在异步任务完成后，会调用该接口的实现来处理结果。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/28 16:21
 */
public interface AsyncConsumer extends Consumer<Result> {

}
