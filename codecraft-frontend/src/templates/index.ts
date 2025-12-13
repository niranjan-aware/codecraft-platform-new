import { ProjectTemplate } from '../types';

export const projectTemplates: ProjectTemplate[] = [
  {
    id: 'nodejs-express',
    name: 'Node.js + Express',
    description: 'RESTful API with Express.js',
    language: 'javascript',
    files: [
      {
        path: 'server.js',
        content: `const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

app.get('/', (req, res) => {
  res.json({ message: 'Welcome to Express API' });
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(\`Server running on port \${PORT}\`);
});
`
      },
      {
        path: 'package.json',
        content: `{
  "name": "express-api",
  "version": "1.0.0",
  "description": "Express.js REST API",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  },
  "dependencies": {
    "express": "^4.18.2"
  },
  "devDependencies": {
    "nodemon": "^3.0.1"
  }
}
`
      },
      {
        path: '.gitignore',
        content: `node_modules/
.env
*.log
`
      },
      {
        path: 'README.md',
        content: `# Express API

## Getting Started

\`\`\`bash
npm install
npm start
\`\`\`

## API Endpoints

- GET / - Welcome message
- GET /api/health - Health check
`
      }
    ]
  },
  {
    id: 'react-vite',
    name: 'React + Vite',
    description: 'Modern React app with Vite',
    language: 'javascript',
    files: [
      {
        path: 'src/main.jsx',
        content: `import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
`
      },
      {
        path: 'src/App.jsx',
        content: `import { useState } from 'react'
import './App.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div className="App">
      <h1>React + Vite</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
      </div>
    </div>
  )
}

export default App
`
      },
      {
        path: 'src/App.css',
        content: `.App {
  text-align: center;
  padding: 2rem;
}

.card {
  padding: 2rem;
}

button {
  padding: 0.6em 1.2em;
  font-size: 1em;
  font-weight: 500;
  cursor: pointer;
}
`
      },
      {
        path: 'src/index.css',
        content: `* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: system-ui, -apple-system, sans-serif;
  line-height: 1.6;
}
`
      },
      {
        path: 'index.html',
        content: `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>React App</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
`
      },
      {
        path: 'package.json',
        content: `{
  "name": "react-vite-app",
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.2.0",
    "vite": "^5.0.0"
  }
}
`
      },
      {
        path: 'vite.config.js',
        content: `import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})
`
      }
    ]
  },
  {
    id: 'python-flask',
    name: 'Python + Flask',
    description: 'Flask REST API',
    language: 'python',
    files: [
      {
        path: 'app.py',
        content: `from flask import Flask, jsonify
from datetime import datetime

app = Flask(__name__)

@app.route('/')
def home():
    return jsonify({'message': 'Welcome to Flask API'})

@app.route('/api/health')
def health():
    return jsonify({
        'status': 'OK',
        'timestamp': datetime.utcnow().isoformat()
    })

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
`
      },
      {
        path: 'requirements.txt',
        content: `Flask==3.0.0
python-dotenv==1.0.0
`
      },
      {
        path: '.gitignore',
        content: `__pycache__/
*.pyc
.env
venv/
`
      },
      {
        path: 'README.md',
        content: `# Flask API

## Setup

\`\`\`bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
\`\`\`

## Run

\`\`\`bash
python app.py
\`\`\`
`
      }
    ]
  }
];

export const getTemplate = (templateId: string): ProjectTemplate | undefined => {
  return projectTemplates.find(t => t.id === templateId);
};

export const getTemplatesByLanguage = (language: string): ProjectTemplate[] => {
  return projectTemplates.filter(t => t.language === language);
};
