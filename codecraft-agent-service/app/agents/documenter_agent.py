from app.agents.base_agent import BaseAgent
from typing import Dict, Any

class DocumenterAgent(BaseAgent):
    def __init__(self):
        super().__init__("DOCUMENTER")
    
    async def execute(self, input_data: Dict[str, Any], context: Dict[str, Any]) -> Dict[str, Any]:
        self.validate_input(input_data, ["code", "language"])
        
        user_id = context.get("user_id")
        
        result = await self.call_ai_service(
            "document",
            {
                "code": input_data["code"],
                "language": input_data["language"],
                "doc_style": input_data.get("doc_style")
            },
            user_id
        )
        
        return {
            "documentation": result.get("result"),
            "task_id": result.get("task_id")
        }
