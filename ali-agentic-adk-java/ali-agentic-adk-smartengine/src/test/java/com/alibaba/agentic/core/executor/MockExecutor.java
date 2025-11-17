package com.alibaba.agentic.core.executor;

import com.alibaba.agentic.core.engine.delegation.FrameworkDelegationBase;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.stereotype.Component;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/22 15:35
 */
@Component
public class MockExecutor extends FrameworkDelegationBase {


    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        return Flowable.just(Result.success(request.getParam()));
    }


}
