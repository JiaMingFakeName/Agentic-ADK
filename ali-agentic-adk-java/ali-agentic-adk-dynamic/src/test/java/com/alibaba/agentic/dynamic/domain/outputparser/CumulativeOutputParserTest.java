package com.alibaba.agentic.dynamic.domain.outputparser;

import com.google.adk.models.LlmResponse;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class CumulativeOutputParserTest {

    @After
    public void tearDown() {
    }

    @Test
    public void testBasicAccumulation() {
        CumulativeOutputParser parser = new CumulativeOutputParser();
        
        LlmResponse partial1 = LlmResponse.builder().partial(true).build();
        LlmResponse partial2 = LlmResponse.builder().partial(true).build();
        LlmResponse partial3 = LlmResponse.builder().partial(true).build();
        LlmResponse complete = LlmResponse.builder().partial(false).build();
        
        String result1 = parser.parse(partial1, "1");
        assertEquals("1", result1);
        
        String result2 = parser.parse(partial2, "2");
        assertEquals("12", result2);
        
        String result3 = parser.parse(partial3, "3");
        assertEquals("123", result3);
        
        String result4 = parser.parse(complete, "4");
        assertEquals("1234", result4);
    }

    @Test
    public void testAutoResetOnComplete() {
        CumulativeOutputParser parser = new CumulativeOutputParser();
        
        LlmResponse partial = LlmResponse.builder().partial(true).build();
        LlmResponse complete = LlmResponse.builder().partial(false).build();
        
        parser.parse(partial, "first");
        parser.parse(partial, "second");
        String result1 = parser.parse(complete, "third");
        assertEquals("firstsecondthird", result1);
        
        String result2 = parser.parse(partial, "new");
        assertEquals("new", result2);
        
        String result3 = parser.parse(complete, "stream");
        assertEquals("newstream", result3);
    }

    @Test
    public void testMultipleStreams() {
        CumulativeOutputParser parser = new CumulativeOutputParser();
        
        LlmResponse partial = LlmResponse.builder().partial(true).build();
        LlmResponse complete = LlmResponse.builder().partial(false).build();
        
        parser.parse(partial, "A");
        parser.parse(partial, "B");
        String result1 = parser.parse(complete, "C");
        assertEquals("ABC", result1);
        
        parser.parse(partial, "X");
        String result2 = parser.parse(complete, "Y");
        assertEquals("XY", result2);
        
        String result3 = parser.parse(complete, "Z");
        assertEquals("Z", result3);
    }

    @Test
    public void testEmptyStringAccumulation() {
        CumulativeOutputParser parser = new CumulativeOutputParser();
        
        LlmResponse partial = LlmResponse.builder().partial(true).build();
        LlmResponse complete = LlmResponse.builder().partial(false).build();
        
        String result1 = parser.parse(partial, "");
        assertEquals("", result1);
        
        String result2 = parser.parse(partial, "text");
        assertEquals("text", result2);
        
        String result3 = parser.parse(partial, "");
        assertEquals("text", result3);
        
        String result4 = parser.parse(complete, "end");
        assertEquals("textend", result4);
    }

    @Test
    public void testNullLlmResponse() {
        CumulativeOutputParser parser = new CumulativeOutputParser();
        
        String result1 = parser.parse(null, "test");
        assertEquals("test", result1);
        
        String result2 = parser.parse(null, "more");
        assertEquals("testmore", result2);
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<String> results = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    CumulativeOutputParser parser = new CumulativeOutputParser();
                    LlmResponse partial = LlmResponse.builder().partial(true).build();
                    LlmResponse complete = LlmResponse.builder().partial(false).build();
                    
                    parser.parse(partial, "Thread" + threadId + "-");
                    parser.parse(partial, "Part1-");
                    String result = parser.parse(complete, "Part2");
                    
                    synchronized (results) {
                        results.add(result);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(threadCount, results.size());
        for (int i = 0; i < threadCount; i++) {
            final int expectedThreadId = i;
            assertTrue(results.stream().anyMatch(r -> r.contains("Thread" + expectedThreadId)));
        }
    }

    @Test
    public void testPartialNullValue() {
        CumulativeOutputParser parser = new CumulativeOutputParser();
        
        LlmResponse noPartial = LlmResponse.builder().build();
        
        String result1 = parser.parse(noPartial, "test");
        assertEquals("test", result1);
    }

    @Test
    public void testLongAccumulation() {
        CumulativeOutputParser parser = new CumulativeOutputParser();
        
        LlmResponse partial = LlmResponse.builder().partial(true).build();
        LlmResponse complete = LlmResponse.builder().partial(false).build();
        
        for (int i = 0; i < 100; i++) {
            parser.parse(partial, "chunk" + i + " ");
        }
        
        String result = parser.parse(complete, "end");
        
        assertTrue(result.startsWith("chunk0"));
        assertTrue(result.contains("chunk50"));
        assertTrue(result.endsWith("end"));
        assertTrue(result.contains("chunk99"));
    }

    @Test
    public void testBackwardCompatibility() {
        CumulativeOutputParser parser = new CumulativeOutputParser();
        
        String result1 = parser.parse("legacy1");
        assertEquals("legacy1", result1);
        
        String result2 = parser.parse("legacy2");
        assertEquals("legacy1legacy2", result2);
    }
}
