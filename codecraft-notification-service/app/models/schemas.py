from pydantic import BaseModel, EmailStr
from typing import Optional, Dict, Any
from datetime import datetime
from enum import Enum

class NotificationType(str, Enum):
    EMAIL = "EMAIL"
    WEBSOCKET = "WEBSOCKET"
    IN_APP = "IN_APP"

class NotificationStatus(str, Enum):
    PENDING = "PENDING"
    SENT = "SENT"
    FAILED = "FAILED"

class EventType(str, Enum):
    EXECUTION_STARTED = "EXECUTION_STARTED"
    EXECUTION_COMPLETED = "EXECUTION_COMPLETED"
    EXECUTION_FAILED = "EXECUTION_FAILED"
    ANALYSIS_COMPLETED = "ANALYSIS_COMPLETED"
    WORKFLOW_COMPLETED = "WORKFLOW_COMPLETED"
    WORKFLOW_FAILED = "WORKFLOW_FAILED"
    PROJECT_CREATED = "PROJECT_CREATED"
    FILE_UPDATED = "FILE_UPDATED"

class EmailNotification(BaseModel):
    to_email: EmailStr
    subject: str
    body: str
    html: Optional[str] = None

class WebSocketNotification(BaseModel):
    user_id: str
    message: str
    data: Optional[Dict[str, Any]] = None

class InAppNotification(BaseModel):
    user_id: str
    title: str
    message: str
    link: Optional[str] = None
    data: Optional[Dict[str, Any]] = None

class NotificationResponse(BaseModel):
    id: str
    user_id: str
    notification_type: NotificationType
    status: NotificationStatus
    data: Dict[str, Any]
    created_at: datetime
    sent_at: Optional[datetime] = None

class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
