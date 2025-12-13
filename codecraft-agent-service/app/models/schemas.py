from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from datetime import datetime
from enum import Enum

class AgentType(str, Enum):
    CODE_GENERATOR = "CODE_GENERATOR"
    TEST_GENERATOR = "TEST_GENERATOR"
    BUG_FIXER = "BUG_FIXER"
    CODE_REVIEWER = "CODE_REVIEWER"
    REFACTORER = "REFACTORER"
    DOCUMENTER = "DOCUMENTER"

class WorkflowStatus(str, Enum):
    PENDING = "PENDING"
    IN_PROGRESS = "IN_PROGRESS"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"

class AgentTask(BaseModel):
    agent_type: AgentType
    input_data: Dict[str, Any]
    dependencies: List[str] = []
    timeout: int = 300

class WorkflowRequest(BaseModel):
    project_id: str
    workflow_type: str
    tasks: List[AgentTask]
    context: Optional[Dict[str, Any]] = None

class WorkflowResponse(BaseModel):
    workflow_id: str
    status: WorkflowStatus
    progress: float
    results: Optional[Dict[str, Any]] = None
    error: Optional[str] = None
    created_at: datetime
    updated_at: datetime

class CodeGenerationWorkflow(BaseModel):
    project_id: str
    requirements: str
    language: str
    generate_tests: bool = True
    generate_docs: bool = True

class BugFixWorkflow(BaseModel):
    project_id: str
    file_path: str
    error_message: str
    code: str
    language: str
    generate_tests: bool = True

class FeatureWorkflow(BaseModel):
    project_id: str
    feature_description: str
    language: str
    files_to_modify: List[str] = []
    generate_tests: bool = True
    review_code: bool = True

class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
