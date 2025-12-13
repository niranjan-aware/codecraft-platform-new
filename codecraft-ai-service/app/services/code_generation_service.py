from app.services.groq_service import groq_service
from app.models.schemas import CodeGenerationRequest

class CodeGenerationService:
    
    def generate_code(self, request: CodeGenerationRequest) -> dict:
        system_prompt = f"""You are an expert {request.language} developer. Generate clean, efficient, and well-commented code based on user requirements.
Follow best practices and coding standards for {request.language}."""

        user_prompt = f"""Generate {request.language} code for the following requirement:

{request.prompt}
"""

        if request.context:
            user_prompt += f"\n\nContext:\n{request.context}"

        user_prompt += f"\n\nProvide only the code without explanations. Use proper formatting and indentation."

        response = groq_service.generate_completion(
            prompt=user_prompt,
            system_prompt=system_prompt,
            temperature=0.7
        )
        
        return response

code_generation_service = CodeGenerationService()
