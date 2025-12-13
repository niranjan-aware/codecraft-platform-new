from app.agents.base_agent import BaseAgent
from typing import Dict, Any

class RefactorerAgent(BaseAgent):
    def __init__(self):
        super().__init__("REFACTORER")
    
    async def execute(self, input_data: Dict[str, Any], context: Dict[str, Any]) -> Dict[str, Any]:
        self.validate_input(input_data, ["code", "language", "refactor_type"])
        
        user_id = context.get("user_id")
        
        result = await self.call_ai_service(
            "refactor",
            {
                "code": input_data["code"],
                "language": input_data["language"],
                "refactor_type": input_data["refactor_type"]
            },
            user_id
        )
        
        return {
            "refactored_code": result.get("result"),
            "task_id": result.get("task_id")
        }
