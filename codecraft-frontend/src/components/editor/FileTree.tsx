import React from 'react';
import { FileNode } from '../../types';
import { File, Folder, ChevronRight, ChevronDown } from 'lucide-react';

interface FileTreeProps {
  files: FileNode[];
  onFileSelect: (path: string) => void;
}

const FileTree: React.FC<FileTreeProps> = ({ files, onFileSelect }) => {
  const [expanded, setExpanded] = React.useState<Set<string>>(new Set());

  const toggleExpand = (path: string) => {
    const newExpanded = new Set(expanded);
    if (newExpanded.has(path)) {
      newExpanded.delete(path);
    } else {
      newExpanded.add(path);
    }
    setExpanded(newExpanded);
  };

  const renderNode = (node: FileNode, level: number = 0) => {
    if (!node) return null;
    
    const isExpanded = expanded.has(node.path);
    const hasChildren = node.children && node.children.length > 0;

    return (
      <div key={node.path}>
        <div
          onClick={() => {
            if (node.type === 'directory') {
              toggleExpand(node.path);
            } else {
              onFileSelect(node.path);
            }
          }}
          style={{
            padding: '0.5rem',
            paddingLeft: `${level * 1 + 0.5}rem`,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            color: '#cccccc',
            fontSize: '0.9rem',
            transition: 'background 0.2s'
          }}
          onMouseOver={(e) => e.currentTarget.style.background = '#2a2d2e'}
          onMouseOut={(e) => e.currentTarget.style.background = 'transparent'}
        >
          {node.type === 'directory' ? (
            <>
              {isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
              <Folder size={16} color="#dcb67a" />
            </>
          ) : (
            <>
              <span style={{ width: '16px' }} />
              <File size={16} color="#519aba" />
            </>
          )}
          <span>{node.name}</span>
        </div>
        {node.type === 'directory' && isExpanded && hasChildren && (
          <div>
            {node.children!.map(child => renderNode(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  if (!files || files.length === 0) {
    return (
      <div style={{ padding: '1rem', color: '#888', textAlign: 'center' }}>
        No files yet
      </div>
    );
  }

  return (
    <div>
      {files.map(node => renderNode(node))}
    </div>
  );
};

export default FileTree;
