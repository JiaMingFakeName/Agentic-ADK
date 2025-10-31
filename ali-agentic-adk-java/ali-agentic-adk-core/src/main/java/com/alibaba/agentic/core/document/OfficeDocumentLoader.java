/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.document;

import com.alibaba.langengine.core.indexes.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Office文档加载器
 * 支持加载Word、Excel、PowerPoint文档
 */
@Slf4j
public class OfficeDocumentLoader extends BaseDocumentLoader {

    @Override
    public List<Document> loadFromPath(String filePath, Map<String, Object> metadata) {
        try {
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString().toLowerCase();
            String fileExtension = getFileExtension(fileName);
            
            if (metadata == null) {
                metadata = new HashMap<>();
            }
            metadata.put("file_name", fileName);
            metadata.put("file_extension", fileExtension);
            
            try (InputStream inputStream = Files.newInputStream(path)) {
                return loadFromStream(inputStream, metadata);
            }
        } catch (Exception e) {
            log.error("Failed to load document from path: {}", filePath, e);
            throw new RuntimeException("Failed to load document from path: " + filePath, e);
        }
    }

    @Override
    public List<Document> loadFromStream(InputStream inputStream, Map<String, Object> metadata) {
        try {
            if (metadata == null || !metadata.containsKey("file_extension")) {
                throw new IllegalArgumentException("Metadata must contain 'file_extension' for Office documents");
            }
            
            String fileExtension = (String) metadata.get("file_extension");
            String content;
            
            switch (fileExtension) {
                case "doc":
                    content = extractWordContent(inputStream, false);
                    break;
                case "docx":
                    content = extractWordContent(inputStream, true);
                    break;
                case "xls":
                    content = extractExcelContent(inputStream, false);
                    break;
                case "xlsx":
                    content = extractExcelContent(inputStream, true);
                    break;
                case "ppt":
                    content = extractPowerPointContent(inputStream, false);
                    break;
                case "pptx":
                    content = extractPowerPointContent(inputStream, true);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
            }
            
            return splitTextIntoDocuments(content, metadata);
        } catch (Exception e) {
            log.error("Failed to load document from stream", e);
            throw new RuntimeException("Failed to load document from stream", e);
        }
    }

    /**
     * 提取Word文档内容
     *
     * @param inputStream 输入流
     * @param isDocx 是否是DOCX格式
     * @return 文档内容
     */
    private String extractWordContent(InputStream inputStream, boolean isDocx) throws Exception {
        if (isDocx) {
            try (XWPFDocument document = new XWPFDocument(inputStream)) {
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                return extractor.getText();
            }
        } else {
            try (WordExtractor extractor = new WordExtractor(inputStream)) {
                return extractor.getText();
            }
        }
    }

    /**
     * 提取Excel文档内容
     *
     * @param inputStream 输入流
     * @param isXlsx 是否是XLSX格式
     * @return 文档内容
     */
    private String extractExcelContent(InputStream inputStream, boolean isXlsx) throws Exception {
        if (isXlsx) {
            try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
                XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
                extractor.setFormulasNotResults(false);
                extractor.setIncludeSheetNames(true);
                return extractor.getText();
            }
        } else {
            try (Workbook workbook = WorkbookFactory.create(inputStream)) {
                ExcelExtractor extractor = new ExcelExtractor((org.apache.poi.hssf.usermodel.HSSFWorkbook) workbook);
                extractor.setFormulasNotResults(false);
                extractor.setIncludeSheetNames(true);
                return extractor.getText();
            }
        }
    }

    /**
     * 提取PowerPoint文档内容
     *
     * @param inputStream 输入流
     * @param isPptx 是否是PPTX格式
     * @return 文档内容
     */
    private String extractPowerPointContent(InputStream inputStream, boolean isPptx) throws Exception {
        if (isPptx) {
            try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
                XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(ppt);
                return extractor.getText();
            }
        } else {
            try (PowerPointExtractor extractor = new PowerPointExtractor(inputStream)) {
                return extractor.getText();
            }
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public List<String> getSupportedTypes() {
        return Arrays.asList("doc", "docx", "xls", "xlsx", "ppt", "pptx");
    }
}