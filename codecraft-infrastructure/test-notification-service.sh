#!/bin/bash

TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIwMGQyNjljMS0wMmQxLTQ4ZjEtOWQ4NS00MDVkMjQ2NzJiYjYiLCJlbWFpbCI6Im5pcm9iYS5hd2FyZS4yNkBnbWFpbC5jb20iLCJpYXQiOjE3NjU2MDY3MDEsImV4cCI6MTc2NTY5MzEwMX0.UURM-zEdgnzW2b2_9cqMQtKaZIHLqm7uCvXpdSx4vzFCwrFRVIciXCHdJoxDMTkTlM5ZOYZgLgExKutQojv35A"

echo "======================================"
echo " Notification Service Tests"
echo "======================================"

echo -e "\n1. Health Check..."
curl -s http://localhost:8087/health | python3 -m json.tool

echo -e "\n\n2. Create In-App Notification..."
NOTIF_RESPONSE=$(curl -s -X POST http://localhost:8080/api/notifications/in-app \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "user_id": "00d269c1-02d1-48f1-9d85-405d24672bb6",
    "title": "Welcome",
    "message": "Welcome to CodeCraft Platform!",
    "link": "/dashboard"
  }')
echo "$NOTIF_RESPONSE" | python3 -m json.tool

echo -e "\n\n3. Get All Notifications..."
curl -s http://localhost:8080/api/notifications \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo -e "\n\n4. Test Event Publishing..."
python3 << PYTHON
import pika
import json

try:
    connection = pika.BlockingConnection(pika.URLParameters('amqp://guest:guest@localhost:5672/'))
    channel = connection.channel()
    channel.exchange_declare(exchange='codecraft', exchange_type='topic', durable=True)
    
    event = {
        "user_id": "00d269c1-02d1-48f1-9d85-405d24672bb6",
        "user_email": "niroba.aware.26@gmail.com",
        "project_id": "704b25e4-8641-4cc4-818b-78e172fae2df",
        "workflow_id": "test-workflow-123"
    }
    
    channel.basic_publish(
        exchange='codecraft',
        routing_key='workflow.completed',
        body=json.dumps(event)
    )
    
    print("✓ Published test event: workflow.completed")
    connection.close()
except Exception as e:
    print(f"✗ Failed to publish event: {e}")
PYTHON

echo -e "\n\n5. Check RabbitMQ Queue..."
sleep 2
curl -s -u guest:guest http://localhost:15672/api/queues/%2F/notifications | python3 -m json.tool

echo -e "\n\n======================================"
echo " ✅ Notification Service Tests Complete"
echo "======================================"
