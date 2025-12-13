import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authAPI = {
  register: (email: string, password: string, fullName: string) =>
    api.post('/auth/register', { email, password, fullName }),

  login: (email: string, password: string) =>
    api.post('/auth/login', { email, password })
};

export const projectAPI = {
  create: (data: any) => api.post('/projects', data),
  list: () => api.get('/projects'),
  get: (id: string) => api.get(`/projects/${id}`),
  update: (id: string, data: any) => api.put(`/projects/${id}`, data),
  delete: (id: string) => api.delete(`/projects/${id}`)
};

export const fileAPI = {
  create: (projectId: string, data: any) =>
    api.post(`/projects/${projectId}/files`, data),

  list: (projectId: string) =>
    api.get(`/projects/${projectId}/files`),

  getTree: (projectId: string) =>
    api.get(`/projects/${projectId}/files/tree`),

  get: (projectId: string, path: string) =>
    api.get(`/projects/${projectId}/files`, {
      headers: { 'X-File-Path': path }
    }),

  update: (projectId: string, path: string, content: string) =>
    api.put(`/projects/${projectId}/files`,
      { path, content },
      { headers: { 'X-File-Path': path } }
    ),

  delete: (projectId: string, path: string) =>
    api.delete(`/projects/${projectId}/files`, {
      headers: { 'X-File-Path': path }
    })
};

export const executionAPI = {
  start: (projectId: string, language: string) =>
    api.post('/executions', { projectId, language }),

  get: (executionId: string) =>
    api.get(`/executions/${executionId}`),

  stop: (executionId: string) =>
    api.delete(`/executions/${executionId}`),

  getLogs: (executionId: string) =>
    api.get(`/executions/${executionId}/logs`),

  getByProject: (projectId: string) =>
    api.get(`/executions/project/${projectId}`)
};

export const analysisAPI = {
  start: (projectId: string, analysisType: string) =>
    api.post('/analysis', { projectId, analysisType }),

  get: (reportId: string) =>
    api.get(`/analysis/${reportId}`),

  getIssues: (reportId: string) =>
    api.get(`/analysis/${reportId}/issues`),

  getMetrics: (reportId: string) =>
    api.get(`/analysis/${reportId}/metrics`),

  getDependencies: (reportId: string) =>
    api.get(`/analysis/${reportId}/dependencies`),

  getByProject: (projectId: string) =>
    api.get(`/analysis/project/${projectId}`)
};

export const aiAPI = {
  generate: (data: any) =>
    api.post('/ai/generate', data),

  explain: (data: any) =>
    api.post('/ai/explain', data),

  review: (data: any) =>
    api.post('/ai/review', data),

  fix: (data: any) =>
    api.post('/ai/fix', data),

  test: (data: any) =>
    api.post('/ai/test', data),

  refactor: (data: any) =>
    api.post('/ai/refactor', data),

  document: (data: any) =>
    api.post('/ai/document', data),

  getTask: (taskId: string) =>
    api.get(`/ai/task/${taskId}`),

  getTasks: () =>
    api.get('/ai/tasks')
};

export default api;
