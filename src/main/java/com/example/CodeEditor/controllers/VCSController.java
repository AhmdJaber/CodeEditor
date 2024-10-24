package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.vcs.VCSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/vcs")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class VCSController {
    @Autowired
    private VCSService vcsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/init/{projectId}")
    public ResponseEntity<String> init(@PathVariable Long projectId) throws Exception {
        vcsService.initVCS(projectId); //TODO:(exception handling) if the .vcs folder already exists
        return ResponseEntity.ok(".vcs directory initialized");
    }

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<String> deleteVCS(@PathVariable Long projectId) {
        vcsService.deleteVCS(projectId);
        return ResponseEntity.ok(".vcs directory deleted");
    }

    @GetMapping("/status/{projectId}")
    public ResponseEntity<?> status(@PathVariable Long projectId) throws IOException {
        Map<String, List<String>> status =  vcsService.status(projectId); //TODO: Provide the branch name
        return ResponseEntity.ok().body(status);
    }

    @PostMapping("/add/{projectId}")
    public ResponseEntity<?> add(@PathVariable Long projectId, @RequestBody Map<String, Object> body) throws Exception {
        List<String> filePaths = (List<String>) body.get("files");
        List<String> addResult = vcsService.add(projectId, filePaths); //TODO: Provide the branch name
        return ResponseEntity.ok().body(addResult);
    }

    @PostMapping("/commit/{projectId}")
    public ResponseEntity<?> commit(@PathVariable Long projectId, @RequestBody String message, @RequestHeader("Authorization") String reqToken) throws Exception {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientRepository.findByEmail(senderEmail).orElseThrow();
        message = message.substring(1, message.length() - 1);
        vcsService.commit(projectId, client, message);
        return ResponseEntity.ok("Changes committed!"); //TODO: return the list of changes that have been commited
    }

    @GetMapping("/log/{projectId}")
    public ResponseEntity<?> log(@PathVariable Long projectId) throws IOException {
        String log = vcsService.log(projectId);
        return ResponseEntity.ok(log);
    }

    @PostMapping("/revert/{projectId}")
    public ResponseEntity<?> revert(@PathVariable Long projectId, @RequestBody String commitId) throws Exception {
        vcsService.revert(projectId, commitId.replace("\"", ""));
        return ResponseEntity.ok("Reverted to commit with id: " + commitId);
    }

    @GetMapping("/check-vcs/{projectId}")
    public ResponseEntity<?> checkVCS(@PathVariable Long projectId){
        return ResponseEntity.ok().body(vcsService.checkVCSProject(projectId));
    }

    @PostMapping("/fork/{projectId}")
    public ResponseEntity<?> fork(@PathVariable Long projectId, @RequestHeader("Authorization") String reqToken) throws Exception {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientRepository.findByEmail(senderEmail).orElseThrow();
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (Objects.equals(project.getClient(), client)){
            return ResponseEntity.badRequest().body("Cannot fork projects you own");
        }
        vcsService.fork(client, projectId);
        return ResponseEntity.ok("Forked successfully");
    }

    @PostMapping("/fork")
    public ResponseEntity<?> fork(@RequestBody Map<String, String> body, @RequestHeader("Authorization") String reqToken) throws Exception {
        String ownerEmail = body.get("owner");
        String projectName = body.get("project");
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client client = clientRepository.findByEmail(senderEmail).orElseThrow();
        Client owner = clientRepository.findByEmail(ownerEmail).orElseThrow();
        Project project = projectRepository.findByNameAndClient(projectName, owner).orElseThrow();
        if (Objects.equals(project.getClient(), client)){
            return ResponseEntity.badRequest().body("Cannot fork projects you own");
        }
        vcsService.fork(client, project);
        return ResponseEntity.ok("Forked successfully");
    }

    @GetMapping("/branches/{projectId}")
    public ResponseEntity<?> allBranches(@PathVariable Long projectId){
        return ResponseEntity.ok(vcsService.allBranches(projectId));
    }

    @PostMapping("/create-branch/{projectId}")
    public ResponseEntity<?> createBranch(@PathVariable Long projectId, @RequestBody String branchName) throws Exception {
        vcsService.createBranch(projectId, branchName);
        return ResponseEntity.ok("Created new branch: " + branchName);
    }

    @DeleteMapping("/delete-branch/{projectId}")
    public ResponseEntity<?> deleteBranch(@PathVariable Long projectId, @RequestBody String branchName) throws Exception {
        vcsService.deleteBranch(projectId, branchName);
        return ResponseEntity.ok("Deleted branch: " + branchName);
    }

    @PutMapping("/checkout/{projectId}")
    public ResponseEntity<?> chekcout(@PathVariable Long projectId, @RequestBody String branchName) throws Exception {
        vcsService.checkout(projectId, branchName);
        return ResponseEntity.ok("Switched to branch: " + branchName);
    }

    @PostMapping("/checkout-create/{projectId}")
    public ResponseEntity<?> checkoutCreate(@PathVariable Long projectId, @RequestBody String branchName) throws Exception {
        vcsService.createBranch(projectId, branchName);
        vcsService.checkout(projectId, branchName);
        return ResponseEntity.ok("Created new branch: " + branchName + " and switched to it");
    }


}
