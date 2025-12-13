from sqlalchemy import Column, String, Text, DateTime, Enum as SQLEnum
from app.config.database import Base
from datetime import datetime
import uuid
import enum

class TaskType(enum.Enum):
    CODE_GENERATION = "CODE_GENERATION"
    CODE_EXPLANATION = "CODE_EXPLANATION"
    CODE_REVIEW = "CODE_REVIEW"
    BUG_FIX = "BUG_FIX"
    TEST_GENERATION = "TEST_GENERATION"
    CODE_REFACTOR = "CODE_REFACTOR"
    DOCUMENTATION = "DOCUMENTATION"

class TaskStatus(enum.Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"

class AITask(Base):
    __tablename__ = "ai_tasks"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, nullable=False, index=True)
    project_id = Column(String, nullable=True, index=True)
    task_type = Column(SQLEnum(TaskType), nullable=False)
    status = Column(SQLEnum(TaskStatus), default=TaskStatus.PENDING, nullable=False)
    input_data = Column(Text, nullable=False)
    result = Column(Text, nullable=True)
    error = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    completed_at = Column(DateTime, nullable=True)
    model_name = Column(String, nullable=True)
    tokens_used = Column(String, nullable=True)
