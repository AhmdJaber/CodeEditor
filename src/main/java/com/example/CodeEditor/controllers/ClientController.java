package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.ProjectStructure;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.services.ClientService;
import com.example.CodeEditor.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/editor")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class ClientController {
    @Autowired
    private StorageService storageService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/all-editors")
    public List<Client> allEditors(){
        return clientService.getAllEditors();
    }

    @GetMapping("/directory/{ownerId}/{projectId}")
    public ProjectStructure getEditorDirectory(@PathVariable Long ownerId, @PathVariable Long projectId) { // TODO: clean "security"?
        Client editor = clientRepository.findById(ownerId).orElseThrow();
        return storageService.loadEditorDirObj(editor, projectId);
    }

    @PostMapping("/share_project_edit/{email}/{ownerId}/{projectId}")
    public ResponseEntity<?> shareProjectWithEdit(@PathVariable String email, @PathVariable Long ownerId, @PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) throws IOException {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientRepository.findByEmail(senderEmail).orElseThrow();
        if (!Objects.equals(client.getId(), ownerId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't allowed to share this project");
        }
        Client clientToShareWith = clientRepository.findByEmail(email).orElse(null);
        if (clientToShareWith != null) {
            storageService.shareProjectWithEdit(clientToShareWith, projectId, ownerId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/share_project_view/{email}/{ownerId}/{projectId}")
    public ResponseEntity<?> shareProjectWithView(@PathVariable String email, @PathVariable Long ownerId, @PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) throws IOException {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientRepository.findByEmail(senderEmail).orElseThrow();
        if (!Objects.equals(client.getId(), ownerId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't allowed to share this project");
        }
        Client clientToShareWith = clientRepository.findByEmail(email).orElse(null);
        if (clientToShareWith != null) {
            storageService.shareProjectWithView(clientToShareWith, projectId, ownerId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/share_project_view_token/{projectId}")
    public ResponseEntity<?> shareProjectWithViewByToken(@PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) throws IOException {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientRepository.findByEmail(senderEmail).orElse(null);
        if (client != null) {
            Project project = projectRepository.findById(projectId).orElseThrow();
            if (Objects.equals(client, project.getClient())){
                return ResponseEntity.badRequest().body("Sharing project to view with the owner is not allowed");
            }
            storageService.shareProjectWithView(client, projectId, project.getClient().getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/share-project-public/{projectId}")
    public ResponseEntity<?> shareToPublic(@PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) throws IOException {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientRepository.findByEmail(senderEmail).orElseThrow();
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (!Objects.equals(client.getId(), project.getClient().getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't allowed to share this project");
        }
        storageService.shareProjectToPublic(projectId);
        return ResponseEntity.ok("Shared to public successfully");
    }

    @GetMapping("/get-public-projects/{clientId}")
    public ResponseEntity<?> getPublicProjects(@PathVariable Long clientId){
        return ResponseEntity.ok().body(storageService.getPublicProjects(clientId));
    }

    @DeleteMapping("/remove-project-public/{projectId}")
    public ResponseEntity<?> removePublicProject(@PathVariable Long projectId){
        storageService.removeProjectFromPublic(projectId);
        return ResponseEntity.ok("Removed from public successfully");
    }

    @GetMapping("/check-project-public/{projectId}")
    public ResponseEntity<?> checkProjectPublic(@PathVariable Long projectId){
        return ResponseEntity.ok(storageService.checkProjectPublic(projectId));
    }

    @GetMapping("/get-editor-by-data/{email}")
    public ResponseEntity<?> getEditorByData(@PathVariable String email){
        if (clientRepository.existsByEmail(email)){
            return ResponseEntity.ok(clientRepository.findByEmail(email));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
