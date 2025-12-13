package com.codecraft.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileTreeNode {
    private String name;
    private String path;
    private String type;
    private List<FileTreeNode> children = new ArrayList<>();
}
