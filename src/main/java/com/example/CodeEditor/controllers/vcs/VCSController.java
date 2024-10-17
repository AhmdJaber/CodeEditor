package com.example.CodeEditor.controllers.vcs;

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

    @GetMapping("/init/{projectId}")
    public ResponseEntity<String> init(@PathVariable Long projectId) throws IOException {
        vcsService.initVCS(projectId); //TODO:(exception handling) if the .vcs folder already exists
        return ResponseEntity.ok(".vcs directory initialized");
    }

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<String> deleteVCS(@PathVariable Long projectId) {
        vcsService.deleteVCS(projectId);
        return ResponseEntity.ok(".vcs directory deleted");
    }

    @GetMapping("/status/{projectId}")
    public ResponseEntity<?> status(@PathVariable Long projectId) {
        Map<String, List<String>> status =  vcsService.status(projectId, "main"); //TODO: Provide the branch name
        return ResponseEntity.ok().body(status);
    }

    @PostMapping("/add/{projectId}")
    public ResponseEntity<?> add(@PathVariable Long projectId, @RequestBody Map<String, Object> body) throws IOException {
        List<String> filePaths = (List<String>) body.get("files");
        List<String> addResult = vcsService.add(projectId, filePaths); //TODO: Provide the branch name
        return ResponseEntity.ok().body(addResult);
    }



}
