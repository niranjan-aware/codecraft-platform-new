from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    service_name: str = "notification-service"
    service_port: int = 8087
    database_url: str
    redis_url: str
    rabbitmq_url: str
    smtp_host: str
    smtp_port: int
    smtp_username: str
    smtp_password: str
    from_email: str
    eureka_server: str
    jwt_secret: str
    
    class Config:
        env_file = ".env"

settings = Settings()
