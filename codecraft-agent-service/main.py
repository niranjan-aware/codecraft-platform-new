from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import workflow_router, health_router
from app.config.database import engine, Base
from app.utils.eureka_client import eureka_client
from app.config.settings import settings
import uvicorn
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    eureka_client.register()
    yield

app = FastAPI(
    title="CodeCraft Agent Service",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health_router.router)
app.include_router(workflow_router.router)

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=settings.service_port,
        reload=True
    )
