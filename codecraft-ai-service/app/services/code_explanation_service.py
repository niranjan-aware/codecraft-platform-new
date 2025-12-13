from app.services.groq_service import groq_service
from app.models.schemas import CodeExplanationRequest

class CodeExplanationService:
    
    def explain_code(self, request: CodeExplanationRequest) -> dict:
        system_prompt = f"""You are an expert {request.language} developer and teacher. Explain code clearly and concisely.
Break down complex concepts and provide examples when helpful."""

        user_prompt = f"""Explain the following {request.language} code:
```{request.language}
{request.code}
```

Provide a clear explanation covering:
1. What the code does (high-level overview)
2. How it works (step-by-step breakdown)
3. Key concepts or patterns used
4. Potential improvements or best practices"""

        response = groq_service.generate_completion(
            prompt=user_prompt,
            system_prompt=system_prompt,
            temperature=0.5
        )
        
        return response

code_explanation_service = CodeExplanationService()
