from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    service_name: str = "agent-service"
    service_port: int = 8086
    database_url: str
    redis_url: str
    rabbitmq_url: str
    ai_service_url: str
    project_service_url: str
    analysis_service_url: str
    eureka_server: str
    jwt_secret: str
    
    class Config:
        env_file = ".env"

settings = Settings()
