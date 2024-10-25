package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.services.ClientService;
import com.example.CodeEditor.services.JwtService;
import com.example.CodeEditor.services.ProjectService;
import com.example.CodeEditor.services.storage.ProjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/project")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectStorageService projectStorageService;

    @GetMapping("/client_projects")
    public List<Project> getProjects(@RequestHeader("Authorization") String reqToken) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientService.getClientByEmail(email);
        return projectService.getClientProjects(client);
    }

    @GetMapping("/shared_projects_edit")
    public List<Project> getSharedEditProjects(@RequestHeader("Authorization") String reqToken) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientService.getClientByEmail(email);
        return projectService.getSharedEditProjects(client);
    }

    @GetMapping("/shared_projects_view")
    public List<Project> getSharedViewProjects(@RequestHeader("Authorization") String reqToken) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientService.getClientByEmail(email);
        return projectService.getShareViewProjects(client);
    }

    @PostMapping("/create")
    public Project createProject(@RequestBody Map<String, String> data, @RequestHeader("Authorization") String reqToken) {
        String projectName = data.get("projectName");
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientService.getClientByEmail(email);
        Project project = Project.builder()
                .name(projectName)
                .client(client)
                .build();
        Project createdProject = projectService.create(project);
        projectStorageService.createProject(client, createdProject);
        return createdProject;
    }

    @DeleteMapping("/delete/{projectId}/{ownerId}")
    public ResponseEntity<String> deleteProject(@RequestHeader("Authorization") String reqToken, @PathVariable long projectId, @PathVariable long ownerId) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientService.getClientByEmail(email);
        if (client.getId() != ownerId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't allowed to delete this project");
        }

        projectStorageService.deleteProject(client, projectId);
        projectService.deleteProjectById(projectId);
        return ResponseEntity.ok("Project deleted successfully");
    }
}