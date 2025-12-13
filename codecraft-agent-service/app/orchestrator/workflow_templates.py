from app.models.schemas import AgentTask, AgentType
from typing import List

class WorkflowTemplates:
    
    @staticmethod
    def code_generation_workflow(requirements: str, language: str, generate_tests: bool, generate_docs: bool) -> List[AgentTask]:
        tasks = [
            AgentTask(
                agent_type=AgentType.CODE_GENERATOR,
                input_data={
                    "prompt": requirements,
                    "language": language
                }
            )
        ]
        
        if generate_tests:
            tasks.append(
                AgentTask(
                    agent_type=AgentType.TEST_GENERATOR,
                    input_data={
                        "code": "${CODE_GENERATOR.code}",
                        "language": language
                    },
                    dependencies=["CODE_GENERATOR"]
                )
            )
        
        if generate_docs:
            tasks.append(
                AgentTask(
                    agent_type=AgentType.DOCUMENTER,
                    input_data={
                        "code": "${CODE_GENERATOR.code}",
                        "language": language
                    },
                    dependencies=["CODE_GENERATOR"]
                )
            )
        
        return tasks
    
    @staticmethod
    def bug_fix_workflow(code: str, language: str, error_message: str, stack_trace: str, generate_tests: bool) -> List[AgentTask]:
        tasks = [
            AgentTask(
                agent_type=AgentType.BUG_FIXER,
                input_data={
                    "code": code,
                    "language": language,
                    "error_message": error_message,
                    "stack_trace": stack_trace
                }
            )
        ]
        
        if generate_tests:
            tasks.append(
                AgentTask(
                    agent_type=AgentType.TEST_GENERATOR,
                    input_data={
                        "code": "${BUG_FIXER.fixed_code}",
                        "language": language
                    },
                    dependencies=["BUG_FIXER"]
                )
            )
        
        return tasks
    
    @staticmethod
    def feature_workflow(feature_description: str, language: str, generate_tests: bool, review_code: bool) -> List[AgentTask]:
        tasks = [
            AgentTask(
                agent_type=AgentType.CODE_GENERATOR,
                input_data={
                    "prompt": feature_description,
                    "language": language
                }
            )
        ]
        
        if review_code:
            tasks.append(
                AgentTask(
                    agent_type=AgentType.CODE_REVIEWER,
                    input_data={
                        "code": "${CODE_GENERATOR.code}",
                        "language": language
                    },
                    dependencies=["CODE_GENERATOR"]
                )
            )
        
        if generate_tests:
            tasks.append(
                AgentTask(
                    agent_type=AgentType.TEST_GENERATOR,
                    input_data={
                        "code": "${CODE_GENERATOR.code}",
                        "language": language
                    },
                    dependencies=["CODE_GENERATOR"]
                )
            )
        
        tasks.append(
            AgentTask(
                agent_type=AgentType.DOCUMENTER,
                input_data={
                    "code": "${CODE_GENERATOR.code}",
                    "language": language
                },
                dependencies=["CODE_GENERATOR"]
            )
        )
        
        return tasks
    
    @staticmethod
    def refactor_workflow(code: str, language: str, refactor_type: str) -> List[AgentTask]:
        return [
            AgentTask(
                agent_type=AgentType.REFACTORER,
                input_data={
                    "code": code,
                    "language": language,
                    "refactor_type": refactor_type
                }
            ),
            AgentTask(
                agent_type=AgentType.CODE_REVIEWER,
                input_data={
                    "code": "${REFACTORER.refactored_code}",
                    "language": language
                },
                dependencies=["REFACTORER"]
            )
        ]
