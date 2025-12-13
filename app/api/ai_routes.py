from fastapi import APIRouter, Depends, Header, HTTPException
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.schemas.ai_schemas import *
from app.services.code_fix_service import CodeFixService
from app.services.code_explanation_service import CodeExplanationService
from app.services.test_generation_service import TestGenerationService
from app.services.code_review_service import CodeReviewService
from app.models.ai_interaction import AIInteraction
from typing import List

router = APIRouter()

code_fix_service = CodeFixService()
explanation_service = CodeExplanationService()
test_service = TestGenerationService()
review_service = CodeReviewService()

def get_user_id(x_user_id: str = Header(...)) -> str:
    return x_user_id

@router.post("/fix", response_model=CodeFixResponse)
async def generate_code_fix(
    request: CodeFixRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    response = await code_fix_service.generate_fix(request)
    
    interaction = AIInteraction(
        user_id=user_id,
        project_id=request.project_id,
        interaction_type="code_fix",
        prompt=request.issue_description,
        response=response.fixed_code,
        model="mixtral-8x7b-32768",
        tokens_used=0
    )
    db.add(interaction)
    db.commit()
    
    return response

@router.post("/explain", response_model=CodeExplanationResponse)
async def explain_code(
    request: CodeExplanationRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    response = await explanation_service.explain_code(request)
    
    interaction = AIInteraction(
        user_id=user_id,
        project_id=request.project_id,
        interaction_type="code_explanation",
        prompt=request.code_snippet[:500],
        response=response.explanation,
        model="mixtral-8x7b-32768",
        tokens_used=0
    )
    db.add(interaction)
    db.commit()
    
    return response

@router.post("/generate-tests", response_model=TestGenerationResponse)
async def generate_tests(
    request: TestGenerationRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    response = await test_service.generate_tests(request)
    
    interaction = AIInteraction(
        user_id=user_id,
        project_id=request.project_id,
        interaction_type="test_generation",
        prompt=request.code_snippet[:500],
        response=response.test_code,
        model="mixtral-8x7b-32768",
        tokens_used=0
    )
    db.add(interaction)
    db.commit()
    
    return response

@router.post("/review", response_model=CodeReviewResponse)
async def review_code(
    request: CodeReviewRequest,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    response = await review_service.review_code(request)
    
    interaction = AIInteraction(
        user_id=user_id,
        project_id=request.project_id,
        interaction_type="code_review",
        prompt=request.code_snippet[:500],
        response=str(response.quality_score),
        model="mixtral-8x7b-32768",
        tokens_used=0
    )
    db.add(interaction)
    db.commit()
    
    return response

@router.get("/history/{project_id}", response_model=List[AIInteractionResponse])
async def get_interaction_history(
    project_id: str,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    interactions = db.query(AIInteraction).filter(
        AIInteraction.user_id == user_id,
        AIInteraction.project_id == project_id
    ).order_by(AIInteraction.created_at.desc()).limit(50).all()
    
    return interactions
