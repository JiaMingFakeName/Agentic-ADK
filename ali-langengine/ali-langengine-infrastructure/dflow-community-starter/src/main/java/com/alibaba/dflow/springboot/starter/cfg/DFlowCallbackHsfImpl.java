package com.alibaba.dflow.springboot.starter.cfg;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.InitEntry.RequestResender;
import com.alibaba.dflow.UserException;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.ContextStack.ContextNode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DFlowCallbackHsfImpl implements RequestResender, DFlowCallbackHsf {
    Logger logger = LoggerFactory.getLogger(DFlowCallbackHsfImpl.class);
    private Callback callback;

    @Override
    public boolean call(String callType, String data, String traceId) throws Exception {
        try{
            return callback.realCall(callType, data,traceId);
        }catch (Exception e){
            logger.error("DFlow transferCall failed@"+traceId,e);
            return false;
        }
    }

    @Override
    public List<String> omitTaskIDPattern(String regex){
        return (DFlow.addOmitTaskPattern(regex));
    }

    @Override
    public List<String> queryOmittedPattern(){
        return (DFlow.queryPattern());
    }

    @Override
    public Boolean removePattern(String pattern){
        DFlow.clearOmitPattern(pattern);
        return (true);
    }

    @Override
    public Boolean recall(String traceId, String stepName, boolean onlyThisStep) {
        try {
            DFlow.replayStep(traceId,stepName,onlyThisStep);
        } catch (UserException e) {
                    logger.error("Failed to replay step for traceId: {} stepName: {}", traceId, stepName, e);
                }
        return true;
    }

    @Override
    public ContextStack getResult(String traceId) throws Exception {
        return (DFlow.getStoreage().getContext(traceId));
    }

    @Override
    public String getCount(String idname) {
        return (DFlow.getCount(idname));
    }

    @Override
    public ContextNode getStatus(String traceId, String debugName) {
        return (DFlow.checkStatus(traceId,debugName));
    }


    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }


    @Override
    public boolean resendCall(String ip, String callType, String params, String id) {
        throw new RuntimeException("Using httpresender instead");
    }

}
