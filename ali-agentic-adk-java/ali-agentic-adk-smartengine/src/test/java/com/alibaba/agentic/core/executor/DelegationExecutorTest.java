package com.alibaba.agentic.core.executor;

import com.alibaba.agentic.core.Application;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/8 20:25
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class DelegationExecutorTest {

    @Resource
    private MockExecutor mockExecutor;

    @Test
    public void testChain() {
        Request request = new Request();
        request.setInvokeMode(InvokeMode.SYNC);
        request.setParam(Map.of("a", "testChain"));
        Flowable<Result> result = DelegationExecutor.invoke(new SystemContext().setExecutor(mockExecutor), request);
        Assert.assertEquals("testChain", result.blockingFirst());
    }

}
