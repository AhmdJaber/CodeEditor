package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.ProjectDirectory;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/editor")
@CrossOrigin("http://localhost:5000")
public class ClientController {
    @Autowired
    private StorageService storageService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/directory/{ownerId}/{projectId}")
    public ProjectDirectory getEditorDirectory(@PathVariable Long ownerId, @PathVariable Long projectId) { // TODO: clean "security"?
        Client editor = clientRepository.findById(ownerId).orElseThrow();
        return storageService.loadEditorDirObj(editor, projectId);
    }

    @PostMapping("/share_project/{email}/{ownerId}/{projectId}")
    public ResponseEntity<?> shareEditorProject(@PathVariable String email, @PathVariable Long ownerId, @PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) throws IOException {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientRepository.findByEmail(senderEmail).orElseThrow();
        if (!Objects.equals(client.getId(), ownerId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't allowed to share this project");
        }
        Client clientToShareWith = clientRepository.findByEmail(email).orElse(null);
        if (clientToShareWith != null) {
            storageService.shareProject(clientToShareWith, projectId, ownerId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
