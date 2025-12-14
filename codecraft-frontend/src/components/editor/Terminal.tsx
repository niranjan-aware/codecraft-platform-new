import React, { useEffect, useRef } from 'react';

interface TerminalProps {
  logs: Array<{ level: string; message: string; timestamp: string }>;
}

const Terminal: React.FC<TerminalProps> = ({ logs }) => {
  const terminalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (terminalRef.current) {
      terminalRef.current.scrollTop = terminalRef.current.scrollHeight;
    }
  }, [logs]);

  const getLogColor = (level: string) => {
    switch (level) {
      case 'ERROR': return '#f48771';
      case 'WARN': return '#dcdcaa';
      case 'INFO': return '#4ec9b0';
      default: return '#d4d4d4';
    }
  };

  return (
    <div
      ref={terminalRef}
      style={{
        height: '100%',
        width: '100%',
        background: '#1e1e1e',
        color: '#d4d4d4',
        padding: '1rem',
        fontFamily: 'Menlo, Monaco, "Courier New", monospace',
        fontSize: '14px',
        overflow: 'auto',
        whiteSpace: 'pre-wrap',
        wordBreak: 'break-word'
      }}
    >
      <div style={{ color: '#4EC9B0', marginBottom: '1rem' }}>
        CodeCraft Terminal
      </div>
      
      {logs.length === 0 ? (
        <div style={{ color: '#888' }}>
          → Terminal ready. Click "Run" to execute code...
        </div>
      ) : (
        logs.map((log, index) => (
          <div key={index} style={{ color: getLogColor(log.level) }}>
            {log.message}
          </div>
        ))
      )}
    </div>
  );
};

export default Terminal;
