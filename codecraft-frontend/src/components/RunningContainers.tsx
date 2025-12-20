import React, { useEffect, useState } from 'react';
import { executionService, ExecutionResponse } from '../services/executionService';
import { Play, StopCircle, ExternalLink } from 'lucide-react';

const RunningContainers: React.FC = () => {
  const [runningContainers, setRunningContainers] = useState<ExecutionResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const loadRunningContainers = async () => {
    try {
      setLoading(true);
      const containers = await executionService.getRunningExecutions();
      setRunningContainers(containers);
    } catch (error) {
      console.error('Failed to load running containers:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRunningContainers();
    const interval = setInterval(loadRunningContainers, 10000); // Refresh every 10 seconds
    return () => clearInterval(interval);
  }, []);

  const handleStop = async (executionId: string) => {
    if (!window.confirm('Are you sure you want to stop this container? This action cannot be undone.')) {
      return;
    }

    try {
      await executionService.stopExecution(executionId);
      loadRunningContainers();
    } catch (error) {
      console.error('Failed to stop container:', error);
      alert('Failed to stop container');
    }
  };

  if (runningContainers.length === 0) {
    return null;
  }

  return (
    <div className="bg-green-50 border-b border-green-200 px-4 py-2">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Play className="w-4 h-4 text-green-600" fill="currentColor" />
          <span className="text-sm font-medium text-green-800">
            {runningContainers.length} container{runningContainers.length !== 1 ? 's' : ''} running
          </span>
        </div>
        
        <div className="flex items-center gap-3">
          {runningContainers.slice(0, 3).map((container) => (
            <div key={container.id} className="flex items-center gap-2 bg-white rounded-md px-3 py-1 shadow-sm">
              <span className="text-xs font-medium text-gray-700">
                Port {container.hostPort}
              </span>
              
              {container.publicUrl && (
                
                  href={container.publicUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:text-blue-800"
                  title="Open in new tab"
                >
                  <ExternalLink className="w-3 h-3" />
                </a>
              )}
              
              <button
                onClick={() => handleStop(container.id)}
                className="text-red-600 hover:text-red-800"
                title="Stop container"
              >
                <StopCircle className="w-4 h-4" />
              </button>
            </div>
          ))}
          
          {runningContainers.length > 3 && (
            <span className="text-xs text-gray-600">
              +{runningContainers.length - 3} more
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export default RunningContainers;
