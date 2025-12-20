import api from './api';

export const executionService = {
  async startExecution(request: { projectId: string; language: string }) {
    const response = await api.post('/executions', request);
    return response.data;
  },

  async getExecution(executionId: string) {
    const response = await api.get(`/executions/${executionId}`);
    return response.data;
  },

  async stopExecution(executionId: string) {
    await api.delete(`/executions/${executionId}`);
  },

  async getLogs(executionId: string) {
    const response = await api.get(`/executions/${executionId}/logs`);
    return response.data;
  }
};
