package com.alibaba.agentic.agent.dflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.DFlow.Param;
import com.alibaba.dflow.InitEntry.Entry;
import com.alibaba.dflow.PersistentException;
import com.alibaba.dflow.UserException;
import com.alibaba.dflow.config.ContextStoreInterface;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.reactivex.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DFlowTest {
    public static void main(String args[])
            throws Exception {
        try {
            DFlow.globalInitForTest();

            DFlow.setStoreage(new ContextStoreInterface() {

                ConcurrentHashMap<String, String> m = new ConcurrentHashMap<>();

                @Override
                public ContextStack getContext(String s) {
                    String result = m.get(s);
                    if (result == null) {
                        return null;
                    }
                    return ContextStack.rebuild(result);
                }

                @Override
                public void putContext(String s, ContextStack contextStack) throws PersistentException {
                    m.put(s, contextStack.dump());
                }

                @Override
                public void expireContext(String traceId) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                            }
                            m.remove(traceId);
                        }
                    }).start();
                }

                @Override
                public void removeContext(String s) {
                    m.remove(s);
                }
            });



            System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
            //DFlow.setStrictMode(false);
            Entry entry = DFlow.fromCall("startTask").id("开始发布")
                    .flatMap((c, p) -> {
                        System.out.print("准备处理前置节点\n");
                        return mapToSeqBatch(p);
                        //return p;
                    }).id("seq")
                    .flatMap(x -> {
                                Collection<DFlow<String>> flows = new ArrayList<DFlow<String>>();
                                Collection<Consumer<ContextStack>> triggers = new ArrayList<>();
                                for (int i = 0; i < 10; i++) {
                                    flows.add(mapToParHost(i));
                                    //trigger used to trigger out side system
                                    triggers.add(null);
                                }

                                return DFlow.zip(flows, triggers,
                                        (c, results) -> {
                                            //合并逻辑在此;
                                            String[] re = results;
                                            for (int i = 0; i < re.length; i++) {
                                                if ("failed".equals(results[i])) {
                                                    return "failed";
                                                }
                                            }
                                            return "success";
                                        },

                                        new TypeReference<String>() {}).id("a");
                            }
                    ).id("处理并发流")
                    .map((c, p) -> {
                        System.out.print("处理后置节点");
                        return "";
                    }).id("last")
                    .init().getEntry("startTask");

            //运行时触发
            DFlow.call(entry, "projectId", "requestId111111");

            Thread.sleep(1000);

            System.out.println(JSON.toJSONString(DFlow.getStoreage().getContext("ttt")));
            //DFlow.replayStep("ttt","2-2->last-last->null",true);
            ;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String mergeFunc(ContextStack contextStack, String[] strings) {
        return strings[0];
    }

    private static DFlow mapToParHost(int i) throws DFlowConstructionException {
        return DFlow.just(i).id("just"+i)
                .map(p->{
                    System.out.println("处理第"+p+"个节点");
                    return "";
                }).id("childprocess");
    }

    private static DFlow<String> mapToSeqBatch(String p) throws DFlowConstructionException {
        return DFlow.just(p)
                .map(para->"").id("dd")
                .onErrorReturn(c->{
                    //在此处理单任务失败
                    throw new Exception("Failed finnally");
                });
    }
}


