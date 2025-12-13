from app.services.groq_service import groq_service
from app.models.schemas import CodeReviewRequest

class CodeReviewService:
    
    def review_code(self, request: CodeReviewRequest) -> dict:
        system_prompt = f"""You are a senior {request.language} code reviewer. Provide constructive, actionable feedback.
Focus on code quality, best practices, security, and performance."""

        file_context = f"\nFile: {request.file_path}" if request.file_path else ""

        user_prompt = f"""Review the following {request.language} code:{file_context}
```{request.language}
{request.code}
```

Provide a comprehensive code review covering:
1. Code Quality: readability, maintainability, organization
2. Best Practices: adherence to {request.language} conventions
3. Security: potential vulnerabilities or risks
4. Performance: optimization opportunities
5. Bugs: potential issues or edge cases
6. Suggestions: specific improvements with examples

Format your review with clear sections and actionable recommendations."""

        response = groq_service.generate_completion(
            prompt=user_prompt,
            system_prompt=system_prompt,
            temperature=0.3
        )
        
        return response

code_review_service = CodeReviewService()
