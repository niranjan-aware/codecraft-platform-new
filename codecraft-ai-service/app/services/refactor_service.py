from app.services.groq_service import groq_service
from app.models.schemas import RefactorRequest

class RefactorService:
    
    def refactor_code(self, request: RefactorRequest) -> dict:
        system_prompt = f"""You are an expert {request.language} developer specializing in code refactoring.
Improve code while preserving functionality. Focus on clean code principles."""

        user_prompt = f"""Refactor the following {request.language} code:
```{request.language}
{request.code}
```

Refactoring Type: {request.refactor_type}

Provide:
1. Refactored Code: Complete improved code
2. Changes Made: List of specific improvements
3. Benefits: How the refactored code is better
4. Considerations: Any trade-offs or notes

Ensure the refactored code maintains the same functionality."""

        response = groq_service.generate_completion(
            prompt=user_prompt,
            system_prompt=system_prompt,
            temperature=0.4
        )
        
        return response

refactor_service = RefactorService()
