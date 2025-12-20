import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { projectAPI } from '../../services/api';
import { setProjects } from '../../store/projectSlice';
import { logout } from '../../store/authSlice';
import { RootState } from '../../store';
import { Project } from '../../types';

export default function DashboardPage() {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const projects = useSelector((state: RootState) => state.project.projects);
  const user = useSelector((state: RootState) => state.auth.user);
  const [showModal, setShowModal] = useState(false);
  const [projectName, setProjectName] = useState('');
  const [language, setLanguage] = useState('NODEJS');

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      const response = await projectAPI.list();
      dispatch(setProjects(response.data));
    } catch (error) {
      console.error('Failed to load projects', error);
    }
  };

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await projectAPI.create({
        name: projectName,
        description: '',
        language,
        visibility: 'PRIVATE'
      });
      
      setShowModal(false);
      setProjectName('');
      loadProjects();
      
      // Navigate to editor
      navigate(`/editor/${response.data.id}`);
    } catch (error) {
      console.error('Failed to create project', error);
    }
  };

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <div style={{ padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '30px' }}>
        <h1>CodeCraft Dashboard</h1>
        <div>
          <span style={{ marginRight: '20px' }}>Welcome, {user?.fullName}</span>
          <button onClick={handleLogout}>Logout</button>
        </div>
      </div>

      <button onClick={() => setShowModal(true)} style={{ marginBottom: '20px', padding: '10px 20px' }}>
        Create New Project
      </button>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
        {projects.map((project: Project) => (
          <div
            key={project.id}
            onClick={() => navigate(`/editor/${project.id}`)}
            style={{
              border: '1px solid #ccc',
              padding: '20px',
              borderRadius: '8px',
              cursor: 'pointer',
              transition: 'box-shadow 0.2s',
            }}
            onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)'}
            onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
          >
            <h3>{project.name}</h3>
            <p>Language: {project.language}</p>
            <p style={{ fontSize: '12px', color: '#666' }}>
              Created: {new Date(project.createdAt).toLocaleDateString()}
            </p>
          </div>
        ))}
      </div>

      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '8px', width: '400px' }}>
            <h2>Create New Project</h2>
            <form onSubmit={handleCreateProject}>
              <div style={{ marginBottom: '15px' }}>
                <input
                  type="text"
                  placeholder="Project Name"
                  value={projectName}
                  onChange={(e) => setProjectName(e.target.value)}
                  required
                  style={{ width: '100%', padding: '10px' }}
                />
              </div>
              <div style={{ marginBottom: '15px' }}>
                <select
                  value={language}
                  onChange={(e) => setLanguage(e.target.value)}
                  style={{ width: '100%', padding: '10px' }}
                >
                  <option value="NODEJS">Node.js</option>
                  <option value="PYTHON">Python</option>
                  <option value="JAVA">Java</option>
                  <option value="HTML_CSS_JS">HTML/CSS/JS</option>
                </select>
              </div>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="submit" style={{ flex: 1, padding: '10px' }}>Create</button>
                <button type="button" onClick={() => setShowModal(false)} style={{ flex: 1, padding: '10px' }}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
