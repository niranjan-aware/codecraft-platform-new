from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.ai_routes import router as ai_router
from app.core.database import engine, Base
from app.core.config import settings
import py_eureka_client.eureka_client as eureka_client

Base.metadata.create_all(bind=engine)

app = FastAPI(title=settings.app_name, version=settings.app_version)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(ai_router, prefix="/api/ai", tags=["AI"])

@app.on_event("startup")
async def startup_event():
    eureka_client.init(
        eureka_server=settings.eureka_server,
        app_name=settings.service_name,
        instance_port=settings.port,
        instance_host="localhost"
    )

@app.get("/health")
async def health_check():
    return {"status": "UP"}

@app.get("/")
async def root():
    return {"service": settings.app_name, "version": settings.app_version}
