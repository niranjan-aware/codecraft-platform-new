import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { analysisAPI } from '../../services/api';
import { AnalysisReport, CodeIssue, ProjectMetrics, Dependency } from '../../types';
import { ArrowLeft, Play, AlertCircle, CheckCircle, Clock } from 'lucide-react';

export default function AnalysisPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  
  const [reports, setReports] = useState<AnalysisReport[]>([]);
  const [currentReport, setCurrentReport] = useState<AnalysisReport | null>(null);
  const [issues, setIssues] = useState<CodeIssue[]>([]);
  const [metrics, setMetrics] = useState<ProjectMetrics | null>(null);
  const [dependencies, setDependencies] = useState<Dependency[]>([]);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [activeTab, setActiveTab] = useState<'overview' | 'issues' | 'dependencies'>('overview');

  useEffect(() => {
    loadReports();
  }, [projectId]);

  const loadReports = async () => {
    try {
      const response = await analysisAPI.getByProject(projectId!);
      setReports(response.data);
      if (response.data.length > 0) {
        loadReportDetails(response.data[0].id);
      }
    } catch (error) {
      console.error('Failed to load reports', error);
    }
  };

  const loadReportDetails = async (reportId: string) => {
    try {
      const [reportRes, issuesRes, metricsRes, depsRes] = await Promise.all([
        analysisAPI.get(reportId),
        analysisAPI.getIssues(reportId),
        analysisAPI.getMetrics(reportId),
        analysisAPI.getDependencies(reportId)
      ]);

      setCurrentReport(reportRes.data);
      setIssues(issuesRes.data);
      setMetrics(metricsRes.data);
      setDependencies(depsRes.data);
    } catch (error) {
      console.error('Failed to load report details', error);
    }
  };

  const startAnalysis = async () => {
    try {
      setIsAnalyzing(true);
      const response = await analysisAPI.start(projectId!);
      pollAnalysisStatus(response.data.id);
    } catch (error) {
      console.error('Failed to start analysis', error);
      setIsAnalyzing(false);
    }
  };

  const pollAnalysisStatus = async (reportId: string) => {
    const interval = setInterval(async () => {
      try {
        const response = await analysisAPI.get(reportId);
        const report = response.data;

        if (['COMPLETED', 'FAILED'].includes(report.status)) {
          clearInterval(interval);
          setIsAnalyzing(false);
          loadReports();
        }
      } catch (error) {
        clearInterval(interval);
        setIsAnalyzing(false);
      }
    }, 3000);
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'BLOCKER': return '#d32f2f';
      case 'CRITICAL': return '#f44336';
      case 'MAJOR': return '#ff9800';
      case 'MINOR': return '#ffc107';
      case 'INFO': return '#2196f3';
      default: return '#666';
    }
  };

  const getRatingColor = (rating: string) => {
    switch (rating) {
      case 'A': return '#00c853';
      case 'B': return '#64dd17';
      case 'C': return '#ffc107';
      case 'D': return '#ff9800';
      case 'E': return '#f44336';
      default: return '#666';
    }
  };

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
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
            onClick={() => navigate(`/editor/${projectId}`)}
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
            Back to Editor
          </button>
          <h2 style={{ margin: 0 }}>Code Analysis</h2>
        </div>
        
        <button
          onClick={startAnalysis}
          disabled={isAnalyzing}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '5px',
            padding: '8px 16px',
            backgroundColor: isAnalyzing ? '#95a5a6' : '#27ae60',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: isAnalyzing ? 'not-allowed' : 'pointer'
          }}
        >
          <Play size={16} />
          {isAnalyzing ? 'Analyzing...' : 'Run Analysis'}
        </button>
      </div>

      <div style={{ flex: 1, display: 'flex' }}>
        <div style={{
          width: '250px',
          borderRight: '1px solid #ddd',
          padding: '20px',
          overflowY: 'auto',
          backgroundColor: '#f5f5f5'
        }}>
          <h3>Analysis History</h3>
          {reports.map(report => (
            <div
              key={report.id}
              onClick={() => loadReportDetails(report.id)}
              style={{
                padding: '10px',
                marginBottom: '10px',
                backgroundColor: currentReport?.id === report.id ? '#e3f2fd' : 'white',
                borderRadius: '4px',
                cursor: 'pointer',
                border: currentReport?.id === report.id ? '2px solid #2196f3' : '1px solid #ddd'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                {report.status === 'COMPLETED' && <CheckCircle size={16} color="#27ae60" />}
                {report.status === 'RUNNING' && <Clock size={16} color="#f39c12" />}
                {report.status === 'FAILED' && <AlertCircle size={16} color="#e74c3c" />}
                <span style={{ fontSize: '12px', fontWeight: 'bold' }}>{report.status}</span>
              </div>
              <div style={{ fontSize: '11px', color: '#666', marginTop: '5px' }}>
                {new Date(report.startedAt).toLocaleString()}
              </div>
            </div>
          ))}
        </div>

        <div style={{ flex: 1, padding: '20px', overflowY: 'auto' }}>
          {currentReport && currentReport.status === 'COMPLETED' ? (
            <>
              <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', borderBottom: '1px solid #ddd' }}>
                {['overview', 'issues', 'dependencies'].map(tab => (
                  <button
                    key={tab}
                    onClick={() => setActiveTab(tab as any)}
                    style={{
                      padding: '10px 20px',
                      background: 'none',
                      border: 'none',
                      borderBottom: activeTab === tab ? '2px solid #2196f3' : 'none',
                      color: activeTab === tab ? '#2196f3' : '#666',
                      cursor: 'pointer',
                      fontWeight: activeTab === tab ? 'bold' : 'normal'
                    }}
                  >
                    {tab.charAt(0).toUpperCase() + tab.slice(1)}
                  </button>
                ))}
              </div>

              {activeTab === 'overview' && metrics && (
                <div>
                  <h2>Project Metrics</h2>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                    <div style={{ padding: '20px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#2196f3' }}>
                        {metrics.linesOfCode}
                      </div>
                      <div style={{ color: '#666' }}>Lines of Code</div>
                    </div>
                    
                    <div style={{ padding: '20px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#e74c3c' }}>
                        {metrics.vulnerabilities}
                      </div>
                      <div style={{ color: '#666' }}>Vulnerabilities</div>
                    </div>
                    
                    <div style={{ padding: '20px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#f39c12' }}>
                        {metrics.complexityScore.toFixed(1)}
                      </div>
                      <div style={{ color: '#666' }}>Complexity Score</div>
                    </div>
                    
                    <div style={{ padding: '20px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: getRatingColor(metrics.maintainabilityRating) }}>
                        {metrics.maintainabilityRating}
                      </div>
                      <div style={{ color: '#666' }}>Maintainability</div>
                    </div>
                  </div>

                  <h3>Quality Ratings</h3>
                  <div style={{ display: 'flex', gap: '20px', marginBottom: '30px' }}>
                    <div style={{ flex: 1, padding: '15px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                      <div style={{ fontWeight: 'bold', marginBottom: '10px' }}>Reliability</div>
                      <div style={{
                        padding: '10px',
                        backgroundColor: getRatingColor(metrics.reliabilityRating),
                        color: 'white',
                        borderRadius: '4px',
                        textAlign: 'center',
                        fontSize: '24px',
                        fontWeight: 'bold'
                      }}>
                        {metrics.reliabilityRating}
                      </div>
                    </div>

                    <div style={{ flex: 1, padding: '15px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                      <div style={{ fontWeight: 'bold', marginBottom: '10px' }}>Security</div>
                      <div style={{
                        padding: '10px',
                        backgroundColor: getRatingColor(metrics.securityRating),
                        color: 'white',
                        borderRadius: '4px',
                        textAlign: 'center',
                        fontSize: '24px',
                        fontWeight: 'bold'
                      }}>
                        {metrics.securityRating}
                      </div>
                    </div>

                    <div style={{ flex: 1, padding: '15px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                      <div style={{ fontWeight: 'bold', marginBottom: '10px' }}>Maintainability</div>
                      <div style={{
                        padding: '10px',
                        backgroundColor: getRatingColor(metrics.maintainabilityRating),
                        color: 'white',
                        borderRadius: '4px',
                        textAlign: 'center',
                        fontSize: '24px',
                        fontWeight: 'bold'
                      }}>
                        {metrics.maintainabilityRating}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {activeTab === 'issues' && (
                <div>
                  <h2>Security Issues ({issues.length})</h2>
                  {issues.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                      <CheckCircle size={48} color="#27ae60" />
                      <p style={{ marginTop: '20px' }}>No security issues found!</p>
                    </div>
                  ) : (
                    <div>
                      {issues.map(issue => (
                        <div
                          key={issue.id}
                          style={{
                            padding: '15px',
                            marginBottom: '15px',
                            border: `2px solid ${getSeverityColor(issue.severity)}`,
                            borderRadius: '8px',
                            backgroundColor: 'white'
                          }}
                        >
                          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px' }}>
                            <span style={{
                              padding: '4px 8px',
                              backgroundColor: getSeverityColor(issue.severity),
                              color: 'white',
                              borderRadius: '4px',
                              fontSize: '12px',
                              fontWeight: 'bold'
                            }}>
                              {issue.severity}
                            </span>
                            <span style={{ fontSize: '12px', color: '#666' }}>{issue.ruleId}</span>
                          </div>
                          
                          <div style={{ fontWeight: 'bold', marginBottom: '8px' }}>
                            {issue.message}
                          </div>
                          
                          <div style={{ fontSize: '13px', color: '#666', marginBottom: '8px' }}>
                            {issue.filePath}:{issue.lineNumber}
                          </div>

                          {issue.codeSnippet && (
                            <pre style={{
                              padding: '10px',
                              backgroundColor: '#f5f5f5',
                              borderRadius: '4px',
                              overflow: 'auto',
                              fontSize: '12px'
                            }}>
                              {issue.codeSnippet}
                            </pre>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'dependencies' && (
                <div>
                  <h2>Dependencies ({dependencies.length})</h2>
                  {dependencies.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                      <p>No dependencies found</p>
                    </div>
                  ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                      <thead>
                        <tr style={{ backgroundColor: '#f5f5f5' }}>
                          <th style={{ padding: '10px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Name</th>
                          <th style={{ padding: '10px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Version</th>
                          <th style={{ padding: '10px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Type</th>
                          <th style={{ padding: '10px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>License</th>
                        </tr>
                      </thead>
                      <tbody>
                        {dependencies.map(dep => (
                          <tr key={dep.id} style={{ borderBottom: '1px solid #eee' }}>
                            <td style={{ padding: '10px' }}>{dep.name}</td>
                            <td style={{ padding: '10px' }}>{dep.version}</td>
                            <td style={{ padding: '10px' }}>
                              <span style={{
                                padding: '2px 8px',
                                backgroundColor: dep.dependencyType === 'DIRECT' ? '#2196f3' : '#95a5a6',
                                color: 'white',
                                borderRadius: '4px',
                                fontSize: '11px'
                              }}>
                                {dep.dependencyType}
                              </span>
                            </td>
                            <td style={{ padding: '10px' }}>{dep.license || 'Unknown'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              )}
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
              {isAnalyzing ? (
                <>
                  <Clock size={48} color="#f39c12" />
                  <p style={{ marginTop: '20px' }}>Analysis in progress...</p>
                </>
              ) : (
                <p>Run an analysis to see results</p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
