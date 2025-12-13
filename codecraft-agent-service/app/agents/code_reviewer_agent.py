from app.agents.base_agent import BaseAgent
from typing import Dict, Any

class CodeReviewerAgent(BaseAgent):
    def __init__(self):
        super().__init__("CODE_REVIEWER")
    
    async def execute(self, input_data: Dict[str, Any], context: Dict[str, Any]) -> Dict[str, Any]:
        self.validate_input(input_data, ["code", "language"])
        
        user_id = context.get("user_id")
        
        result = await self.call_ai_service(
            "review",
            {
                "code": input_data["code"],
                "language": input_data["language"],
                "file_path": input_data.get("file_path")
            },
            user_id
        )
        
        return {
            "review": result.get("result"),
            "task_id": result.get("task_id")
        }
