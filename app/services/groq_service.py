from groq import Groq
from app.core.config import settings
from typing import Optional
import json

class GroqService:
    def __init__(self):
        self.client = Groq(api_key=settings.groq_api_key)
        self.model = settings.groq_model
    
    async def generate_completion(
        self,
        prompt: str,
        system_message: Optional[str] = None,
        temperature: float = None,
        max_tokens: int = None
    ) -> str:
        messages = []
        
        if system_message:
            messages.append({"role": "system", "content": system_message})
        
        messages.append({"role": "user", "content": prompt})
        
        response = self.client.chat.completions.create(
            model=self.model,
            messages=messages,
            temperature=temperature or settings.temperature,
            max_tokens=max_tokens or settings.max_tokens
        )
        
        return response.choices[0].message.content
    
    async def generate_structured_completion(
        self,
        prompt: str,
        system_message: Optional[str] = None,
        response_format: Optional[dict] = None
    ) -> dict:
        content = await self.generate_completion(prompt, system_message)
        
        try:
            start_idx = content.find('{')
            end_idx = content.rfind('}') + 1
            if start_idx != -1 and end_idx > start_idx:
                json_str = content[start_idx:end_idx]
                return json.loads(json_str)
            return json.loads(content)
        except:
            return {"raw_response": content}
