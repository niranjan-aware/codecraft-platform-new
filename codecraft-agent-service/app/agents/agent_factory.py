from app.agents.code_generator_agent import CodeGeneratorAgent
from app.agents.test_generator_agent import TestGeneratorAgent
from app.agents.bug_fixer_agent import BugFixerAgent
from app.agents.code_reviewer_agent import CodeReviewerAgent
from app.agents.refactorer_agent import RefactorerAgent
from app.agents.documenter_agent import DocumenterAgent
from app.models.schemas import AgentType

class AgentFactory:
    _agents = {
        AgentType.CODE_GENERATOR: CodeGeneratorAgent,
        AgentType.TEST_GENERATOR: TestGeneratorAgent,
        AgentType.BUG_FIXER: BugFixerAgent,
        AgentType.CODE_REVIEWER: CodeReviewerAgent,
        AgentType.REFACTORER: RefactorerAgent,
        AgentType.DOCUMENTER: DocumenterAgent
    }
    
    @classmethod
    def create_agent(cls, agent_type: AgentType):
        agent_class = cls._agents.get(agent_type)
        if not agent_class:
            raise ValueError(f"Unknown agent type: {agent_type}")
        return agent_class()
