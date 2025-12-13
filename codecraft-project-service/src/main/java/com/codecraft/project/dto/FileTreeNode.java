package com.codecraft.project.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class FileTreeNode {
    private String name;
    private String path;
    private String type;
    private List<FileTreeNode> children = new ArrayList<>();
}
