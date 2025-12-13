from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime

class CodeFixRequest(BaseModel):
    project_id: str
    file_path: str
    issue_id: str
    code_snippet: str
    issue_description: str
    severity: str

class CodeFixResponse(BaseModel):
    fixed_code: str
    explanation: str
    changes_made: List[str]
    confidence: float

class CodeExplanationRequest(BaseModel):
    project_id: str
    file_path: str
    code_snippet: str
    line_start: Optional[int] = None
    line_end: Optional[int] = None

class CodeExplanationResponse(BaseModel):
    explanation: str
    complexity: str
    suggestions: List[str]

class TestGenerationRequest(BaseModel):
    project_id: str
    file_path: str
    code_snippet: str
    test_framework: Optional[str] = "jest"

class TestGenerationResponse(BaseModel):
    test_code: str
    test_cases: List[Dict[str, str]]
    coverage_percentage: float

class CodeReviewRequest(BaseModel):
    project_id: str
    file_path: str
    code_snippet: str

class CodeReviewResponse(BaseModel):
    issues: List[Dict[str, Any]]
    suggestions: List[str]
    quality_score: float
    refactoring_suggestions: List[str]

class AIInteractionResponse(BaseModel):
    id: str
    user_id: str
    project_id: str
    interaction_type: str
    created_at: datetime
    tokens_used: int
