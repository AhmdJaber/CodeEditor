package com.example.CodeEditor.controllers.vcs;

import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.services.StorageService;
import com.example.CodeEditor.vcs.VCSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vcs")
public class VCSController {
    @Autowired
    private VCSService vcsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClientRepository clientRepository;

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


}
