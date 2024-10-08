package com.example.CodeEditor.controllers.project;

import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.services.ProjectService;
import com.example.CodeEditor.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/shared_projects")
    public List<Project> getSharedProjects(@RequestHeader("Authorization") String reqToken) {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client client = clientRepository.findByEmail(email).orElseThrow();
        return projectService.getSharedProjects(client);
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
}