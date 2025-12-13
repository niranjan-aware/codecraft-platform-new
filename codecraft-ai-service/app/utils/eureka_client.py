import requests
import socket
import threading
import time
from app.config.settings import settings

class EurekaClient:
    def __init__(self):
        self.eureka_server = settings.eureka_server
        self.service_name = settings.service_name
        self.service_port = settings.service_port
        self.host_name = socket.gethostname()
        self.ip_address = socket.gethostbyname(self.host_name)
        self.heartbeat_thread = None
        self.running = False
    
    def register(self):
        url = f"{self.eureka_server}/apps/{self.service_name.upper()}"
        
        payload = {
            "instance": {
                "instanceId": f"{self.ip_address}:{self.service_name}:{self.service_port}",
                "hostName": self.ip_address,
                "app": self.service_name.upper(),
                "ipAddr": self.ip_address,
                "status": "UP",
                "port": {"$": self.service_port, "@enabled": "true"},
                "securePort": {"$": 443, "@enabled": "false"},
                "healthCheckUrl": f"http://{self.ip_address}:{self.service_port}/health",
                "statusPageUrl": f"http://{self.ip_address}:{self.service_port}/health",
                "homePageUrl": f"http://{self.ip_address}:{self.service_port}/",
                "dataCenterInfo": {
                    "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                    "name": "MyOwn"
                },
                "vipAddress": self.service_name,
                "metadata": {
                    "management.port": str(self.service_port)
                }
            }
        }
        
        try:
            response = requests.post(
                url,
                json=payload,
                headers={"Content-Type": "application/json"}
            )
            if response.status_code in [200, 204]:
                print(f"Successfully registered with Eureka: {self.service_name}")
                self.start_heartbeat()
            else:
                print(f"Failed to register with Eureka: {response.status_code}")
        except Exception as e:
            print(f"Error registering with Eureka: {str(e)}")
    
    def send_heartbeat(self):
        url = f"{self.eureka_server}/apps/{self.service_name.upper()}/{self.ip_address}:{self.service_name}:{self.service_port}"
        
        try:
            response = requests.put(url)
            if response.status_code == 200:
                print(f"Heartbeat sent successfully for {self.service_name}")
                return True
            else:
                print(f"Heartbeat failed: {response.status_code}")
                return False
        except Exception as e:
            print(f"Heartbeat error: {str(e)}")
            return False
    
    def heartbeat_loop(self):
        while self.running:
            time.sleep(30)
            self.send_heartbeat()
    
    def start_heartbeat(self):
        if not self.running:
            self.running = True
            self.heartbeat_thread = threading.Thread(target=self.heartbeat_loop, daemon=True)
            self.heartbeat_thread.start()
            print(f"Heartbeat thread started for {self.service_name}")
    
    def stop_heartbeat(self):
        self.running = False
        if self.heartbeat_thread:
            self.heartbeat_thread.join(timeout=5)

eureka_client = EurekaClient()
