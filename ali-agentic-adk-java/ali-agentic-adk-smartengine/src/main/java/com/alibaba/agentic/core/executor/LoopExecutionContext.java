package com.alibaba.agentic.core.executor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class LoopExecutionContext implements Serializable {

    private static final long serialVersionUID = -2551783059766599420L;
    /**
     * 当前的loopFlowNode的节点id
     */
    private String loopId;

    /**
     * 当前执行的循环轮次，从0开始
     */
    private int index = 0;

    /**
     * 循环最大次数
     */
    private int loopMaxCount;

    /**
     * 循环元素列表
     */
    private List<?> loopItems;

    /**
     * 当前执行轮次的循环元素
     */
    private Object item;

    /**
     * 当前循环内各轮次循环各节点产出的结果数据
     * index -> {activityId -> result}
     */
    private Map<Integer, Map<String, Map<String, Object>>> loopResult = new ConcurrentHashMap<>();

    /**
     * 循环是否结束，用于区别嵌套循环情况下，判断是否需要重新对数据进行初始化
     */
    private boolean loopFinished;
}
