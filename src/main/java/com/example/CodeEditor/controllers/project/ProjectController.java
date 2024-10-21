package com.example.CodeEditor.controllers.project;

import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.services.ProjectService;
import com.example.CodeEditor.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private JwtService jwtService;

    @GetMapping("/client_projects")
    public List<Project> getProjects(@RequestHeader("Authorization") String reqToken) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow();
        return projectService.getClientProjects(client);
    }

    @GetMapping("/shared_projects_edit")
    public List<Project> getSharedEditProjects(@RequestHeader("Authorization") String reqToken) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow();
        return projectService.getSharedEditProjects(client);
    }

    @GetMapping("/shared_projects_view")
    public List<Project> getSharedViewProjects(@RequestHeader("Authorization") String reqToken) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow();
        return projectService.getShareViewProjects(client);
    }

    @PostMapping("/create")
    public Project createProject(@RequestBody Map<String, String> data, @RequestHeader("Authorization") String reqToken) {
        String projectName = data.get("projectName");
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow();
        Project project = Project.builder()
                .name(projectName)
                .client(client)
                .build();
        Project createdProject = projectService.create(project);
        storageService.createProject(client, createdProject);
        return createdProject;
    }

    @DeleteMapping("/delete/{projectId}/{ownerId}")
    public ResponseEntity<String> deleteProject(@RequestHeader("Authorization") String reqToken, @PathVariable long projectId, @PathVariable long ownerId) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow();
        if (client.getId() != ownerId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't allowed to delete this project");
        }

        projectService.deleteProjectById(projectId);
        storageService.deleteProject(client, projectId);
        return ResponseEntity.ok("Project deleted successfully");
    }
}