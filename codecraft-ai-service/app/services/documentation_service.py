from app.services.groq_service import groq_service
from app.models.schemas import DocumentationRequest

class DocumentationService:
    
    def generate_documentation(self, request: DocumentationRequest) -> dict:
        doc_style = request.doc_style or self._default_doc_style(request.language)
        
        system_prompt = f"""You are a technical writer specializing in {request.language} documentation.
Generate clear, comprehensive documentation following {doc_style} standards."""

        user_prompt = f"""Generate documentation for the following {request.language} code:
```{request.language}
{request.code}
```

Generate documentation in {doc_style} format that includes:
1. Function/Class descriptions
2. Parameters and return values
3. Usage examples
4. Edge cases or notes
5. Related functions or dependencies

Provide well-formatted, complete documentation."""

        response = groq_service.generate_completion(
            prompt=user_prompt,
            system_prompt=system_prompt,
            temperature=0.4
        )
        
        return response
    
    def _default_doc_style(self, language: str) -> str:
        styles = {
            "javascript": "JSDoc",
            "typescript": "TSDoc",
            "python": "docstring (Google style)",
            "java": "JavaDoc",
            "go": "GoDoc",
            "rust": "rustdoc"
        }
        return styles.get(language.lower(), "inline comments and README")

documentation_service = DocumentationService()
