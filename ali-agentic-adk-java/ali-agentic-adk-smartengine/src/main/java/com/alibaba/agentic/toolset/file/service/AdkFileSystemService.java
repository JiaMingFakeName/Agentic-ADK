package com.alibaba.agentic.toolset.file.service;


import com.alibaba.agentic.toolset.file.dto.FileSystemNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface AdkFileSystemService {

    /**
     * 写入文件
     * @param path 文件路径
     * @param content 文件内容
     * @param append 是否追加写入
     * @return 是否写入成功
     */
    boolean writeFile(String path, byte[] content, boolean append);

    /**
     * 写入文件
     * @param path 文件路径
     * @param inputStream 文件内容输入流
     * @param append 是否追加写入
     * @return 是否写入成功
     */
    boolean writeFile(String path, InputStream inputStream, boolean append);

    /**
     * 获取文件系统结构
     * @param path 起始路径
     * @return 文件系统结构树
     */
    FileSystemNode getFileSystemStructure(String path);

    /**
     * 搜索文件
     * @param path 搜索起始路径
     * @param keyword 搜索关键词
     * @return 匹配的文件列表
     */
    List<File> searchFiles(String path, String keyword);

    /**
     /**
     * 将文件夹打包成zip并返回输出流
     * @param folderPath 要打包的文件夹路径
     * @return ZIP文件的输出流
     * @throws IOException 如果压缩过程中发生IO错误
     */
    OutputStream zipFolder(String folderPath) throws IOException;

}
