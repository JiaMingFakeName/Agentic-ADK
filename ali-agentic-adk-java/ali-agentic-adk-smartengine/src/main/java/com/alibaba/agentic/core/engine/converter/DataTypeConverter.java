package com.alibaba.agentic.core.engine.converter;

import org.apache.commons.collections.MapUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataTypeConverter {

    public static <T> List<T> convertMap2ListWithIndexKeyOrder(Map<String, T> inputMap) {
        if (MapUtils.isEmpty(inputMap)) {
            return Collections.emptyList();
        }
        return inputMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(Integer::parseInt)))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
