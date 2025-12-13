import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectAPI, fileAPI } from '../../services/api';
import { Project, FileNode } from '../../types';
import Editor from '@monaco-editor/react';
import FileTree from '../../components/editor/FileTree';
import Terminal from '../../components/editor/Terminal';
import { Play, ArrowLeft, Save, BarChart3 } from 'lucide-react';

const EditorPage: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const [project, setProject] = useState<Project | null>(null);
  const [fileTree, setFileTree] = useState<FileNode[]>([]);
  const [currentFile, setCurrentFile] = useState<string>('');
  const [fileContent, setFileContent] = useState<string>('');
  const [language, setLanguage] = useState<string>('javascript');

  useEffect(() => {
    if (projectId) {
      loadProject();
      loadFileTree();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projectId]);

  const loadProject = async () => {
    try {
      const response = await projectAPI.get(projectId!);
      setProject(response.data);
      setLanguage(response.data.language);
    } catch (error) {
      console.error('Failed to load project', error);
    }
  };

  const loadFileTree = async () => {
    try {
      const response = await fileAPI.getTree(projectId!);
      const data = response.data;
      if (Array.isArray(data)) {
        setFileTree(data);
      } else if (data && typeof data === 'object') {
        setFileTree([data]);
      } else {
        setFileTree([]);
      }
    } catch (error) {
      console.error('Failed to load file tree', error);
      setFileTree([]);
    }
  };

  const handleFileSelect = async (path: string) => {
    try {
      const response = await fileAPI.get(projectId!, path);
      setCurrentFile(path);
      // Response is { file: {...}, content: "..." }
      setFileContent(response.data.content || '');
      
      const ext = path.split('.').pop();
      const langMap: { [key: string]: string } = {
        js: 'javascript',
        ts: 'typescript',
        jsx: 'javascript',
        tsx: 'typescript',
        py: 'python',
        java: 'java',
        html: 'html',
        css: 'css',
        json: 'json',
        md: 'markdown'
      };
      setLanguage(langMap[ext || ''] || 'javascript');
    } catch (error) {
      console.error('Failed to load file', error);
    }
  };

  const handleSave = async () => {
    if (!currentFile) return;
    try {
      await fileAPI.update(projectId!, currentFile, fileContent);
      alert('File saved successfully!');
    } catch (error) {
      console.error('Failed to save file', error);
      alert('Failed to save file');
    }
  };

  const handleCreateFile = async () => {
    const fileName = prompt('Enter file name (e.g., index.js):');
    if (!fileName) return;
    
    try {
      const ext = fileName.split('.').pop() || '';
      const defaultContent: { [key: string]: string } = {
        'js': '// TODO: Add JavaScript code\nconsole.log("Hello World");\n',
        'ts': '// TODO: Add TypeScript code\nconsole.log("Hello World");\n',
        'py': '# TODO: Add Python code\nprint("Hello World")\n',
        'java': '// TODO: Add Java code\npublic class Main {\n  public static void main(String[] args) {\n    System.out.println("Hello World");\n  }\n}\n',
        'html': '<!DOCTYPE html>\n<html>\n<head>\n  <title>Document</title>\n</head>\n<body>\n  <h1>Hello World</h1>\n</body>\n</html>\n',
        'css': '/* Add your styles here */\nbody {\n  font-family: sans-serif;\n}\n',
        'json': '{\n  "name": "project"\n}\n',
        'md': '# Document\n\nHello World\n'
      };
      
      const content = defaultContent[ext] || '// TODO: Add content\n';
      
      await fileAPI.create(projectId!, { path: fileName, content });
      await loadFileTree();
    } catch (error) {
      console.error('Failed to create file', error);
      alert('Failed to create file');
    }
  };

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <nav style={{ 
        background: '#1e1e1e', 
        color: 'white', 
        padding: '0.75rem 1rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        borderBottom: '1px solid #333'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <button
            onClick={() => navigate('/dashboard')}
            style={{
              background: 'transparent',
              border: 'none',
              color: 'white',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.5rem'
            }}
          >
            <ArrowLeft size={20} />
            Back
          </button>
          <h2 style={{ margin: 0 }}>{project?.name || 'Loading...'}</h2>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            onClick={handleSave}
            disabled={!currentFile}
            style={{
              padding: '0.5rem 1rem',
              background: currentFile ? '#4CAF50' : '#555',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: currentFile ? 'pointer' : 'not-allowed',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            <Save size={18} />
            Save
          </button>
          <button
            onClick={() => navigate(`/analysis/${projectId}`)}
            style={{
              padding: '0.5rem 1rem',
              background: '#9C27B0',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            <BarChart3 size={18} />
            Analysis
          </button>
          <button
            style={{
              padding: '0.5rem 1rem',
              background: '#2196F3',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            <Play size={18} />
            Run
          </button>
        </div>
      </nav>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        <div style={{ width: '250px', background: '#252526', borderRight: '1px solid #333', overflow: 'auto' }}>
          <div style={{ padding: '0.5rem', borderBottom: '1px solid #333' }}>
            <button
              onClick={handleCreateFile}
              style={{
                width: '100%',
                padding: '0.5rem',
                background: '#007acc',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              + New File
            </button>
          </div>
          <FileTree files={fileTree} onFileSelect={handleFileSelect} />
        </div>

        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <div style={{ flex: 1, overflow: 'hidden' }}>
            {currentFile ? (
              <Editor
                height="100%"
                language={language}
                value={fileContent}
                onChange={(value) => setFileContent(value || '')}
                theme="vs-dark"
                options={{
                  minimap: { enabled: false },
                  fontSize: 14,
                  lineNumbers: 'on',
                  rulers: [80],
                  wordWrap: 'on'
                }}
              />
            ) : (
              <div style={{ 
                height: '100%', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                color: '#888',
                background: '#1e1e1e'
              }}>
                Select a file to edit
              </div>
            )}
          </div>

          <div style={{ height: '200px', borderTop: '1px solid #333' }}>
            <Terminal projectId={projectId || ''} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditorPage;
