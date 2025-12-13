from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import notification_router, health_router
from app.config.database import engine, Base
from app.utils.eureka_client import eureka_client
from app.config.settings import settings
import uvicorn
from contextlib import asynccontextmanager
import threading
from app.consumers.event_consumer import event_consumer

@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    eureka_client.register()
    
    consumer_thread = threading.Thread(target=event_consumer.start, daemon=True)
    consumer_thread.start()
    
    yield

app = FastAPI(
    title="CodeCraft Notification Service",
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
app.include_router(notification_router.router)

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=settings.service_port,
        reload=True
    )
