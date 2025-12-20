import React, { useEffect, useState, useRef } from 'react';
import { useParams } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { Terminal } from '@xterm/xterm';
import { FitAddon } from '@xterm/addon-fit';
import '@xterm/xterm/css/xterm.css';
import {
  Play,
  Save,
  FolderTree,
  Terminal as TerminalIcon,
  FileText,
  ExternalLink,
  StopCircle
} from 'lucide-react';
import Header from '../components/Header';
import { projectService } from '../services/projectService';
import { fileService } from '../services/fileService';
import { executionService, ExecutionResponse } from '../services/executionService';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const EditorPage: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const [project, setProject] = useState<any>(null);
  const [files, setFiles] = useState<any[]>([]);
  const [selectedFile, setSelectedFile] = useState<any>(null);
  const [fileContent, setFileContent] = useState('');
  const [saving, setSaving] = useState(false);
  const [running, setRunning] = useState(false);
  const [currentExecution, setCurrentExecution] = useState<ExecutionResponse | null>(null);

  const terminalRef = useRef<HTMLDivElement>(null);
  const xtermRef = useRef<Terminal | null>(null);
  const fitAddonRef = useRef<FitAddon | null>(null);
  const stompClientRef = useRef<Client | null>(null);

  useEffect(() => {
    loadProject();
    loadFiles();
    initializeTerminal();

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
      if (xtermRef.current) {
        xtermRef.current.dispose();
      }
    };
  }, [projectId]);

  const loadProject = async () => {
    try {
      const projectData = await projectService.getProject(projectId!);
      setProject(projectData);
    } catch (error) {
      console.error('Failed to load project:', error);
    }
  };

  const loadFiles = async () => {
    try {
      const filesData = await fileService.listFiles(projectId!);
      setFiles(filesData);
      if (filesData.length > 0 && !selectedFile) {
        loadFile(filesData[0]);
      }
    } catch (error) {
      console.error('Failed to load files:', error);
    }
  };

  const loadFile = async (file: any) => {
    try {
      setSelectedFile(file);
      const content = await fileService.getFileContent(projectId!, file.path);
      setFileContent(content.content);
    } catch (error) {
      console.error('Failed to load file:', error);
    }
  };

  const handleSave = async () => {
    if (!selectedFile) return;

    try {
      setSaving(true);
      await fileService.updateFile(projectId!, selectedFile.path, fileContent);
      xtermRef.current?.writeln('\r\n\x1b[32m✓ File saved successfully\x1b[0m');
    } catch (error) {
      console.error('Failed to save file:', error);
      xtermRef.current?.writeln('\r\n\x1b[31m✗ Failed to save file\x1b[0m');
    } finally {
      setSaving(false);
    }
  };

  const handleRun = async () => {
    try {
      setRunning(true);
      xtermRef.current?.clear();
      xtermRef.current?.writeln('\x1b[36m🚀 Starting execution...\x1b[0m\r\n');

      const execution = await executionService.startExecution({
        projectId: projectId!,
        language: project.language
      });

      setCurrentExecution(execution);
      connectToExecutionLogs(execution.id);
    } catch (error: any) {
      console.error('Failed to start execution:', error);
      xtermRef.current?.writeln('\r\n\x1b[31m✗ Failed to start execution\x1b[0m');
      xtermRef.current?.writeln(`\x1b[31m${error.response?.data?.message || error.message}\x1b[0m`);
      setRunning(false);
    }
  };

  const handleStop = async () => {
    if (!currentExecution) return;

    if (!window.confirm('Are you sure you want to stop this execution?')) {
      return;
    }

    try {
      await executionService.stopExecution(currentExecution.id);
      xtermRef.current?.writeln('\r\n\x1b[33m⚠ Execution stopped by user\x1b[0m');
      setRunning(false);
      setCurrentExecution(null);
    } catch (error) {
      console.error('Failed to stop execution:', error);
      xtermRef.current?.writeln('\r\n\x1b[31m✗ Failed to stop execution\x1b[0m');
    }
  };

  const connectToExecutionLogs = (executionId: string) => {
    const socket = new SockJS('http://localhost:8080/api/executions/ws/execution');
    const client = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('WebSocket connected');
        client.subscribe(`/topic/execution/${executionId}`, (message) => {
          const log = JSON.parse(message.body);
          const color = log.level === 'ERROR' ? '\x1b[31m' : '\x1b[37m';
          xtermRef.current?.writeln(`${color}${log.message}\x1b[0m`);
          
          if (log.level === 'INFO' && log.message.includes('completed successfully')) {
            setRunning(false);
          } else if (log.level === 'ERROR' && log.message.includes('failed')) {
            setRunning(false);
          }
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      }
    });

    client.activate();
    stompClientRef.current = client;
  };

  const initializeTerminal = () => {
    if (terminalRef.current && !xtermRef.current) {
      const term = new Terminal({
        cursorBlink: true,
        fontSize: 14,
        fontFamily: 'Menlo, Monaco, "Courier New", monospace',
        theme: {
          background: '#1e1e1e',
          foreground: '#d4d4d4'
        }
      });

      const fitAddon = new FitAddon();
      term.loadAddon(fitAddon);
      term.open(terminalRef.current);
      fitAddon.fit();

      term.writeln('\x1b[36mCodeCraft Terminal Ready\x1b[0m');
      term.writeln('Click "Run" to execute your code\r\n');

      xtermRef.current = term;
      fitAddonRef.current = fitAddon;

      window.addEventListener('resize', () => fitAddon.fit());
    }
  };

  const getFileIcon = (fileName: string) => {
    return <FileText className="w-4 h-4 text-gray-500" />;
  };

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      <Header />

      <div className="flex-1 flex overflow-hidden">
        {/* File Tree */}
        <div className="w-64 bg-white border-r border-gray-200 overflow-y-auto">
          <div className="p-4 border-b border-gray-200">
            <div className="flex items-center gap-2 text-sm font-medium text-gray-700">
              <FolderTree className="w-4 h-4" />
              <span>{project?.name || 'Project'}</span>
            </div>
            {project?.projectType && (
              <div className="mt-2">
                <span className={`inline-block px-2 py-1 text-xs rounded ${
                  project.projectType === 'SERVER' 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-blue-100 text-blue-800'
                }`}>
                  {project.projectType}
                </span>
              </div>
            )}
          </div>
          <div className="p-2">
            {files.map((file) => (
              <button
                key={file.id}
                onClick={() => loadFile(file)}
                className={`w-full flex items-center gap-2 px-3 py-2 rounded-md text-sm ${
                  selectedFile?.id === file.id
                    ? 'bg-blue-50 text-blue-700'
                    : 'text-gray-700 hover:bg-gray-100'
                }`}
              >
                {getFileIcon(file.path)}
                <span className="truncate">{file.path}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Editor and Terminal */}
        <div className="flex-1 flex flex-col">
          {/* Toolbar */}
          <div className="bg-white border-b border-gray-200 px-4 py-2 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <button
                onClick={handleSave}
                disabled={saving || !selectedFile}
                className="flex items-center gap-2 px-3 py-1.5 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                {saving ? 'Saving...' : 'Save'}
              </button>

              {currentExecution?.status === 'RUNNING' ? (
                <button
                  onClick={handleStop}
                  className="flex items-center gap-2 px-3 py-1.5 text-sm bg-red-600 text-white rounded-md hover:bg-red-700"
                >
                  <StopCircle className="w-4 h-4" />
                  Stop
                </button>
              ) : (
                <button
                  onClick={handleRun}
                  disabled={running}
                  className="flex items-center gap-2 px-3 py-1.5 text-sm bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                >
                  <Play className="w-4 h-4" />
                  {running ? 'Running...' : 'Run'}
                </button>
              )}
            </div>

            {currentExecution?.publicUrl && (
              <a href={currentExecution.publicUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-2 px-3 py-1.5 text-sm bg-purple-600 text-white rounded-md hover:bg-purple-700"
              >
                <ExternalLink className="w-4 h-4" />
                Open App
              </a>
            )}
          </div>

          {/* Editor */}
          <div className="flex-1 bg-[#1e1e1e]">
            <Editor
              height="100%"
              language="javascript"
              theme="vs-dark"
              value={fileContent}
              onChange={(value) => setFileContent(value || '')}
              options={{
                minimap: { enabled: false },
                fontSize: 14,
                lineNumbers: 'on',
                scrollBeyondLastLine: false,
                automaticLayout: true
              }}
            />
          </div>

          {/* Terminal */}
          <div className="h-64 bg-[#1e1e1e] border-t border-gray-700">
            <div className="h-8 bg-[#2d2d2d] border-b border-gray-700 px-4 flex items-center">
              <div className="flex items-center gap-2 text-sm text-gray-400">
                <TerminalIcon className="w-4 h-4" />
                <span>Terminal</span>
              </div>
            </div>
            <div ref={terminalRef} className="h-[calc(100%-2rem)]" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditorPage;
