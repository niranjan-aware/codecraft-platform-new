from fastapi import APIRouter
from app.models.schemas import HealthResponse

router = APIRouter(tags=["Health"])

@router.get("/health", response_model=HealthResponse)
async def health_check():
    return HealthResponse(
        status="UP",
        service="ai-service",
        version="1.0.0"
    )

@router.get("/", response_model=HealthResponse)
async def root():
    return HealthResponse(
        status="UP",
        service="ai-service",
        version="1.0.0"
    )
