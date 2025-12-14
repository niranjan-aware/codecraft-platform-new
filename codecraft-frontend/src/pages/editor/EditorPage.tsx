import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectAPI, fileAPI, executionAPI } from '../../services/api';
import { Project, FileNode } from '../../types';
import Editor from '@monaco-editor/react';
import FileTree from '../../components/editor/FileTree';
import Terminal from '../../components/editor/Terminal';
import { Play, Square, ArrowLeft, Save, BarChart3 } from 'lucide-react';

const EditorPage: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const [project, setProject] = useState<Project | null>(null);
  const [fileTree, setFileTree] = useState<FileNode[]>([]);
  const [currentFile, setCurrentFile] = useState<string>('');
  const [fileContent, setFileContent] = useState<string>('');
  const [language, setLanguage] = useState<string>('javascript');
  const [running, setRunning] = useState(false);
  const [logs, setLogs] = useState<Array<{ level: string; message: string; timestamp: string }>>([]);
  const [executionId, setExecutionId] = useState<string | null>(null);

  useEffect(() => {
    if (projectId) {
      loadProject();
      loadFileTree();
    }
  }, [projectId]);

  // Poll for logs every 500ms when execution is running
  useEffect(() => {
    if (!executionId || !running) return;

    const interval = setInterval(async () => {
      try {
        const response = await executionAPI.getLogs(executionId);
        setLogs(response.data);
        
        // Check execution status
        const execResponse = await executionAPI.get(executionId);
        const status = execResponse.data.status;
        
        if (['SUCCESS', 'FAILED', 'STOPPED', 'TIMEOUT'].includes(status)) {
          setRunning(false);
          clearInterval(interval);
        }
      } catch (error) {
        console.error('Failed to fetch logs', error);
      }
    }, 500);

    return () => clearInterval(interval);
  }, [executionId, running]);

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
      addLog('INFO', `✓ Saved ${currentFile}`);
    } catch (error) {
      console.error('Failed to save file', error);
      addLog('ERROR', `✗ Failed to save ${currentFile}`);
    }
  };

  const addLog = (level: string, message: string) => {
    setLogs(prev => [...prev, { level, message, timestamp: new Date().toISOString() }]);
  };

  const handleRun = async () => {
    if (!project) return;
    
    setRunning(true);
    setLogs([]);
    addLog('INFO', `🚀 Starting execution (${project.language})...`);
    
    try {
      const execution = await executionAPI.start(projectId!, project.language);
      setExecutionId(execution.data.id);
      addLog('INFO', `Execution ID: ${execution.data.id}`);
    } catch (error: any) {
      console.error('Failed to execute', error);
      addLog('ERROR', `✗ Failed to start execution: ${error.response?.data?.message || error.message}`);
      setRunning(false);
    }
  };

  const handleStop = async () => {
    if (!executionId) return;
    try {
      await executionAPI.stop(executionId);
      setRunning(false);
      addLog('INFO', '⏹ Execution stopped');
    } catch (error) {
      console.error('Failed to stop execution', error);
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
        'java': 'public class Main {\n  public static void main(String[] args) {\n    System.out.println("Hello World");\n  }\n}\n',
        'html': '<!DOCTYPE html>\n<html>\n<head>\n  <title>Document</title>\n</head>\n<body>\n  <h1>Hello World</h1>\n</body>\n</html>\n',
        'css': '/* Add your styles here */\nbody {\n  font-family: sans-serif;\n}\n',
        'json': '{\n  "name": "project"\n}\n',
        'md': '# Document\n\nHello World\n'
      };
      
      const content = defaultContent[ext] || '// TODO: Add content\n';
      
      await fileAPI.create(projectId!, { path: fileName, content });
      await loadFileTree();
      addLog('INFO', `✓ Created ${fileName}`);
    } catch (error) {
      console.error('Failed to create file', error);
      addLog('ERROR', '✗ Failed to create file');
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
          {running ? (
            <button
              onClick={handleStop}
              style={{
                padding: '0.5rem 1rem',
                background: '#f44336',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem'
              }}
            >
              <Square size={18} />
              Stop
            </button>
          ) : (
            <button
              onClick={handleRun}
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
          )}
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
                  wordWrap: 'on',
                  automaticLayout: true
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
            <Terminal logs={logs} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditorPage;
