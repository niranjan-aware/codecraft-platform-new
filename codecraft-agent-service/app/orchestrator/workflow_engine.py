from typing import Dict, Any, List
from app.agents.agent_factory import AgentFactory
from app.models.schemas import AgentTask, WorkflowStatus
from app.models.database_models import Workflow, AgentExecution
from sqlalchemy.orm import Session
from datetime import datetime
import asyncio

class WorkflowEngine:
    def __init__(self, db: Session):
        self.db = db
    
    async def execute_workflow(
        self, 
        workflow_id: str, 
        tasks: List[AgentTask], 
        context: Dict[str, Any]
    ) -> Dict[str, Any]:
        workflow = self.db.query(Workflow).filter(Workflow.id == workflow_id).first()
        workflow.status = WorkflowStatus.IN_PROGRESS
        self.db.commit()
        
        results = {}
        total_tasks = len(tasks)
        completed_tasks = 0
        
        try:
            for task in tasks:
                await self._wait_for_dependencies(task.dependencies, results)
                
                agent = AgentFactory.create_agent(task.agent_type)
                
                execution = AgentExecution(
                    workflow_id=workflow_id,
                    agent_type=task.agent_type.value,
                    input_data=task.input_data,
                    status="RUNNING"
                )
                self.db.add(execution)
                self.db.commit()
                
                try:
                    result = await asyncio.wait_for(
                        agent.execute(task.input_data, context),
                        timeout=task.timeout
                    )
                    
                    execution.output_data = result
                    execution.status = "COMPLETED"
                    execution.completed_at = datetime.utcnow()
                    
                    results[task.agent_type.value] = result
                    completed_tasks += 1
                    
                    workflow.progress = (completed_tasks / total_tasks) * 100
                    self.db.commit()
                    
                except Exception as e:
                    execution.status = "FAILED"
                    execution.error = str(e)
                    execution.completed_at = datetime.utcnow()
                    self.db.commit()
                    raise
            
            workflow.status = WorkflowStatus.COMPLETED
            workflow.results = results
            workflow.progress = 100.0
            self.db.commit()
            
            return results
            
        except Exception as e:
            workflow.status = WorkflowStatus.FAILED
            workflow.error = str(e)
            self.db.commit()
            raise
    
    async def _wait_for_dependencies(self, dependencies: List[str], results: Dict[str, Any]):
        for dep in dependencies:
            while dep not in results:
                await asyncio.sleep(0.5)
