import json
import asyncio
from app.events.rabbitmq_client import rabbitmq_client
from app.services.event_handler import event_handler
from app.config.database import SessionLocal

class EventConsumer:
    
    def __init__(self):
        self.db = SessionLocal()
    
    def callback(self, ch, method, properties, body):
        try:
            message = json.loads(body)
            event_type = method.routing_key
            
            print(f"Received event: {event_type}")
            
            asyncio.run(
                event_handler.handle_event(event_type, message, self.db)
            )
            
            ch.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as e:
            print(f"Error processing event: {str(e)}")
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
    
    def start(self):
        routing_keys = [
            "execution.completed",
            "execution.failed",
            "analysis.completed",
            "workflow.completed",
            "workflow.failed"
        ]
        
        rabbitmq_client.consume(
            queue_name="notifications",
            routing_keys=routing_keys,
            callback=self.callback
        )

event_consumer = EventConsumer()
