from app.services.groq_service import groq_service
from app.models.schemas import BugFixRequest

class BugFixService:
    
    def fix_bug(self, request: BugFixRequest) -> dict:
        system_prompt = f"""You are an expert {request.language} debugger. Analyze errors and provide working fixes.
Explain the root cause and your solution clearly."""

        user_prompt = f"""Fix the bug in the following {request.language} code:
```{request.language}
{request.code}
```

Error Message:
{request.error_message}
"""

        if request.stack_trace:
            user_prompt += f"\nStack Trace:\n{request.stack_trace}"

        user_prompt += f"""

Provide:
1. Root Cause: What's causing the error
2. Fixed Code: Complete corrected code
3. Explanation: Why your fix works
4. Prevention: How to avoid this error in the future"""

        response = groq_service.generate_completion(
            prompt=user_prompt,
            system_prompt=system_prompt,
            temperature=0.3
        )
        
        return response

bug_fix_service = BugFixService()
