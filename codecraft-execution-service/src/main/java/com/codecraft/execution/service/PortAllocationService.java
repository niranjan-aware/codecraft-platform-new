package com.codecraft.execution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
@Slf4j
public class PortAllocationService {

    private static final int PORT_RANGE_START = 3000;
    private static final int PORT_RANGE_END = 4000;
    private static final int MAX_CONTAINERS_PER_USER = 5;

    private final Set<Integer> allocatedPorts = new ConcurrentSkipListSet<>();
    private final Map<UUID, Set<Integer>> userPorts = new ConcurrentHashMap<>();

    public synchronized Integer allocatePort(UUID userId, Integer preferredPort) {
        Set<Integer> userPortSet = userPorts.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        
        if (userPortSet.size() >= MAX_CONTAINERS_PER_USER) {
            throw new RuntimeException("Maximum " + MAX_CONTAINERS_PER_USER + " concurrent containers reached. Please stop one container first.");
        }

        Integer port = null;

        if (preferredPort != null && isPortAvailable(preferredPort)) {
            port = preferredPort;
        } else {
            for (int p = PORT_RANGE_START; p <= PORT_RANGE_END; p++) {
                if (isPortAvailable(p)) {
                    port = p;
                    break;
                }
            }
        }

        if (port == null) {
            throw new RuntimeException("No available ports in range " + PORT_RANGE_START + "-" + PORT_RANGE_END);
        }

        allocatedPorts.add(port);
        userPortSet.add(port);
        
        log.info("Allocated port {} for user {}", port, userId);
        return port;
    }

    public synchronized void releasePort(UUID userId, Integer port) {
        if (port == null) return;

        allocatedPorts.remove(port);
        
        Set<Integer> userPortSet = userPorts.get(userId);
        if (userPortSet != null) {
            userPortSet.remove(port);
            if (userPortSet.isEmpty()) {
                userPorts.remove(userId);
            }
        }
        
        log.info("Released port {} for user {}", port, userId);
    }

    public int getUserPortCount(UUID userId) {
        Set<Integer> userPortSet = userPorts.get(userId);
        return userPortSet != null ? userPortSet.size() : 0;
    }

    public boolean canAllocateMore(UUID userId) {
        return getUserPortCount(userId) < MAX_CONTAINERS_PER_USER;
    }

    private boolean isPortAvailable(Integer port) {
        return port >= PORT_RANGE_START && 
               port <= PORT_RANGE_END && 
               !allocatedPorts.contains(port);
    }
}
