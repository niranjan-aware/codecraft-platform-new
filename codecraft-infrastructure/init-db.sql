CREATE DATABASE auth_db;
CREATE DATABASE project_db;
CREATE DATABASE execution_db;
CREATE DATABASE analysis_db;
CREATE DATABASE ai_db;

\c auth_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c project_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c execution_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c analysis_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c ai_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
