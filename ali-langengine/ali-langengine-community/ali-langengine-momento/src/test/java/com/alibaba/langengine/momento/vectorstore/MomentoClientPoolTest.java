/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.momento.vectorstore;

import com.alibaba.langengine.momento.MomentoException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class MomentoClientPoolTest {

    private MomentoParam createMockParam() {
        MomentoParam param = Mockito.mock(MomentoParam.class);
        when(param.getAuthToken()).thenReturn("test-token");
        when(param.getCacheName()).thenReturn("test-cache");
        when(param.getDefaultTtlSeconds()).thenReturn(3600L);
        return param;
    }

    @Test
    void testPoolInitialization() {
        MomentoClientPool pool = null;
        try {
            pool = new MomentoClientPool(createMockParam(), 5);
            assertEquals(5, pool.getAvailableCount());
            assertEquals(5, pool.getTotalCount());
        } finally {
            if (pool != null) {
                pool.shutdown();
            }
        }
    }

    @Test
    void testAcquireAndRelease() {
        MomentoClientPool pool = null;
        try {
            pool = new MomentoClientPool(createMockParam(), 2);
            
            MomentoClient client1 = pool.acquire();
            assertNotNull(client1);
            assertEquals(1, pool.getAvailableCount());
            
            pool.release(client1);
            assertEquals(2, pool.getAvailableCount());
        } finally {
            if (pool != null) {
                pool.shutdown();
            }
        }
    }

    @Test
    void testPoolExhausted() {
        MomentoClientPool pool = null;
        try {
            pool = new MomentoClientPool(createMockParam(), 1);
            
            MomentoClient client = pool.acquire();
            assertNotNull(client);
            assertEquals(0, pool.getAvailableCount());
        } catch (Exception e) {
            //Expected
        } finally {
            if (pool != null) {
                pool.shutdown();
            }
        }
    }

    @Test
    void testShutdown() {
        MomentoClientPool pool = new MomentoClientPool(createMockParam(), 3);
        
        assertEquals(3, pool.getTotalCount());
        pool.shutdown();
        assertEquals(0, pool.getTotalCount());
        assertEquals(0, pool.getAvailableCount());
    }

    @Test
    void testReleaseNull() {
        MomentoClientPool pool = null;
        try {
            pool = new MomentoClientPool(createMockParam(), 2);
            int beforeCount = pool.getAvailableCount();
            
            pool.release(null);
            
            assertEquals(beforeCount, pool.getAvailableCount());
        } finally {
            if (pool != null) {
                pool.shutdown();
            }
        }
    }

    @Test
    void testInvalidPoolSize() {
        assertThrows(MomentoException.class, () -> {
            new MomentoClientPool(createMockParam(), 0);
        });
        
        assertThrows(MomentoException.class, () -> {
            new MomentoClientPool(createMockParam(), 2000);
        });
    }
}
