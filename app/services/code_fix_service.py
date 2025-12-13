from app.services.groq_service import GroqService
from app.schemas.ai_schemas import CodeFixRequest, CodeFixResponse
import json

class CodeFixService:
    def __init__(self):
        self.groq_service = GroqService()
    
    async def generate_fix(self, request: CodeFixRequest) -> CodeFixResponse:
        system_message = """You are an expert code security and quality assistant. 
Analyze the security issue and provide a fixed version of the code.
Respond with JSON containing: fixed_code, explanation, changes_made (array), confidence (0-1)."""

        prompt = f"""
Security Issue:
- File: {request.file_path}
- Severity: {request.severity}
- Description: {request.issue_description}

Original Code:
```
{request.code_snippet}
```

Provide a secure fixed version with explanation.
"""

        response = await self.groq_service.generate_structured_completion(
            prompt=prompt,
            system_message=system_message
        )
        
        return CodeFixResponse(
            fixed_code=response.get("fixed_code", ""),
            explanation=response.get("explanation", ""),
            changes_made=response.get("changes_made", []),
            confidence=response.get("confidence", 0.8)
        )
