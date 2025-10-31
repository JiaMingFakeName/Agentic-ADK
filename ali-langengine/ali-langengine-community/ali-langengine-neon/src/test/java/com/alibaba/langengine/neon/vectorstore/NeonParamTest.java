package com.alibaba.langengine.neon.vectorstore;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NeonParamTest {

    @Test
    @Order(1)
    void testDefaultValues() {
        NeonParam param = new NeonParam();
        
        assertNotNull(param.getInitParam());
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("meta_data", param.getFieldMeta());
        
        NeonParam.InitParam initParam = param.getInitParam();
        assertEquals("langengine_neon_collection", initParam.getTableName());
        assertEquals("cosine", initParam.getVectorDistance());
        assertEquals(768, initParam.getDimension());
        assertEquals("ivfflat", initParam.getIndexType());
    }

    @Test
    @Order(2)
    void testSetFieldNames() {
        NeonParam param = new NeonParam();
        
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameUniqueId("custom_id");
        param.setFieldMeta("custom_meta");
        
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_meta", param.getFieldMeta());
    }

    @Test
    @Order(3)
    void testSetInitParam() {
        NeonParam param = new NeonParam();
        NeonParam.InitParam initParam = new NeonParam.InitParam();
        
        initParam.setTableName("custom_table");
        initParam.setVectorDistance("l2");
        initParam.setDimension(1536);
        initParam.setIndexType("hnsw");
        
        param.setInitParam(initParam);
        
        assertEquals("custom_table", param.getInitParam().getTableName());
        assertEquals("l2", param.getInitParam().getVectorDistance());
        assertEquals(1536, param.getInitParam().getDimension());
        assertEquals("hnsw", param.getInitParam().getIndexType());
    }

    @Test
    @Order(4)
    void testModifyInitParam() {
        NeonParam param = new NeonParam();
        
        param.getInitParam().setTableName("modified_table");
        param.getInitParam().setDimension(512);
        
        assertEquals("modified_table", param.getInitParam().getTableName());
        assertEquals(512, param.getInitParam().getDimension());
    }
}
