package com.example.CodeEditor.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/editor/change")
    public void handleCodeChange(Map<String, Object> body, SimpMessageHeaderAccessor header) throws IOException {
        long projectId = Long.parseLong(body.get("projectId").toString());
        Map<String, String> resBody = new HashMap<>();
        resBody.put("change", body.get("change").toString());
        resBody.put("token", body.get("token").toString());
        messagingTemplate.convertAndSend("/topic/project/" + projectId, resBody);
    }
}