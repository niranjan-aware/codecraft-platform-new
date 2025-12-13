from app.services.groq_service import GroqService
from app.schemas.ai_schemas import CodeExplanationRequest, CodeExplanationResponse

class CodeExplanationService:
    def __init__(self):
        self.groq_service = GroqService()
    
    async def explain_code(self, request: CodeExplanationRequest) -> CodeExplanationResponse:
        system_message = """You are an expert programming tutor. 
Explain code clearly and provide suggestions for improvement.
Respond with JSON containing: explanation, complexity (Simple/Moderate/Complex), suggestions (array)."""

        prompt = f"""
Explain this code from {request.file_path}:
```
{request.code_snippet}
```

Provide:
1. Clear explanation of what the code does
2. Complexity assessment
3. Suggestions for improvement
"""

        response = await self.groq_service.generate_structured_completion(
            prompt=prompt,
            system_message=system_message
        )
        
        return CodeExplanationResponse(
            explanation=response.get("explanation", ""),
            complexity=response.get("complexity", "Moderate"),
            suggestions=response.get("suggestions", [])
        )
