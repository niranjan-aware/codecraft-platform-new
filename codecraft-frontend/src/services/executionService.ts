import api from './api';

export interface ExecutionRequest {
  projectId: string;
  language: string;
}

export interface ExecutionResponse {
  id: string;
  projectId: string;
  containerId: string | null;
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'STOPPED';
  language: string;
  projectType: 'SCRIPT' | 'SERVER' | null;
  startedAt: string | null;
  completedAt: string | null;
  autoStopAt: string | null;
  hostPort: number | null;
  containerPort: number | null;
  publicUrl: string | null;
  cpuUsage: number | null;
  memoryUsage: number | null;
  exitCode: number | null;
  errorMessage: string | null;
  createdAt: string;
}

export interface LogMessage {
  level: string;
  message: string;
  timestamp: string;
}

export const executionService = {
  async startExecution(request: ExecutionRequest): Promise<ExecutionResponse> {
    const response = await api.post('/executions', request);
    return response.data;
  },

  async getExecution(executionId: string): Promise<ExecutionResponse> {
    const response = await api.get(`/executions/${executionId}`);
    return response.data;
  },

  async getExecutionsByProject(projectId: string): Promise<ExecutionResponse[]> {
    const response = await api.get(`/executions/project/${projectId}`);
    return response.data;
  },

  async getLogs(executionId: string): Promise<LogMessage[]> {
    const response = await api.get(`/executions/${executionId}/logs`);
    return response.data;
  },

  async stopExecution(executionId: string): Promise<void> {
    await api.post(`/executions/${executionId}/stop`);
  },

  async getRunningExecutions(): Promise<ExecutionResponse[]> {
    const response = await api.get('/executions/user/running');
    return response.data;
  }
};
