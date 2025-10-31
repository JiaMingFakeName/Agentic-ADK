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
package com.alibaba.langengine.sharepoint.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.sharepoint.SharePointConfiguration;
import com.alibaba.langengine.sharepoint.client.SharePointClient;
import com.alibaba.langengine.sharepoint.exception.SharePointException;
import com.alibaba.langengine.sharepoint.model.SharePointDocument;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SharePoint文档工具测试类
 * 
 * @author AIDC-AI
 */
public class SharePointDocumentToolTest {
    
    @Mock
    private SharePointClient mockSharePointClient;
    
    private SharePointDocumentTool documentTool;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        documentTool = new SharePointDocumentTool(mockSharePointClient);
    }
    
    @Test
    public void testDefaultConstructor() {
        SharePointDocumentTool tool = new SharePointDocumentTool();
        
        assertEquals("sharepoint_document_operation", tool.getName());
        assertEquals("operateSharePointDocument", tool.getFunctionName());
        assertEquals("SharePoint文档操作", tool.getHumanName());
        assertTrue(tool.getDescription().contains("文档"));
        assertNull(tool.getSharePointClient());
    }
    
    @Test
    public void testConstructorWithConfiguration() {
        SharePointConfiguration config = new SharePointConfiguration(
            "tenant-123", 
            "client-456", 
            "secret-789", 
            "https://test.sharepoint.com"
        );
        
        SharePointDocumentTool tool = new SharePointDocumentTool(config);
        
        assertEquals("sharepoint_document_operation", tool.getName());
        assertNotNull(tool.getSharePointClient());
    }
    
    @Test
    public void testConstructorWithClient() {
        SharePointDocumentTool tool = new SharePointDocumentTool(mockSharePointClient);
        
        assertEquals("sharepoint_document_operation", tool.getName());
        assertEquals(mockSharePointClient, tool.getSharePointClient());
    }
    
    @Test
    public void testExecuteWithNullClient() {
        SharePointDocumentTool tool = new SharePointDocumentTool();
        
        String input = "{\"operation\": \"search\"}";
        ToolExecuteResult result = tool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("客户端未初始化"));
    }
    
    @Test
    public void testExecuteWithEmptyOperation() {
        String input = "{\"operation\": \"\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("操作类型不能为空"));
    }
    
    @Test
    public void testExecuteWithNullOperation() {
        String input = "{\"operation\": null}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("操作类型不能为空"));
    }
    
    @Test
    public void testExecuteWithUnsupportedOperation() {
        String input = "{\"operation\": \"unsupported\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("不支持的操作类型"));
    }
    
    @Test
    public void testSearchDocumentsSuccess() throws SharePointException {
        List<SharePointDocument> documents = new ArrayList<>();
        SharePointDocument doc1 = new SharePointDocument();
        doc1.setId("12345");
        doc1.setName("test1.docx");
        documents.add(doc1);
        
        SharePointDocument doc2 = new SharePointDocument();
        doc2.setId("67890");
        doc2.setName("test2.pdf");
        documents.add(doc2);
        
        when(mockSharePointClient.searchDocuments(eq("test query"), isNull()))
            .thenReturn(documents);
        
        String input = "{\"operation\": \"search\", \"query\": \"test query\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("document_search_result"));
        assertTrue(result.getOutput().contains("test1.docx"));
        assertTrue(result.getOutput().contains("test2.pdf"));
        
        verify(mockSharePointClient).searchDocuments("test query", null);
    }
    
    @Test
    public void testSearchDocumentsWithSiteId() throws SharePointException {
        List<SharePointDocument> documents = new ArrayList<>();
        when(mockSharePointClient.searchDocuments(eq("test"), eq("site-123")))
            .thenReturn(documents);
        
        String input = "{\"operation\": \"search\", \"query\": \"test\", \"site_id\": \"site-123\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertFalse(result.isError());
        verify(mockSharePointClient).searchDocuments("test", "site-123");
    }
    
    @Test
    public void testUploadDocumentSuccess() throws SharePointException {
        SharePointDocument mockDocument = new SharePointDocument();
        mockDocument.setId("12345");
        mockDocument.setName("test.docx");
        mockDocument.setSize(1024L);
        mockDocument.setWebUrl("https://test.sharepoint.com/documents/test.docx");
        
        when(mockSharePointClient.uploadDocument(eq("drive-123"), eq("test.docx"), any(byte[].class)))
            .thenReturn(mockDocument);
        
        String input = "{\"operation\": \"upload\", \"drive_id\": \"drive-123\", \"file_name\": \"test.docx\", \"content\": \"dGVzdCBjb250ZW50\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("document_upload_result"));
        assertTrue(result.getOutput().contains("12345"));
        assertTrue(result.getOutput().contains("test.docx"));
        
        verify(mockSharePointClient).uploadDocument(eq("drive-123"), eq("test.docx"), any(byte[].class));
    }
    
    @Test
    public void testUploadDocumentWithTextContent() throws SharePointException {
        SharePointDocument mockDocument = new SharePointDocument();
        mockDocument.setId("12345");
        mockDocument.setName("test.txt");
        
        when(mockSharePointClient.uploadDocument(eq("drive-123"), eq("test.txt"), any(byte[].class)))
            .thenReturn(mockDocument);
        
        String input = "{\"operation\": \"upload\", \"drive_id\": \"drive-123\", \"file_name\": \"test.txt\", \"content\": \"test content\", \"content_type\": \"text\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertFalse(result.isError());
        verify(mockSharePointClient).uploadDocument(eq("drive-123"), eq("test.txt"), any(byte[].class));
    }
    
    @Test
    public void testUploadDocumentWithMissingParameters() {
        String input = "{\"operation\": \"upload\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("驱动器ID不能为空"));
    }
    
    @Test
    public void testDownloadDocumentSuccess() throws SharePointException {
        String content = "This is test document content";
        when(mockSharePointClient.getDocumentContent(eq("12345")))
            .thenReturn(content);
        
        String input = "{\"operation\": \"download\", \"document_id\": \"12345\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("document_download_result"));
        assertTrue(result.getOutput().contains("12345"));
        assertTrue(result.getOutput().contains(content));
        
        verify(mockSharePointClient).getDocumentContent("12345");
    }
    
    @Test
    public void testDownloadDocumentWithMissingDocumentId() {
        String input = "{\"operation\": \"download\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("文档ID不能为空"));
    }
    
    @Test
    public void testDeleteDocumentSuccess() throws SharePointException {
        doNothing().when(mockSharePointClient).deleteDocument(eq("12345"));
        
        String input = "{\"operation\": \"delete\", \"document_id\": \"12345\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertFalse(result.isError());
        assertTrue(result.getOutput().contains("document_delete_result"));
        assertTrue(result.getOutput().contains("12345"));
        assertTrue(result.getOutput().contains("deleted"));
        
        verify(mockSharePointClient).deleteDocument("12345");
    }
    
    @Test
    public void testDeleteDocumentWithMissingDocumentId() {
        String input = "{\"operation\": \"delete\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("文档ID不能为空"));
    }
    
    @Test
    public void testExecuteWithSharePointException() throws SharePointException {
        when(mockSharePointClient.searchDocuments(anyString(), anyString()))
            .thenThrow(new SharePointException("API调用失败"));
        
        String input = "{\"operation\": \"search\", \"query\": \"test\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("文档操作失败"));
        assertTrue(result.getOutput().contains("API调用失败"));
    }
    
    @Test
    public void testExecuteWithGeneralException() throws SharePointException {
        when(mockSharePointClient.searchDocuments(anyString(), anyString()))
            .thenThrow(new RuntimeException("网络错误"));
        
        String input = "{\"operation\": \"search\", \"query\": \"test\"}";
        ToolExecuteResult result = documentTool.execute(input);
        
        assertTrue(result.isError());
        assertTrue(result.getOutput().contains("文档操作过程中发生未知错误"));
        assertTrue(result.getOutput().contains("网络错误"));
    }
    
    @Test
    public void testExecuteWithMapParameters() throws SharePointException {
        List<SharePointDocument> documents = new ArrayList<>();
        when(mockSharePointClient.searchDocuments(eq("test"), isNull()))
            .thenReturn(documents);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "search");
        parameters.put("query", "test");
        
        ToolExecuteResult result = documentTool.execute(parameters);
        
        assertFalse(result.isError());
        verify(mockSharePointClient).searchDocuments("test", null);
    }
    
    @Test
    public void testRunMethod() throws SharePointException {
        List<SharePointDocument> documents = new ArrayList<>();
        when(mockSharePointClient.searchDocuments(anyString(), anyString()))
            .thenReturn(documents);
        
        String input = "{\"operation\": \"search\", \"query\": \"test\"}";
        ToolExecuteResult result = documentTool.run(input);
        
        assertFalse(result.isError());
        verify(mockSharePointClient).searchDocuments("test", null);
    }
}
