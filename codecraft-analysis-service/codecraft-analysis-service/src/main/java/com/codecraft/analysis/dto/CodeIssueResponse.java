package com.codecraft.analysis.dto;

import com.codecraft.analysis.entity.CodeIssue;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CodeIssueResponse {
    private UUID id;
    private String filePath;
    private Integer lineNumber;
    private CodeIssue.Severity severity;
    private String ruleId;
    private String message;
    private String suggestedFix;
    private Boolean aiFixAvailable;
    private String codeSnippet;
}
