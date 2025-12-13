from sqlalchemy.orm import Session
from app.models.database_models import Workflow, WorkflowStatus
from app.models.schemas import WorkflowRequest, WorkflowResponse
from app.orchestrator.workflow_engine import WorkflowEngine
from app.orchestrator.workflow_templates import WorkflowTemplates
from datetime import datetime
import asyncio

class WorkflowService:
    
    @staticmethod
    async def create_workflow(
        user_id: str,
        project_id: str,
        workflow_type: str,
        tasks: list,
        context: dict,
        db: Session
    ) -> Workflow:
        workflow = Workflow(
            user_id=user_id,
            project_id=project_id,
            workflow_type=workflow_type,
            status=WorkflowStatus.PENDING,
            context=context
        )
        db.add(workflow)
        db.commit()
        db.refresh(workflow)
        
        engine = WorkflowEngine(db)
        
        asyncio.create_task(
            engine.execute_workflow(
                workflow.id,
                tasks,
                {**context, "user_id": user_id, "project_id": project_id}
            )
        )
        
        return workflow
    
    @staticmethod
    def get_workflow(workflow_id: str, user_id: str, db: Session) -> Workflow:
        return db.query(Workflow).filter(
            Workflow.id == workflow_id,
            Workflow.user_id == user_id
        ).first()
    
    @staticmethod
    def list_workflows(user_id: str, project_id: str, db: Session, limit: int = 20):
        query = db.query(Workflow).filter(Workflow.user_id == user_id)
        
        if project_id:
            query = query.filter(Workflow.project_id == project_id)
        
        return query.order_by(Workflow.created_at.desc()).limit(limit).all()
    
    @staticmethod
    def cancel_workflow(workflow_id: str, user_id: str, db: Session) -> Workflow:
        workflow = db.query(Workflow).filter(
            Workflow.id == workflow_id,
            Workflow.user_id == user_id
        ).first()
        
        if workflow and workflow.status in [WorkflowStatus.PENDING, WorkflowStatus.IN_PROGRESS]:
            workflow.status = WorkflowStatus.CANCELLED
            db.commit()
        
        return workflow
