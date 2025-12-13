from sqlalchemy import Column, String, Text, DateTime, Enum as SQLEnum, JSON, Boolean
from app.config.database import Base
from datetime import datetime
import uuid
import enum

class NotificationType(enum.Enum):
    EMAIL = "EMAIL"
    WEBSOCKET = "WEBSOCKET"
    IN_APP = "IN_APP"

class NotificationStatus(enum.Enum):
    PENDING = "PENDING"
    SENT = "SENT"
    FAILED = "FAILED"

class Notification(Base):
    __tablename__ = "notifications"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, nullable=False, index=True)
    notification_type = Column(SQLEnum(NotificationType), nullable=False)
    status = Column(SQLEnum(NotificationStatus), default=NotificationStatus.PENDING, nullable=False)
    data = Column(JSON, nullable=False)
    error = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    sent_at = Column(DateTime, nullable=True)
    read = Column(Boolean, default=False, nullable=False)
    read_at = Column(DateTime, nullable=True)

class Event(Base):
    __tablename__ = "events"
    
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    event_type = Column(String, nullable=False, index=True)
    user_id = Column(String, nullable=False, index=True)
    data = Column(JSON, nullable=False)
    processed = Column(Boolean, default=False, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    processed_at = Column(DateTime, nullable=True)
