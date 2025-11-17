package com.alibaba.agentic.core.converter;

import com.alibaba.agentic.core.Application;
import com.alibaba.agentic.core.engine.converter.DataTypeConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class })
@ActiveProfiles("testing")
public class ConverterTest {

    @Test
    public void convertMap2ListWithIndexKeyOrderTest() {
        Map<String, Map<String, Object>> inputMap = new HashMap<>();
        inputMap.put("3", Collections.singletonMap("c", 3));
        inputMap.put("100", Collections.singletonMap("a", 1));
        inputMap.put("20", Collections.singletonMap("b", 2));
        inputMap.put("0", Collections.singletonMap("f", 6));
        inputMap.put("1", Collections.singletonMap("e", 5));
        inputMap.put("2", Collections.singletonMap("d", 4));

        System.out.println(DataTypeConverter.convertMap2ListWithIndexKeyOrder(inputMap));
    }
}
