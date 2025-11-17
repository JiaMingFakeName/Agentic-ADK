package com.alibaba.agentic.toolset.file.dto;

import com.alibaba.agentic.toolset.file.service.AdkFileSystemService;
import lombok.Data;

import java.util.List;
@Data
public class FileSystemNode {

    private String name;
    private boolean isDirectory;
    private List<FileSystemNode> children;

}
