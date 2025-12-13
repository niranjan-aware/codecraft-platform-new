from app.services.groq_service import groq_service
from app.models.schemas import TestGenerationRequest

class TestGenerationService:
    
    def generate_tests(self, request: TestGenerationRequest) -> dict:
        framework = request.test_framework or self._default_framework(request.language)
        
        system_prompt = f"""You are an expert in {request.language} testing. Generate comprehensive, well-structured tests.
Use {framework} framework and follow testing best practices."""

        user_prompt = f"""Generate unit tests for the following {request.language} code:
```{request.language}
{request.code}
```

Generate tests using {framework} that cover:
1. Happy path scenarios
2. Edge cases
3. Error handling
4. Boundary conditions

Provide complete, runnable test code with proper setup and assertions."""

        response = groq_service.generate_completion(
            prompt=user_prompt,
            system_prompt=system_prompt,
            temperature=0.5
        )
        
        return response
    
    def _default_framework(self, language: str) -> str:
        frameworks = {
            "javascript": "Jest",
            "typescript": "Jest",
            "python": "pytest",
            "java": "JUnit",
            "go": "testing",
            "rust": "cargo test"
        }
        return frameworks.get(language.lower(), "standard testing framework")

test_generation_service = TestGenerationService()
