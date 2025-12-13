from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    app_name: str = "CodeCraft AI Service"
    app_version: str = "1.0.0"
    host: str = "0.0.0.0"
    port: int = 8085
    
    database_url: str = "postgresql://postgres:postgres@localhost:5432/ai_db"
    redis_url: str = "redis://localhost:6379"
    
    groq_api_key: str
    groq_model: str = "mixtral-8x7b-32768"
    
    eureka_server: str = "http://localhost:8761/eureka"
    service_name: str = "ai-service"
    
    analysis_service_url: str = "http://localhost:8084"
    project_service_url: str = "http://localhost:8082"
    
    max_tokens: int = 4096
    temperature: float = 0.7
    
    class Config:
        env_file = ".env"

settings = Settings()
