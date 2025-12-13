import requests
import socket
from app.config.settings import settings

class EurekaClient:
    def __init__(self):
        self.eureka_server = settings.eureka_server
        self.service_name = settings.service_name
        self.service_port = settings.service_port
        self.host_name = socket.gethostname()
        self.ip_address = socket.gethostbyname(self.host_name)
    
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
            else:
                print(f"Failed to register with Eureka: {response.status_code}")
        except Exception as e:
            print(f"Error registering with Eureka: {str(e)}")
    
    def send_heartbeat(self):
        url = f"{self.eureka_server}/apps/{self.service_name.upper()}/{self.ip_address}:{self.service_name}:{self.service_port}"
        
        try:
            response = requests.put(url)
            if response.status_code == 200:
                return True
            return False
        except Exception as e:
            print(f"Heartbeat failed: {str(e)}")
            return False

eureka_client = EurekaClient()
