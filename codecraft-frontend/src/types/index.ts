export interface User {
  id: string;
  email: string;
  fullName: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  fullName: string;
}

export interface Project {
  id: string;
  name: string;
  description: string;
  language: string;
  userId: string;
  createdAt: string;
  updatedAt: string;
}

export interface FileNode {
  path: string;
  name: string;
  type: 'file' | 'directory';
  children?: FileNode[];
}

export interface File {
  path: string;
  content: string;
  language: string;
}

export interface Execution {
  id: string;
  projectId: string;
  language: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  output: string;
  error: string;
  startedAt: string;
  completedAt: string;
}

export interface AnalysisReport {
  id: string;
  projectId: string;
  userId: string;
  analysisType: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  startedAt: string;
  completedAt: string;
  errorMessage?: string;
}

export interface CodeIssue {
  id: string;
  analysisReportId: string;
  filePath: string;
  lineNumber: number;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  ruleId: string;
  message: string;
  suggestedFix?: string;
  codeSnippet?: string;
}

export interface ProjectMetrics {
  id: string;
  projectId: string;
  analysisReportId: string;
  linesOfCode: number;
  codeSmells: number;
  bugs: number;
  vulnerabilities: number;
  complexityScore: number;
  maintainabilityRating: string;
  reliabilityRating: string;
  securityRating: string;
}

export interface Dependency {
  id: string;
  projectId: string;
  analysisReportId: string;
  name: string;
  version: string;
  dependencyType: string;
  vulnerabilities: number;
  license?: string;
  outdated: boolean;
  latestVersion?: string;
}

export interface AITask {
  task_id: string;
  task_type: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  result?: string;
  error?: string;
  created_at: string;
  completed_at?: string;
}

export interface Workflow {
  workflow_id: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  progress: number;
  results?: any;
  error?: string;
  created_at: string;
  updated_at: string;
}

export interface Notification {
  id: string;
  user_id: string;
  notification_type: 'EMAIL' | 'WEBSOCKET' | 'IN_APP';
  status: 'PENDING' | 'SENT' | 'FAILED';
  data: {
    title?: string;
    message?: string;
    link?: string;
    [key: string]: any;
  };
  created_at: string;
  sent_at?: string;
}

export interface ProjectTemplate {
  id: string;
  name: string;
  description: string;
  language: string;
  files: {
    path: string;
    content: string;
  }[];
}
