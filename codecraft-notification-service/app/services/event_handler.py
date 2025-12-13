from sqlalchemy.orm import Session
from app.models.database_models import Event
from app.services.notification_service import NotificationService
from datetime import datetime
import json

class EventHandler:
    
    @staticmethod
    async def handle_execution_completed(event_data: dict, db: Session):
        user_id = event_data.get("user_id")
        project_id = event_data.get("project_id")
        execution_id = event_data.get("execution_id")
        
        await NotificationService.create_email_notification(
            user_id=user_id,
            to_email=event_data.get("user_email"),
            subject="Code Execution Completed",
            body=f"Your code execution for project {project_id} has completed successfully.",
            html=f"<p>Your code execution has completed.</p><p>Execution ID: {execution_id}</p>",
            db=db
        )
        
        NotificationService.create_in_app_notification(
            user_id=user_id,
            title="Execution Completed",
            message=f"Your code execution has completed successfully",
            link=f"/projects/{project_id}/executions/{execution_id}",
            data=event_data,
            db=db
        )
    
    @staticmethod
    async def handle_analysis_completed(event_data: dict, db: Session):
        user_id = event_data.get("user_id")
        project_id = event_data.get("project_id")
        report_id = event_data.get("report_id")
        
        NotificationService.create_in_app_notification(
            user_id=user_id,
            title="Code Analysis Complete",
            message=f"Your code analysis has completed",
            link=f"/analysis/{project_id}",
            data=event_data,
            db=db
        )
    
    @staticmethod
    async def handle_workflow_completed(event_data: dict, db: Session):
        user_id = event_data.get("user_id")
        workflow_id = event_data.get("workflow_id")
        
        NotificationService.create_in_app_notification(
            user_id=user_id,
            title="Workflow Completed",
            message=f"Your AI workflow has completed successfully",
            link=f"/workflows/{workflow_id}",
            data=event_data,
            db=db
        )
    
    @staticmethod
    async def handle_event(event_type: str, event_data: dict, db: Session):
        event = Event(
            event_type=event_type,
            user_id=event_data.get("user_id"),
            data=event_data
        )
        db.add(event)
        db.commit()
        
        handlers = {
            "execution.completed": EventHandler.handle_execution_completed,
            "analysis.completed": EventHandler.handle_analysis_completed,
            "workflow.completed": EventHandler.handle_workflow_completed
        }
        
        handler = handlers.get(event_type)
        if handler:
            await handler(event_data, db)
        
        event.processed = True
        event.processed_at = datetime.utcnow()
        db.commit()

event_handler = EventHandler()
