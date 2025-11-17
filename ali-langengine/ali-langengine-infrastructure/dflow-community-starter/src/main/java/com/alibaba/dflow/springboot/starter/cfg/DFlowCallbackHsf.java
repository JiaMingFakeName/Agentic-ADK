package com.alibaba.dflow.springboot.starter.cfg;

import com.alibaba.dflow.InitEntry;
import com.alibaba.dflow.internal.ContextStack;
import java.util.List;

public interface DFlowCallbackHsf extends InitEntry.RequestResender {
    boolean call(String callType, String data, String traceId) throws Exception;
    ContextStack getResult(String traceId) throws Exception;

    String getCount(String idname);
    ContextStack.ContextNode getStatus(String traceId, String debugName);

    List<String> omitTaskIDPattern(String regex);

    List<String> queryOmittedPattern();

    Boolean removePattern(String pattern);

    Boolean recall(String traceId,String stepName,boolean onlyThisStep);
}
