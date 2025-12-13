import pika
import json
from app.config.settings import settings
from typing import Callable

class RabbitMQClient:
    def __init__(self):
        self.connection = None
        self.channel = None
        self.connect()
    
    def connect(self):
        try:
            parameters = pika.URLParameters(settings.rabbitmq_url)
            self.connection = pika.BlockingConnection(parameters)
            self.channel = self.connection.channel()
            
            self.channel.exchange_declare(
                exchange='codecraft',
                exchange_type='topic',
                durable=True
            )
            
            print("Connected to RabbitMQ")
        except Exception as e:
            print(f"Failed to connect to RabbitMQ: {str(e)}")
    
    def publish(self, routing_key: str, message: dict):
        try:
            self.channel.basic_publish(
                exchange='codecraft',
                routing_key=routing_key,
                body=json.dumps(message),
                properties=pika.BasicProperties(
                    delivery_mode=2,
                    content_type='application/json'
                )
            )
            print(f"Published message to {routing_key}")
        except Exception as e:
            print(f"Failed to publish message: {str(e)}")
            self.connect()
    
    def consume(self, queue_name: str, routing_keys: list, callback: Callable):
        try:
            self.channel.queue_declare(queue=queue_name, durable=True)
            
            for routing_key in routing_keys:
                self.channel.queue_bind(
                    exchange='codecraft',
                    queue=queue_name,
                    routing_key=routing_key
                )
            
            self.channel.basic_consume(
                queue=queue_name,
                on_message_callback=callback,
                auto_ack=False
            )
            
            print(f"Consuming from queue: {queue_name}")
            self.channel.start_consuming()
        except Exception as e:
            print(f"Error consuming messages: {str(e)}")
    
    def close(self):
        if self.connection:
            self.connection.close()

rabbitmq_client = RabbitMQClient()
