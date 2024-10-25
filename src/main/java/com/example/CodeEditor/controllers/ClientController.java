package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.ProjectStructure;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.services.ClientService;
import com.example.CodeEditor.services.JwtService;
import com.example.CodeEditor.services.ProjectService;
import com.example.CodeEditor.services.storage.ProjectStorageService;
import com.example.CodeEditor.services.storage.PublicRepoStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/editor")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class ClientController {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PublicRepoStorageService publicRepoStorageService;

    @Autowired
    private ProjectStorageService projectStorageService;

    @GetMapping("/all-editors")
    public List<Client> allEditors(){
        return clientService.getAllEditors();
    }

    @GetMapping("/directory/{ownerId}/{projectId}")
    public ProjectStructure getEditorDirectory(@PathVariable Long ownerId, @PathVariable Long projectId) {
        Client editor = clientService.getClientById(ownerId);
        return projectStorageService.loadProjectStructure(editor, projectId);
    }

    @PostMapping("/share_project_edit/{email}/{ownerId}/{projectId}")
    public ResponseEntity<?> shareProjectWithEdit(@PathVariable String email, @PathVariable Long ownerId, @PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) {
        Client clientToShareWith = clientService.getClientToShareWith(reqToken, ownerId, email);
        projectStorageService.shareProjectWithEdit(clientToShareWith, projectId, ownerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/share_project_view/{email}/{ownerId}/{projectId}")
    public ResponseEntity<?> shareProjectWithView(@PathVariable String email, @PathVariable Long ownerId, @PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) {
        Client clientToShareWith = clientService.getClientToShareWith(reqToken, ownerId, email);
        projectStorageService.shareProjectWithView(clientToShareWith, projectId, ownerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/share_project_view_token/{projectId}")
    public ResponseEntity<?> shareProjectWithViewByToken(@PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientService.getClientByEmail(senderEmail);
        Project project = projectService.getProjectById(projectId);
        if (Objects.equals(client, project.getClient())){
            return ResponseEntity.badRequest().body("Sharing project to view with the owner is not allowed");
        }
        projectStorageService.shareProjectWithView(client, projectId, project.getClient().getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/share-project-public/{projectId}")
    public ResponseEntity<?> shareToPublic(@PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientService.getClientByEmail(senderEmail);
        Project project = projectService.getProjectById(projectId);
        if (!Objects.equals(client.getId(), project.getClient().getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't allowed to share this project");
        }
        publicRepoStorageService.shareProjectToPublic(projectId);
        return ResponseEntity.ok("Shared to public successfully");
    }

    @GetMapping("/get-public-projects/{clientId}")
    public ResponseEntity<?> getPublicProjects(@PathVariable Long clientId){
        return ResponseEntity.ok().body(publicRepoStorageService.getPublicProjects(clientId));
    }

    @DeleteMapping("/remove-project-public/{projectId}")
    public ResponseEntity<?> removePublicProject(@PathVariable Long projectId){
        publicRepoStorageService.removeProjectFromPublic(projectId);
        return ResponseEntity.ok("Removed from public successfully");
    }

    @GetMapping("/check-project-public/{projectId}")
    public ResponseEntity<?> checkProjectPublic(@PathVariable Long projectId){
        return ResponseEntity.ok(publicRepoStorageService.checkProjectPublic(projectId));
    }

    @GetMapping("/get-editor-by-data/{email}")
    public ResponseEntity<?> getEditorByEmail(@PathVariable String email){
        if (clientService.existsClientByEmail(email)){
            return ResponseEntity.ok(clientService.getClientByEmail(email));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
