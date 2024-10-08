package com.example.CodeEditor.socket;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.services.SnippetService;
import com.example.CodeEditor.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.util.Map;

@Controller
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SnippetService snippetService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClientRepository clientRepository; //TODO: mvc

    @MessageMapping("/editor/change")
    public void handleCodeChange(Map<String, String> body, SimpMessageHeaderAccessor header) throws IOException {
        Long ownerId = Long.parseLong(body.get("ownerId"));
        Client client = clientRepository.findById(ownerId).orElseThrow();
        Long projectId = Long.parseLong(body.get("projectId"));
        Long id = Long.parseLong(body.get("snippetId"));
        String name = body.get("snippetName");
        String content = body.get("change");
        snippetService.updateSnippet(client, id, name, content, projectId);
        messagingTemplate.convertAndSend("/topic/project/" + projectId, body.get("change"));
    }
}