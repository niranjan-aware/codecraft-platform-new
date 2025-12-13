from app.agents.base_agent import BaseAgent
from typing import Dict, Any

class BugFixerAgent(BaseAgent):
    def __init__(self):
        super().__init__("BUG_FIXER")
    
    async def execute(self, input_data: Dict[str, Any], context: Dict[str, Any]) -> Dict[str, Any]:
        self.validate_input(input_data, ["code", "language", "error_message"])
        
        user_id = context.get("user_id")
        
        result = await self.call_ai_service(
            "fix",
            {
                "code": input_data["code"],
                "language": input_data["language"],
                "error_message": input_data["error_message"],
                "stack_trace": input_data.get("stack_trace")
            },
            user_id
        )
        
        return {
            "fixed_code": result.get("result"),
            "task_id": result.get("task_id")
        }
