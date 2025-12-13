from abc import ABC, abstractmethod
from typing import Dict, Any
import httpx
from app.config.settings import settings

class BaseAgent(ABC):
    def __init__(self, agent_type: str):
        self.agent_type = agent_type
        self.ai_service_url = settings.ai_service_url
    
    @abstractmethod
    async def execute(self, input_data: Dict[str, Any], context: Dict[str, Any]) -> Dict[str, Any]:
        pass
    
    async def call_ai_service(self, endpoint: str, data: Dict[str, Any], user_id: str) -> Dict[str, Any]:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{self.ai_service_url}/ai/{endpoint}",
                json=data,
                headers={"X-User-Id": user_id},
                timeout=300.0
            )
            response.raise_for_status()
            return response.json()
    
    def validate_input(self, input_data: Dict[str, Any], required_fields: list) -> None:
        missing = [field for field in required_fields if field not in input_data]
        if missing:
            raise ValueError(f"Missing required fields: {', '.join(missing)}")
