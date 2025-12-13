from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from enum import Enum

class TaskType(str, Enum):
    CODE_GENERATION = "CODE_GENERATION"
    CODE_EXPLANATION = "CODE_EXPLANATION"
    CODE_REVIEW = "CODE_REVIEW"
    BUG_FIX = "BUG_FIX"
    TEST_GENERATION = "TEST_GENERATION"
    CODE_REFACTOR = "CODE_REFACTOR"
    DOCUMENTATION = "DOCUMENTATION"

class TaskStatus(str, Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"

class CodeGenerationRequest(BaseModel):
    prompt: str
    language: str
    context: Optional[str] = None
    project_id: Optional[str] = None

class CodeExplanationRequest(BaseModel):
    code: str
    language: str

class CodeReviewRequest(BaseModel):
    code: str
    language: str
    file_path: Optional[str] = None

class BugFixRequest(BaseModel):
    code: str
    language: str
    error_message: str
    stack_trace: Optional[str] = None

class TestGenerationRequest(BaseModel):
    code: str
    language: str
    test_framework: Optional[str] = None

class RefactorRequest(BaseModel):
    code: str
    language: str
    refactor_type: str

class DocumentationRequest(BaseModel):
    code: str
    language: str
    doc_style: Optional[str] = None

class AITaskResponse(BaseModel):
    task_id: str
    task_type: TaskType
    status: TaskStatus
    result: Optional[str] = None
    error: Optional[str] = None
    created_at: datetime
    completed_at: Optional[datetime] = None

class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
