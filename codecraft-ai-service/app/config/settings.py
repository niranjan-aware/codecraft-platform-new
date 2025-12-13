from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    service_name: str = "ai-service"
    service_port: int = 8085
    database_url: str
    redis_url: str
    rabbitmq_url: str
    minio_endpoint: str
    minio_access_key: str
    minio_secret_key: str
    minio_bucket: str
    groq_api_key: str
    langfuse_public_key: Optional[str] = None
    langfuse_secret_key: Optional[str] = None
    langfuse_host: Optional[str] = None
    eureka_server: str
    jwt_secret: str
    
    class Config:
        env_file = ".env"

settings = Settings()
