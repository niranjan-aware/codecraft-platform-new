from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.orm import Session
from app.config.database import get_db
from app.models.schemas import *
from app.models.database_models import AITask, TaskStatus, TaskType as DBTaskType
from app.services.code_generation_service import code_generation_service
from app.services.code_explanation_service import code_explanation_service
from app.services.code_review_service import code_review_service
from app.services.bug_fix_service import bug_fix_service
from app.services.test_generation_service import test_generation_service
from app.services.refactor_service import refactor_service
from app.services.documentation_service import documentation_service
from datetime import datetime
import json

router = APIRouter(prefix="/ai", tags=["AI"])

def get_user_id(x_user_id: str = Header(...)) -> str:
    if not x_user_id:
        raise HTTPException(status_code=401, detail="User ID required")
    return x_user_id

@router.post("/generate", response_model=AITaskResponse)
async def generate_code(
    request: CodeGenerationRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    task = AITask(
        user_id=user_id,
        project_id=request.project_id,
        task_type=DBTaskType.CODE_GENERATION,
        status=TaskStatus.PROCESSING,
        input_data=request.json()
    )
    db.add(task)
    db.commit()
    
    try:
        response = code_generation_service.generate_code(request)
        task.result = response["content"]
        task.status = TaskStatus.COMPLETED
        task.completed_at = datetime.utcnow()
        task.model_name = response["model"]
        task.tokens_used = json.dumps(response["tokens"])
        db.commit()
        
        return AITaskResponse(
            task_id=task.id,
            task_type=TaskType.CODE_GENERATION,
            status=TaskStatus.COMPLETED,
            result=task.result,
            created_at=task.created_at,
            completed_at=task.completed_at
        )
    except Exception as e:
        task.status = TaskStatus.FAILED
        task.error = str(e)
        task.completed_at = datetime.utcnow()
        db.commit()
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/explain", response_model=AITaskResponse)
async def explain_code(
    request: CodeExplanationRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    task = AITask(
        user_id=user_id,
        task_type=DBTaskType.CODE_EXPLANATION,
        status=TaskStatus.PROCESSING,
        input_data=request.json()
    )
    db.add(task)
    db.commit()
    
    try:
        response = code_explanation_service.explain_code(request)
        task.result = response["content"]
        task.status = TaskStatus.COMPLETED
        task.completed_at = datetime.utcnow()
        task.model_name = response["model"]
        task.tokens_used = json.dumps(response["tokens"])
        db.commit()
        
        return AITaskResponse(
            task_id=task.id,
            task_type=TaskType.CODE_EXPLANATION,
            status=TaskStatus.COMPLETED,
            result=task.result,
            created_at=task.created_at,
            completed_at=task.completed_at
        )
    except Exception as e:
        task.status = TaskStatus.FAILED
        task.error = str(e)
        task.completed_at = datetime.utcnow()
        db.commit()
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/review", response_model=AITaskResponse)
async def review_code(
    request: CodeReviewRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    task = AITask(
        user_id=user_id,
        task_type=DBTaskType.CODE_REVIEW,
        status=TaskStatus.PROCESSING,
        input_data=request.json()
    )
    db.add(task)
    db.commit()
    
    try:
        response = code_review_service.review_code(request)
        task.result = response["content"]
        task.status = TaskStatus.COMPLETED
        task.completed_at = datetime.utcnow()
        task.model_name = response["model"]
        task.tokens_used = json.dumps(response["tokens"])
        db.commit()
        
        return AITaskResponse(
            task_id=task.id,
            task_type=TaskType.CODE_REVIEW,
            status=TaskStatus.COMPLETED,
            result=task.result,
            created_at=task.created_at,
            completed_at=task.completed_at
        )
    except Exception as e:
        task.status = TaskStatus.FAILED
        task.error = str(e)
        task.completed_at = datetime.utcnow()
        db.commit()
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/fix", response_model=AITaskResponse)
async def fix_bug(
    request: BugFixRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    task = AITask(
        user_id=user_id,
        task_type=DBTaskType.BUG_FIX,
        status=TaskStatus.PROCESSING,
        input_data=request.json()
    )
    db.add(task)
    db.commit()
    
    try:
        response = bug_fix_service.fix_bug(request)
        task.result = response["content"]
        task.status = TaskStatus.COMPLETED
        task.completed_at = datetime.utcnow()
        task.model_name = response["model"]
        task.tokens_used = json.dumps(response["tokens"])
        db.commit()
        
        return AITaskResponse(
            task_id=task.id,
            task_type=TaskType.BUG_FIX,
            status=TaskStatus.COMPLETED,
            result=task.result,
            created_at=task.created_at,
            completed_at=task.completed_at
        )
    except Exception as e:
        task.status = TaskStatus.FAILED
        task.error = str(e)
        task.completed_at = datetime.utcnow()
        db.commit()
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/test", response_model=AITaskResponse)
async def generate_tests(
    request: TestGenerationRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    task = AITask(
        user_id=user_id,
        task_type=DBTaskType.TEST_GENERATION,
        status=TaskStatus.PROCESSING,
        input_data=request.json()
    )
    db.add(task)
    db.commit()
    
    try:
        response = test_generation_service.generate_tests(request)
        task.result = response["content"]
        task.status = TaskStatus.COMPLETED
        task.completed_at = datetime.utcnow()
        task.model_name = response["model"]
        task.tokens_used = json.dumps(response["tokens"])
        db.commit()
        
        return AITaskResponse(
            task_id=task.id,
            task_type=TaskType.TEST_GENERATION,
            status=TaskStatus.COMPLETED,
            result=task.result,
            created_at=task.created_at,
            completed_at=task.completed_at
        )
    except Exception as e:
        task.status = TaskStatus.FAILED
        task.error = str(e)
        task.completed_at = datetime.utcnow()
        db.commit()
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/refactor", response_model=AITaskResponse)
async def refactor_code(
    request: RefactorRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    task = AITask(
        user_id=user_id,
        task_type=DBTaskType.CODE_REFACTOR,
        status=TaskStatus.PROCESSING,
        input_data=request.json()
    )
    db.add(task)
    db.commit()
    
    try:
        response = refactor_service.refactor_code(request)
        task.result = response["content"]
        task.status = TaskStatus.COMPLETED
        task.completed_at = datetime.utcnow()
        task.model_name = response["model"]
        task.tokens_used = json.dumps(response["tokens"])
        db.commit()
        
        return AITaskResponse(
            task_id=task.id,
            task_type=TaskType.CODE_REFACTOR,
            status=TaskStatus.COMPLETED,
            result=task.result,
            created_at=task.created_at,
            completed_at=task.completed_at
        )
    except Exception as e:
        task.status = TaskStatus.FAILED
        task.error = str(e)
        task.completed_at = datetime.utcnow()
        db.commit()
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/document", response_model=AITaskResponse)
async def generate_documentation(
    request: DocumentationRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    task = AITask(
        user_id=user_id,
        task_type=DBTaskType.DOCUMENTATION,
        status=TaskStatus.PROCESSING,
        input_data=request.json()
    )
    db.add(task)
    db.commit()
    
    try:
        response = documentation_service.generate_documentation(request)
        task.result = response["content"]
        task.status = TaskStatus.COMPLETED
        task.completed_at = datetime.utcnow()
        task.model_name = response["model"]
        task.tokens_used = json.dumps(response["tokens"])
        db.commit()
        
        return AITaskResponse(
            task_id=task.id,
            task_type=TaskType.DOCUMENTATION,
            status=TaskStatus.COMPLETED,
            result=task.result,
            created_at=task.created_at,
            completed_at=task.completed_at
        )
    except Exception as e:
        task.status = TaskStatus.FAILED
        task.error = str(e)
        task.completed_at = datetime.utcnow()
        db.commit()
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/task/{task_id}", response_model=AITaskResponse)
async def get_task(
    task_id: str,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    task = db.query(AITask).filter(
        AITask.id == task_id,
        AITask.user_id == user_id
    ).first()
    
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    
    return AITaskResponse(
        task_id=task.id,
        task_type=TaskType[task.task_type.name],
        status=TaskStatus[task.status.name],
        result=task.result,
        error=task.error,
        created_at=task.created_at,
        completed_at=task.completed_at
    )

@router.get("/tasks", response_model=list[AITaskResponse])
async def get_user_tasks(
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db),
    limit: int = 20
):
    tasks = db.query(AITask).filter(
        AITask.user_id == user_id
    ).order_by(AITask.created_at.desc()).limit(limit).all()
    
    return [
        AITaskResponse(
            task_id=task.id,
            task_type=TaskType[task.task_type.name],
            status=TaskStatus[task.status.name],
            result=task.result,
            error=task.error,
            created_at=task.created_at,
            completed_at=task.completed_at
        )
        for task in tasks
    ]
