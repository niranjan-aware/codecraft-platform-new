from app.agents.base_agent import BaseAgent
from typing import Dict, Any

class CodeGeneratorAgent(BaseAgent):
    def __init__(self):
        super().__init__("CODE_GENERATOR")
    
    async def execute(self, input_data: Dict[str, Any], context: Dict[str, Any]) -> Dict[str, Any]:
        self.validate_input(input_data, ["prompt", "language"])
        
        user_id = context.get("user_id")
        project_id = input_data.get("project_id")
        
        result = await self.call_ai_service(
            "generate",
            {
                "prompt": input_data["prompt"],
                "language": input_data["language"],
                "context": input_data.get("context"),
                "project_id": project_id
            },
            user_id
        )
        
        return {
            "code": result.get("result"),
            "task_id": result.get("task_id"),
            "language": input_data["language"]
        }
