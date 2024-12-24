package org.finsible.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/")
public class ServerController {
    @Value("${server.port}")
    private Integer ServerPort;

    @GetMapping("health")
    public HashMap<String,String> getServerHealth() {
        HashMap<String, String> map =new HashMap<>();
        map.put("status", "OK");
        map.put( "message","Server is running on Port: " + ServerPort);
        return map;
    }
}
