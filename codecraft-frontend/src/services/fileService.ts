import api from './api';

export interface FileNode {
  id: string;
  path: string;
  sizeBytes?: number;
  mimeType?: string;
}

export interface FileContent {
  file: FileNode;
  content: string;
}

export const fileService = {
  async listFiles(projectId: string): Promise<FileNode[]> {
    const response = await api.get(`/projects/${projectId}/files`);
    return response.data;
  },

  async getFileTree(projectId: string): Promise<any> {
    const response = await api.get(`/projects/${projectId}/files/tree`);
    return response.data;
  },

  async getFileContent(projectId: string, filePath: string): Promise<FileContent> {
    const response = await api.get(`/projects/${projectId}/files/**`, {
      headers: {
        'X-File-Path': filePath
      }
    });
    return response.data;
  },

  async updateFile(projectId: string, filePath: string, content: string): Promise<void> {
    await api.put(`/projects/${projectId}/files/**`, 
      { path: filePath, content },
      {
        headers: {
          'X-File-Path': filePath
        }
      }
    );
  },

  async createFile(projectId: string, request: { path: string; content: string }): Promise<FileNode> {
    const response = await api.post(`/projects/${projectId}/files`, request);
    return response.data;
  },

  async deleteFile(projectId: string, filePath: string): Promise<void> {
    await api.delete(`/projects/${projectId}/files/**`, {
      headers: {
        'X-File-Path': filePath
      }
    });
  }
};
