import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Split from 'react-split';
import { RootState } from '../../store';
import { fileAPI, projectAPI, executionAPI } from '../../services/api';
import { wsService } from '../../services/websocket';
import FileTree from '../../components/editor/FileTree';
import CodeEditor from '../../components/editor/CodeEditor';
import Terminal from '../../components/editor/Terminal';
import { FileTreeNode, EditorFile, LogMessage, Project, Execution } from '../../types';
import { Play, Square, Save, ArrowLeft, FilePlus, FolderPlus } from 'lucide-react';

export default function EditorPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const user = useSelector((state: RootState) => state.auth.user);

  const [project, setProject] = useState<Project | null>(null);
  const [fileTree, setFileTree] = useState<FileTreeNode | null>(null);
  const [openFiles, setOpenFiles] = useState<EditorFile[]>([]);
  const [activeFile, setActiveFile] = useState<string | null>(null);
  const [logs, setLogs] = useState<LogMessage[]>([]);
  const [execution, setExecution] = useState<Execution | null>(null);
  const [isExecuting, setIsExecuting] = useState(false);
  const [showNewFileModal, setShowNewFileModal] = useState(false);
  const [newFileName, setNewFileName] = useState('');
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadProject();
    loadFileTree();

    return () => {
      if (execution) {
        wsService.unsubscribeLogs(execution.id);
      }
      wsService.disconnect();
    };
  }, [projectId]);

  const loadProject = async () => {
    try {
      const response = await projectAPI.get(projectId!);
      setProject(response.data);
    } catch (error) {
      console.error('Failed to load project', error);
      setError('Failed to load project');
    }
  };

  const loadFileTree = async () => {
    try {
      const response = await fileAPI.getTree(projectId!);
      setFileTree(response.data);
    } catch (error: any) {
      console.error('Failed to load file tree', error);
      
      if (error.response?.status === 404 || !error.response) {
        setFileTree({
          name: 'root',
          path: '/',
          type: 'directory',
          children: []
        });
      }
    }
  };

  const handleFileSelect = async (path: string) => {
    const existingFile = openFiles.find(f => f.path === path);
    
    if (existingFile) {
      setActiveFile(path);
      return;
    }

    try {
      const response = await fileAPI.get(projectId!, path);
      const fileContent = response.data;
      
      const newFile: EditorFile = {
        path,
        content: fileContent.content,
        language: detectLanguage(path),
        modified: false
      };

      setOpenFiles([...openFiles, newFile]);
      setActiveFile(path);
    } catch (error) {
      console.error('Failed to load file', error);
      setError('Failed to load file');
    }
  };

  const handleCreateFile = async () => {
    if (!newFileName.trim()) {
      setError('File name cannot be empty');
      return;
    }

    try {
      // Create file with empty content (validation now allows this)
      await fileAPI.create(projectId!, {
        path: newFileName.trim(),
        content: ''
      });

      setShowNewFileModal(false);
      setNewFileName('');
      setError('');
      
      // Reload file tree
      await loadFileTree();
      
      // Open the newly created file
      setTimeout(() => {
        handleFileSelect(newFileName.trim());
      }, 500);
    } catch (error: any) {
      console.error('Failed to create file', error);
      setError(error.response?.data?.message || 'Failed to create file');
    }
  };

  const handleEditorChange = (value: string | undefined) => {
    if (!activeFile || value === undefined) return;

    setOpenFiles(openFiles.map(file => 
      file.path === activeFile 
        ? { ...file, content: value, modified: true }
        : file
    ));
  };

  const handleSaveFile = async () => {
    if (!activeFile) return;

    const file = openFiles.find(f => f.path === activeFile);
    if (!file) return;

    try {
      await fileAPI.update(projectId!, file.path, file.content);
      setOpenFiles(openFiles.map(f => 
        f.path === activeFile ? { ...f, modified: false } : f
      ));
      setError('');
    } catch (error) {
      console.error('Failed to save file', error);
      setError('Failed to save file');
    }
  };

  const handleRun = async () => {
    if (!project || isExecuting) return;

    try {
      setIsExecuting(true);
      setLogs([]);
      setError('');

      wsService.connect(() => {
        console.log('WebSocket connected for logs');
      });

      const response = await executionAPI.start(projectId!, project.language);
      const newExecution = response.data;
      setExecution(newExecution);

      wsService.subscribeLogs(newExecution.id, (log: LogMessage) => {
        setLogs(prev => [...prev, log]);
      });

      pollExecutionStatus(newExecution.id);
    } catch (error) {
      console.error('Failed to start execution', error);
      setIsExecuting(false);
      setError('Failed to start execution');
    }
  };

  const pollExecutionStatus = async (executionId: string) => {
    const interval = setInterval(async () => {
      try {
        const response = await executionAPI.get(executionId);
        const exec = response.data;
        setExecution(exec);

        if (['SUCCESS', 'FAILED', 'TIMEOUT', 'STOPPED'].includes(exec.status)) {
          clearInterval(interval);
          setIsExecuting(false);
          wsService.unsubscribeLogs(executionId);
        }
      } catch (error) {
        clearInterval(interval);
        setIsExecuting(false);
      }
    }, 2000);
  };

  const handleStop = async () => {
    if (!execution) return;

    try {
      await executionAPI.stop(execution.id);
      wsService.unsubscribeLogs(execution.id);
      setIsExecuting(false);
    } catch (error) {
      console.error('Failed to stop execution', error);
    }
  };

  const handleCloseTab = (path: string) => {
    setOpenFiles(openFiles.filter(f => f.path !== path));
    if (activeFile === path) {
      const remainingFiles = openFiles.filter(f => f.path !== path);
      setActiveFile(remainingFiles.length > 0 ? remainingFiles[0].path : null);
    }
  };

  const detectLanguage = (path: string): string => {
    const ext = path.split('.').pop()?.toLowerCase();
    const languageMap: { [key: string]: string } = {
      js: 'javascript',
      jsx: 'javascript',
      ts: 'typescript',
      tsx: 'typescript',
      py: 'python',
      java: 'java',
      html: 'html',
      css: 'css',
      json: 'json',
      md: 'markdown',
      yml: 'yaml',
      yaml: 'yaml',
      xml: 'xml',
      sql: 'sql'
    };
    return languageMap[ext || ''] || 'plaintext';
  };

  const currentFile = openFiles.find(f => f.path === activeFile);

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Header - same as before */}
      <div style={{
        height: '60px',
        borderBottom: '1px solid #ddd',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 20px',
        backgroundColor: '#2c3e50',
        color: 'white'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <button
            onClick={() => navigate('/dashboard')}
            style={{
              background: 'transparent',
              border: 'none',
              color: 'white',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '5px'
            }}
          >
            <ArrowLeft size={20} />
            Back
          </button>
          <h2 style={{ margin: 0 }}>{project?.name || 'Loading...'}</h2>
          <span style={{ 
            fontSize: '12px', 
            padding: '4px 8px', 
            backgroundColor: '#34495e',
            borderRadius: '4px'
          }}>
            {project?.language}
          </span>
        </div>
        
        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            onClick={() => setShowNewFileModal(true)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              padding: '8px 16px',
              backgroundColor: '#3498db'
            }}
          >
            <FilePlus size={16} />
            New File
          </button>

          <button
            onClick={handleSaveFile}
            disabled={!currentFile?.modified}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              padding: '8px 16px',
              backgroundColor: currentFile?.modified ? '#27ae60' : '#95a5a6',
              cursor: currentFile?.modified ? 'pointer' : 'not-allowed'
            }}
          >
            <Save size={16} />
            Save
          </button>
          
          {!isExecuting ? (
            <button
              onClick={handleRun}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                padding: '8px 16px',
                backgroundColor: '#27ae60'
              }}
            >
              <Play size={16} />
              Run
            </button>
          ) : (
            <button
              onClick={handleStop}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                padding: '8px 16px',
                backgroundColor: '#e74c3c'
              }}
            >
              <Square size={16} />
              Stop
            </button>
          )}
        </div>
      </div>

      {error && (
        <div style={{
          padding: '10px',
          backgroundColor: '#e74c3c',
          color: 'white',
          textAlign: 'center'
        }}>
          {error}
          <button onClick={() => setError('')} style={{
            marginLeft: '10px',
            background: 'transparent',
            border: '1px solid white',
            color: 'white',
            cursor: 'pointer',
            padding: '4px 8px',
            borderRadius: '4px'
          }}>
            Dismiss
          </button>
        </div>
      )}

      <div style={{ flex: 1, overflow: 'hidden' }}>
        <Split
          sizes={[20, 80]}
          minSize={200}
          style={{ display: 'flex', height: '100%' }}
        >
          <div style={{ height: '100%', overflow: 'auto', backgroundColor: '#f5f5f5' }}>
            {fileTree && fileTree.children.length > 0 ? (
              <FileTree
                tree={fileTree}
                onFileSelect={handleFileSelect}
                selectedFile={activeFile}
              />
            ) : (
              <div style={{
                padding: '20px',
                textAlign: 'center',
                color: '#666'
              }}>
                <p>No files yet</p>
                <button
                  onClick={() => setShowNewFileModal(true)}
                  style={{
                    marginTop: '10px',
                    padding: '8px 16px',
                    backgroundColor: '#3498db',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Create First File
                </button>
              </div>
            )}
          </div>

          <Split
            direction="vertical"
            sizes={[60, 40]}
            minSize={100}
            style={{ height: '100%' }}
          >
            <div style={{ height: '100%', backgroundColor: '#1e1e1e' }}>
              {openFiles.length > 0 && (
                <div style={{
                  height: '40px',
                  backgroundColor: '#2d2d2d',
                  display: 'flex',
                  alignItems: 'center',
                  borderBottom: '1px solid #1e1e1e'
                }}>
                  {openFiles.map(file => (
                    <div
                      key={file.path}
                      style={{
                        padding: '10px 20px',
                        cursor: 'pointer',
                        backgroundColor: file.path === activeFile ? '#1e1e1e' : 'transparent',
                        color: 'white',
                        borderRight: '1px solid #1e1e1e',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px'
                      }}
                    >
                      <span onClick={() => setActiveFile(file.path)}>
                        {file.path.split('/').pop()}
                        {file.modified && <span style={{ color: '#f39c12' }}> ●</span>}
                      </span>
                      <button
                        onClick={() => handleCloseTab(file.path)}
                        style={{
                          background: 'transparent',
                          border: 'none',
                          color: '#999',
                          cursor: 'pointer',
                          fontSize: '16px'
                        }}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              )}
              
              <div style={{ height: openFiles.length > 0 ? 'calc(100% - 40px)' : '100%' }}>
                {currentFile ? (
                  <CodeEditor
                    value={currentFile.content}
                    onChange={handleEditorChange}
                    language={currentFile.language}
                  />
                ) : (
                  <div style={{
                    height: '100%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#888',
                    backgroundColor: '#1e1e1e',
                    flexDirection: 'column',
                    gap: '20px'
                  }}>
                    <p>Select a file to start editing</p>
                    <button
                      onClick={() => setShowNewFileModal(true)}
                      style={{
                        padding: '10px 20px',
                        backgroundColor: '#3498db',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer'
                      }}
                    >
                      Create New File
                    </button>
                  </div>
                )}
              </div>
            </div>

            <div style={{ height: '100%', backgroundColor: '#1e1e1e' }}>
              <div style={{
                height: '30px',
                backgroundColor: '#2d2d2d',
                display: 'flex',
                alignItems: 'center',
                padding: '0 10px',
                color: 'white',
                fontSize: '14px',
                borderBottom: '1px solid #1e1e1e'
              }}>
                Terminal
                {execution && (
                  <span style={{
                    marginLeft: '10px',
                    padding: '2px 8px',
                    fontSize: '12px',
                    backgroundColor: execution.status === 'RUNNING' ? '#f39c12' :
                                   execution.status === 'SUCCESS' ? '#27ae60' :
                                   execution.status === 'FAILED' ? '#e74c3c' : '#95a5a6',
                    borderRadius: '4px'
                  }}>
                    {execution.status}
                  </span>
                )}
              </div>
              <div style={{ height: 'calc(100% - 30px)' }}>
                <Terminal logs={logs} />
              </div>
            </div>
          </Split>
        </Split>
      </div>

      {/* New File Modal */}
      {showNewFileModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{ 
            backgroundColor: 'white', 
            padding: '30px', 
            borderRadius: '8px', 
            width: '400px' 
          }}>
            <h3>Create New File</h3>
            <input
              type="text"
              placeholder="filename.js"
              value={newFileName}
              onChange={(e) => setNewFileName(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleCreateFile()}
              style={{
                width: '100%',
                padding: '10px',
                marginTop: '15px',
                marginBottom: '15px',
                fontSize: '14px',
                border: '1px solid #ccc',
                borderRadius: '4px'
              }}
              autoFocus
            />
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button
                onClick={() => {
                  setShowNewFileModal(false);
                  setNewFileName('');
                  setError('');
                }}
                style={{
                  padding: '10px 20px',
                  backgroundColor: '#95a5a6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Cancel
              </button>
              <button
                onClick={handleCreateFile}
                disabled={!newFileName.trim()}
                style={{
                  padding: '10px 20px',
                  backgroundColor: newFileName.trim() ? '#27ae60' : '#95a5a6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: newFileName.trim() ? 'pointer' : 'not-allowed'
                }}
              >
                Create
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
