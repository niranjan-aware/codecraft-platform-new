from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.orm import Session
from app.config.database import get_db
from app.models.schemas import *
from app.services.notification_service import NotificationService

router = APIRouter(prefix="/notifications", tags=["Notifications"])

def get_user_id(x_user_id: str = Header(...)) -> str:
    if not x_user_id:
        raise HTTPException(status_code=401, detail="User ID required")
    return x_user_id

@router.post("/email")
async def send_email(
    notification: EmailNotification,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    result = await NotificationService.create_email_notification(
        user_id=user_id,
        to_email=notification.to_email,
        subject=notification.subject,
        body=notification.body,
        html=notification.html,
        db=db
    )
    
    return {"id": result.id, "status": result.status.value}

@router.post("/in-app")
async def send_in_app(
    notification: InAppNotification,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    result = NotificationService.create_in_app_notification(
        user_id=notification.user_id,
        title=notification.title,
        message=notification.message,
        link=notification.link,
        data=notification.data,
        db=db
    )
    
    return {"id": result.id, "status": result.status.value}

@router.get("/", response_model=list[NotificationResponse])
async def get_notifications(
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db),
    limit: int = 20
):
    notifications = NotificationService.get_user_notifications(user_id, db, limit)
    
    return [
        NotificationResponse(
            id=n.id,
            user_id=n.user_id,
            notification_type=n.notification_type,
            status=n.status,
            data=n.data,
            created_at=n.created_at,
            sent_at=n.sent_at
        )
        for n in notifications
    ]

@router.put("/{notification_id}/read")
async def mark_as_read(
    notification_id: str,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    notification = NotificationService.mark_as_read(notification_id, user_id, db)
    
    if not notification:
        raise HTTPException(status_code=404, detail="Notification not found")
    
    return {"id": notification_id, "read": True}

@router.put("/read-all")
async def mark_all_as_read(
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    NotificationService.mark_all_as_read(user_id, db)
    return {"message": "All notifications marked as read"}
