import React from 'react';

interface TerminalProps {
  projectId: string;
}

const Terminal: React.FC<TerminalProps> = ({ projectId }) => {
  return (
    <div style={{ 
      height: '100%', 
      width: '100%', 
      background: '#1e1e1e',
      color: '#d4d4d4',
      padding: '1rem',
      fontFamily: 'Menlo, Monaco, "Courier New", monospace',
      fontSize: '14px',
      overflow: 'auto'
    }}>
      <div style={{ color: '#4EC9B0' }}>
        CodeCraft Terminal
      </div>
      <div style={{ marginTop: '0.5rem', color: '#888' }}>
        → Terminal ready. Click "Run" to execute code...
      </div>
      <div style={{ marginTop: '1rem', color: '#666' }}>
        (Full terminal integration coming in next phase)
      </div>
    </div>
  );
};

export default Terminal;
