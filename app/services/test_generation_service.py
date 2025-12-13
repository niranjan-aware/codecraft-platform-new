from app.services.groq_service import GroqService
from app.schemas.ai_schemas import TestGenerationRequest, TestGenerationResponse

class TestGenerationService:
    def __init__(self):
        self.groq_service = GroqService()
    
    async def generate_tests(self, request: TestGenerationRequest) -> TestGenerationResponse:
        system_message = f"""You are an expert test engineer. 
Generate comprehensive tests using {request.test_framework}.
Respond with JSON containing: test_code, test_cases (array of {{name, description}}), coverage_percentage."""

        prompt = f"""
Generate unit tests for this code from {request.file_path}:
```
{request.code_snippet}
```

Framework: {request.test_framework}

Provide:
1. Complete test code
2. List of test cases
3. Estimated coverage percentage
"""

        response = await self.groq_service.generate_structured_completion(
            prompt=prompt,
            system_message=system_message
        )
        
        return TestGenerationResponse(
            test_code=response.get("test_code", ""),
            test_cases=response.get("test_cases", []),
            coverage_percentage=response.get("coverage_percentage", 80.0)
        )
