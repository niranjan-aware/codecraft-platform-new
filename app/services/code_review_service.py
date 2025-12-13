from app.services.groq_service import GroqService
from app.schemas.ai_schemas import CodeReviewRequest, CodeReviewResponse

class CodeReviewService:
    def __init__(self):
        self.groq_service = GroqService()
    
    async def review_code(self, request: CodeReviewRequest) -> CodeReviewResponse:
        system_message = """You are a senior code reviewer. 
Perform a comprehensive code review focusing on quality, maintainability, and best practices.
Respond with JSON containing: issues (array), suggestions (array), quality_score (0-100), refactoring_suggestions (array)."""

        prompt = f"""
Review this code from {request.file_path}:
```
{request.code_snippet}
```

Provide:
1. List of issues found
2. Improvement suggestions
3. Quality score (0-100)
4. Refactoring suggestions
"""

        response = await self.groq_service.generate_structured_completion(
            prompt=prompt,
            system_message=system_message
        )
        
        return CodeReviewResponse(
            issues=response.get("issues", []),
            suggestions=response.get("suggestions", []),
            quality_score=response.get("quality_score", 75.0),
            refactoring_suggestions=response.get("refactoring_suggestions", [])
        )
