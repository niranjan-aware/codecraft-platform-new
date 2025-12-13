from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.orm import Session
from app.config.database import get_db
from app.models.schemas import *
from app.services.workflow_service import WorkflowService
from app.orchestrator.workflow_templates import WorkflowTemplates
from typing import Optional

router = APIRouter(prefix="/workflows", tags=["Workflows"])

def get_user_id(x_user_id: str = Header(...)) -> str:
    if not x_user_id:
        raise HTTPException(status_code=401, detail="User ID required")
    return x_user_id

@router.post("/code-generation", response_model=WorkflowResponse)
async def create_code_generation_workflow(
    request: CodeGenerationWorkflow,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    tasks = WorkflowTemplates.code_generation_workflow(
        request.requirements,
        request.language,
        request.generate_tests,
        request.generate_docs
    )
    
    workflow = await WorkflowService.create_workflow(
        user_id=user_id,
        project_id=request.project_id,
        workflow_type="CODE_GENERATION",
        tasks=tasks,
        context={"requirements": request.requirements},
        db=db
    )
    
    return WorkflowResponse(
        workflow_id=workflow.id,
        status=workflow.status,
        progress=workflow.progress,
        results=workflow.results,
        error=workflow.error,
        created_at=workflow.created_at,
        updated_at=workflow.updated_at
    )

@router.post("/bug-fix", response_model=WorkflowResponse)
async def create_bug_fix_workflow(
    request: BugFixWorkflow,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    tasks = WorkflowTemplates.bug_fix_workflow(
        request.code,
        request.language,
        request.error_message,
        "",
        request.generate_tests
    )
    
    workflow = await WorkflowService.create_workflow(
        user_id=user_id,
        project_id=request.project_id,
        workflow_type="BUG_FIX",
        tasks=tasks,
        context={"file_path": request.file_path},
        db=db
    )
    
    return WorkflowResponse(
        workflow_id=workflow.id,
        status=workflow.status,
        progress=workflow.progress,
        results=workflow.results,
        error=workflow.error,
        created_at=workflow.created_at,
        updated_at=workflow.updated_at
    )

@router.post("/feature", response_model=WorkflowResponse)
async def create_feature_workflow(
    request: FeatureWorkflow,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    tasks = WorkflowTemplates.feature_workflow(
        request.feature_description,
        request.language,
        request.generate_tests,
        request.review_code
    )
    
    workflow = await WorkflowService.create_workflow(
        user_id=user_id,
        project_id=request.project_id,
        workflow_type="FEATURE",
        tasks=tasks,
        context={"files_to_modify": request.files_to_modify},
        db=db
    )
    
    return WorkflowResponse(
        workflow_id=workflow.id,
        status=workflow.status,
        progress=workflow.progress,
        results=workflow.results,
        error=workflow.error,
        created_at=workflow.created_at,
        updated_at=workflow.updated_at
    )

@router.post("/custom", response_model=WorkflowResponse)
async def create_custom_workflow(
    request: WorkflowRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    workflow = await WorkflowService.create_workflow(
        user_id=user_id,
        project_id=request.project_id,
        workflow_type=request.workflow_type,
        tasks=request.tasks,
        context=request.context or {},
        db=db
    )
    
    return WorkflowResponse(
        workflow_id=workflow.id,
        status=workflow.status,
        progress=workflow.progress,
        results=workflow.results,
        error=workflow.error,
        created_at=workflow.created_at,
        updated_at=workflow.updated_at
    )

@router.get("/{workflow_id}", response_model=WorkflowResponse)
async def get_workflow(
    workflow_id: str,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    workflow = WorkflowService.get_workflow(workflow_id, user_id, db)
    
    if not workflow:
        raise HTTPException(status_code=404, detail="Workflow not found")
    
    return WorkflowResponse(
        workflow_id=workflow.id,
        status=workflow.status,
        progress=workflow.progress,
        results=workflow.results,
        error=workflow.error,
        created_at=workflow.created_at,
        updated_at=workflow.updated_at
    )

@router.get("/", response_model=list[WorkflowResponse])
async def list_workflows(
    project_id: Optional[str] = None,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db),
    limit: int = 20
):
    workflows = WorkflowService.list_workflows(user_id, project_id, db, limit)
    
    return [
        WorkflowResponse(
            workflow_id=w.id,
            status=w.status,
            progress=w.progress,
            results=w.results,
            error=w.error,
            created_at=w.created_at,
            updated_at=w.updated_at
        )
        for w in workflows
    ]

@router.delete("/{workflow_id}")
async def cancel_workflow(
    workflow_id: str,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    workflow = WorkflowService.cancel_workflow(workflow_id, user_id, db)
    
    if not workflow:
        raise HTTPException(status_code=404, detail="Workflow not found")
    
    return {"message": "Workflow cancelled", "workflow_id": workflow_id}
