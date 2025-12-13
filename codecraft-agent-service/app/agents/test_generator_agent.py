from app.agents.base_agent import BaseAgent
from typing import Dict, Any

class TestGeneratorAgent(BaseAgent):
    def __init__(self):
        super().__init__("TEST_GENERATOR")
    
    async def execute(self, input_data: Dict[str, Any], context: Dict[str, Any]) -> Dict[str, Any]:
        self.validate_input(input_data, ["code", "language"])
        
        user_id = context.get("user_id")
        
        result = await self.call_ai_service(
            "test",
            {
                "code": input_data["code"],
                "language": input_data["language"],
                "test_framework": input_data.get("test_framework")
            },
            user_id
        )
        
        return {
            "tests": result.get("result"),
            "task_id": result.get("task_id")
        }
