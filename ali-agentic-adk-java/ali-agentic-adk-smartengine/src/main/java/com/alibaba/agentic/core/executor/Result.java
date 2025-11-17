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
package com.alibaba.agentic.core.executor;

import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

/**
 * 执行结果。
 * <p>
 * 统一封装执行是否成功、错误码与错误信息，以及可选的数据负载。
 * 提供便捷的成功/失败工厂方法。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/4 17:52
 */
@Slf4j
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Result {

    /**
     * 结果数据负载。
     */
    Map<String, Object> data;
    /**
     * 是否成功。
     */
    private boolean success;
    /**
     * 错误码或状态码。
     */
    private String code;
    /**
     * 错误信息或异常堆栈。
     */
    private String errorMsg;

    public Result(boolean success, String code, String errorMsg, Map<String, Object> data) {
        this.success = success;
        this.code = code;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    /**
     * 构造成功结果。
     *
     * @param data 数据负载
     * @return 成功结果
     */
    public static Result success(Map<String, Object> data) {
        return new Result(true, "200", null, data);
    }


    /**
     * 将异常封装为失败结果。
     *
     * @param throwable 异常
     * @return 失败结果
     */
    public static Result fail(Throwable throwable) {
        log.error("Result error, error is: ", throwable);
        if (throwable instanceof BaseException) {
            BaseException exception = (BaseException) throwable;
            return new Result(false, exception.getErrorEnum().getCode(), exception.getMessage(), null);
        } else {
            return new Result(false, ErrorEnum.SYSTEM_ERROR.getCode(), ExceptionUtils.getStackTrace(throwable), null);
        }
    }

}
