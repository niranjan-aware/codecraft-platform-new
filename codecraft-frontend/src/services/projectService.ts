import api from './api';

export interface CreateProjectRequest {
  name: string;
  description: string;
  language: 'NODEJS' | 'PYTHON' | 'JAVA' | 'HTML_CSS_JS';
  framework?: 'EXPRESS' | 'REACT' | 'NEXTJS' | 'FLASK' | 'DJANGO' | 'SPRING_BOOT' | 'NONE';
  visibility: 'PUBLIC' | 'PRIVATE';
  projectType?: 'SCRIPT' | 'SERVER';
}

export interface ProjectResponse {
  id: string;
  userId: string;
  name: string;
  description: string;
  language: string;
  framework: string;
  visibility: string;
  projectType: 'SCRIPT' | 'SERVER' | null;
  githubUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export const projectService = {
  async createProject(request: CreateProjectRequest): Promise<ProjectResponse> {
    const response = await api.post('/projects', request);
    return response.data;
  },

  async getProject(projectId: string): Promise<ProjectResponse> {
    const response = await api.get(`/projects/${projectId}`);
    return response.data;
  },

  async listProjects(): Promise<ProjectResponse[]> {
    const response = await api.get('/projects');
    return response.data;
  },

  async updateProject(projectId: string, request: CreateProjectRequest): Promise<ProjectResponse> {
    const response = await api.put(`/projects/${projectId}`, request);
    return response.data;
  },

  async deleteProject(projectId: string): Promise<void> {
    await api.delete(`/projects/${projectId}`);
  }
};
