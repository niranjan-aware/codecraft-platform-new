from sqlalchemy.orm import Session
from app.models.database_models import Notification, NotificationType, NotificationStatus
from app.services.email_service import email_service
from datetime import datetime

class NotificationService:
    
    @staticmethod
    async def create_email_notification(
        user_id: str,
        to_email: str,
        subject: str,
        body: str,
        html: str,
        db: Session
    ) -> Notification:
        notification = Notification(
            user_id=user_id,
            notification_type=NotificationType.EMAIL,
            status=NotificationStatus.PENDING,
            data={
                "to_email": to_email,
                "subject": subject,
                "body": body,
                "html": html
            }
        )
        db.add(notification)
        db.commit()
        db.refresh(notification)
        
        success = await email_service.send_email(to_email, subject, body, html)
        
        if success:
            notification.status = NotificationStatus.SENT
            notification.sent_at = datetime.utcnow()
        else:
            notification.status = NotificationStatus.FAILED
            notification.error = "Failed to send email"
        
        db.commit()
        return notification
    
    @staticmethod
    def create_in_app_notification(
        user_id: str,
        title: str,
        message: str,
        link: str,
        data: dict,
        db: Session
    ) -> Notification:
        notification = Notification(
            user_id=user_id,
            notification_type=NotificationType.IN_APP,
            status=NotificationStatus.SENT,
            data={
                "title": title,
                "message": message,
                "link": link,
                "data": data
            },
            sent_at=datetime.utcnow()
        )
        db.add(notification)
        db.commit()
        db.refresh(notification)
        return notification
    
    @staticmethod
    def get_user_notifications(user_id: str, db: Session, limit: int = 20):
        return db.query(Notification).filter(
            Notification.user_id == user_id
        ).order_by(Notification.created_at.desc()).limit(limit).all()
    
    @staticmethod
    def mark_as_read(notification_id: str, user_id: str, db: Session):
        notification = db.query(Notification).filter(
            Notification.id == notification_id,
            Notification.user_id == user_id
        ).first()
        
        if notification:
            notification.read = True
            notification.read_at = datetime.utcnow()
            db.commit()
        
        return notification
    
    @staticmethod
    def mark_all_as_read(user_id: str, db: Session):
        db.query(Notification).filter(
            Notification.user_id == user_id,
            Notification.read == False
        ).update({
            "read": True,
            "read_at": datetime.utcnow()
        })
        db.commit()
