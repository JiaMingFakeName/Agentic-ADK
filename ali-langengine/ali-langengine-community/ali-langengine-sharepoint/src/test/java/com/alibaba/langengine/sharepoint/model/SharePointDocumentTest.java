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
package com.alibaba.langengine.sharepoint.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SharePoint文档模型测试类
 * 
 * @author AIDC-AI
 */
public class SharePointDocumentTest {
    
    @Test
    public void testDefaultConstructor() {
        SharePointDocument document = new SharePointDocument();
        
        assertNull(document.getId());
        assertNull(document.getName());
        assertNull(document.getSize());
        assertNull(document.getFileType());
        assertNull(document.getWebUrl());
        assertNull(document.getDownloadUrl());
        assertNull(document.getCreatedDateTime());
        assertNull(document.getCreatedBy());
        assertNull(document.getLastModifiedDateTime());
        assertNull(document.getLastModifiedBy());
        assertNull(document.getDriveId());
        assertNull(document.getParentReferenceId());
    }
    
    @Test
    public void testParameterizedConstructor() {
        String id = "12345";
        String name = "test-document.docx";
        
        SharePointDocument document = new SharePointDocument(id, name);
        
        assertEquals(id, document.getId());
        assertEquals(name, document.getName());
    }
    
    @Test
    public void testSettersAndGetters() {
        SharePointDocument document = new SharePointDocument();
        
        document.setId("12345");
        document.setName("test-document.docx");
        document.setSize(1024L);
        document.setFileType("docx");
        document.setWebUrl("https://test.sharepoint.com/sites/test/documents/test-document.docx");
        document.setDownloadUrl("https://test.sharepoint.com/sites/test/documents/test-document.docx/download");
        document.setCreatedDateTime("2024-01-01T00:00:00Z");
        document.setCreatedBy("user@example.com");
        document.setLastModifiedDateTime("2024-01-02T00:00:00Z");
        document.setLastModifiedBy("admin@example.com");
        document.setDriveId("drive-123");
        document.setParentReferenceId("parent-456");
        
        assertEquals("12345", document.getId());
        assertEquals("test-document.docx", document.getName());
        assertEquals(Long.valueOf(1024L), document.getSize());
        assertEquals("docx", document.getFileType());
        assertEquals("https://test.sharepoint.com/sites/test/documents/test-document.docx", document.getWebUrl());
        assertEquals("https://test.sharepoint.com/sites/test/documents/test-document.docx/download", document.getDownloadUrl());
        assertEquals("2024-01-01T00:00:00Z", document.getCreatedDateTime());
        assertEquals("user@example.com", document.getCreatedBy());
        assertEquals("2024-01-02T00:00:00Z", document.getLastModifiedDateTime());
        assertEquals("admin@example.com", document.getLastModifiedBy());
        assertEquals("drive-123", document.getDriveId());
        assertEquals("parent-456", document.getParentReferenceId());
    }
    
    @Test
    public void testLargeFileSize() {
        SharePointDocument document = new SharePointDocument();
        document.setSize(1073741824L); // 1GB
        
        assertEquals(Long.valueOf(1073741824L), document.getSize());
    }
    
    @Test
    public void testZeroFileSize() {
        SharePointDocument document = new SharePointDocument();
        document.setSize(0L);
        
        assertEquals(Long.valueOf(0L), document.getSize());
    }
    
    @Test
    public void testNullFileSize() {
        SharePointDocument document = new SharePointDocument();
        document.setSize(null);
        
        assertNull(document.getSize());
    }
    
    @Test
    public void testSpecialCharactersInName() {
        SharePointDocument document = new SharePointDocument();
        document.setName("test-document with spaces & special chars (1).docx");
        
        assertEquals("test-document with spaces & special chars (1).docx", document.getName());
    }
    
    @Test
    public void testLongFileName() {
        SharePointDocument document = new SharePointDocument();
        String longName = "very-long-document-name-with-many-characters-that-exceeds-normal-length-limits.docx";
        document.setName(longName);
        
        assertEquals(longName, document.getName());
    }
    
    @Test
    public void testEmptyFileName() {
        SharePointDocument document = new SharePointDocument();
        document.setName("");
        
        assertEquals("", document.getName());
    }
    
    @Test
    public void testNullFileName() {
        SharePointDocument document = new SharePointDocument();
        document.setName(null);
        
        assertNull(document.getName());
    }
    
    @Test
    public void testDifferentFileTypes() {
        SharePointDocument document = new SharePointDocument();
        
        document.setFileType("pdf");
        assertEquals("pdf", document.getFileType());
        
        document.setFileType("xlsx");
        assertEquals("xlsx", document.getFileType());
        
        document.setFileType("pptx");
        assertEquals("pptx", document.getFileType());
        
        document.setFileType("txt");
        assertEquals("txt", document.getFileType());
    }
    
    @Test
    public void testComplexUrls() {
        SharePointDocument document = new SharePointDocument();
        
        String complexUrl = "https://test-domain.sharepoint.com/sites/very-long-site-name/documents/folder/subfolder/document.docx";
        document.setWebUrl(complexUrl);
        
        assertEquals(complexUrl, document.getWebUrl());
    }
    
    @Test
    public void testDateTimeFormats() {
        SharePointDocument document = new SharePointDocument();
        
        document.setCreatedDateTime("2024-01-01T00:00:00Z");
        assertEquals("2024-01-01T00:00:00Z", document.getCreatedDateTime());
        
        document.setLastModifiedDateTime("2024-12-31T23:59:59.999Z");
        assertEquals("2024-12-31T23:59:59.999Z", document.getLastModifiedDateTime());
    }
    
    @Test
    public void testUserEmails() {
        SharePointDocument document = new SharePointDocument();
        
        document.setCreatedBy("user.name@company-domain.com");
        assertEquals("user.name@company-domain.com", document.getCreatedBy());
        
        document.setLastModifiedBy("admin.user@company-domain.com");
        assertEquals("admin.user@company-domain.com", document.getLastModifiedBy());
    }
    
    @Test
    public void testDriveAndParentIds() {
        SharePointDocument document = new SharePointDocument();
        
        document.setDriveId("drive-123-456-789");
        assertEquals("drive-123-456-789", document.getDriveId());
        
        document.setParentReferenceId("parent-abc-def-ghi");
        assertEquals("parent-abc-def-ghi", document.getParentReferenceId());
    }
}
