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

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@Disabled("Requires a running Momento instance and credentials")
public class MomentoTest {

    private Momento momento;

    @BeforeEach
    void setUp() {
        // This will use the environment variables for configuration
        momento = new Momento();
        momento.setEmbedding(new FakeEmbeddings());
    }

    @Test
    void testAddAndSearch() {
        com.alibaba.langengine.core.indexes.Document doc = new com.alibaba.langengine.core.indexes.Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("hello world");
        
        momento.addDocuments(Lists.newArrayList(doc));

        java.util.List<com.alibaba.langengine.core.indexes.Document> results = momento.similaritySearch("hello", 1);

        assertNotNull(results);
    }

    @Test
    void testDelete() {
        com.alibaba.langengine.core.indexes.Document doc = new com.alibaba.langengine.core.indexes.Document();
        doc.setUniqueId("doc2");
        doc.setPageContent("to be deleted");
        
        momento.addDocuments(Lists.newArrayList(doc));

        momento.deleteDocuments(Lists.newArrayList("doc2"));
    }
}
