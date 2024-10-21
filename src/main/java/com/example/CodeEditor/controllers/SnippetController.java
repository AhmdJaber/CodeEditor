package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.security.jwt.JwtService;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.services.EditorService;
import com.example.CodeEditor.services.SnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/snippet")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class SnippetController {
    @Autowired
    private SnippetService snippetService;

    @Autowired
    private ClientRepository clientRepository;

    @PostMapping("/create/{ownerId}/{projectId}")
    public Snippet addSnippet(@RequestBody Snippet snippet, @PathVariable Long projectId, @PathVariable Long ownerId) throws Exception {
        Client editor = clientRepository.findById(ownerId).orElseThrow();
        snippet.setId(snippetService.createSnippet(editor, snippet, projectId));
        return snippet;
    }

    @DeleteMapping("/delete/{ownerId}/{projectId}")
    public void deleteSnippet(@RequestBody Snippet snippet, @PathVariable Long projectId, @PathVariable Long ownerId) throws Exception {
        Client editor = clientRepository.findById(ownerId).orElseThrow();
        snippetService.removeSnippet(editor, snippet, projectId);
    }

    @GetMapping("/content/{id}/{name}/{ownerId}/{projectId}")
    public String getSnippetContent(@PathVariable Long id, @PathVariable String name, @PathVariable Long projectId, @PathVariable Long ownerId) throws Exception {
        Client editor = clientRepository.findById(ownerId).orElseThrow();
        return snippetService.loadSnippet(editor, id, name, projectId);
    }

    @PutMapping("/update/{id}/{name}/{ownerId}/{projectId}")
    public void updateSnippet(@PathVariable Long id, @PathVariable String name, @RequestBody String content, @PathVariable Long projectId, @PathVariable Long ownerId) throws IOException {
        Client editor = clientRepository.findById(ownerId).orElseThrow();
        snippetService.updateSnippet(editor, id, name, content, projectId);
    }

    @PostMapping("/execute")
    public String executeCode(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String language = body.get("language");

        System.out.println(language);
        if (language.equals("cpp")){
            return cppExecutor(code);
        } else if (language.equals("java")){
            return javaExecutor(code);
        } else if (language.equals("python")){
            return pyExecutor(code);
        } else {
            return "invalid language";
        }

    }

    public void deleteDirectoryRecursively(Path directory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    deleteDirectoryRecursively(entry);
                } else {
                    Files.deleteIfExists(entry);
                }
            }
        }
        Files.deleteIfExists(directory);
    } //TODO: remove

    public String javaExecutor(String code){
        code = code.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"");
        System.out.println(code);

        Path tempDirectory = null;
        try {
            String className = null;
            String[] words = code.split(" ");
            for(int i = 0; i < words.length - 2; i++){
                if (words[i].equals("public") && words[i+1].equals("class")){
                    className = words[i+2];
                }
            }

            if (className == null){
                throw new RuntimeException("Class not found");
            }

            tempDirectory = Files.createTempDirectory("codeExecution");
            Path javaFilePath = tempDirectory.resolve( className + ".java");
            Files.writeString(javaFilePath, code);


            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "run", "--rm", "-v", tempDirectory + ":/app", "openjdk:17-jdk-slim",
                    "sh", "-c", "javac /app/" + className + ".java && java -cp /app " + className
            );

            Process process = processBuilder.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return "Execution timed out";
            }

            String output = new String(process.getInputStream().readAllBytes());
            String errorOutput = new String(process.getErrorStream().readAllBytes());

            if (!errorOutput.isEmpty()) {
                System.out.println("error: \n" + errorOutput);
                return "Execution failed: " + errorOutput;
            } else {
                System.out.println("output: \n" + output);
                return output;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during code execution: " + e.getMessage();
        } finally {
            try {
                deleteDirectoryRecursively(tempDirectory);
            } catch (IOException ioException) {
                System.out.println("Failed to clean up directory: " + ioException.getMessage());
            }
        }
    } //TODO: clean

    public String cppExecutor(String code){
        code = code.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"");
        System.out.println(code);
        Path tempDirectory = null;
        try {
            tempDirectory = Files.createTempDirectory("cppCodeExecution");

            Path cppFilePath = tempDirectory.resolve("CppCode.cpp");

            Files.writeString(cppFilePath, code);

            System.out.println(tempDirectory);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "run", "--rm", "-v", tempDirectory + ":/app", "gcc:latest",
                    "sh", "-c", "g++ /app/CppCode.cpp -o /app/CppCode && /app/CppCode"
            );

            Process process = processBuilder.start();

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return "Execution timed out";
            }

            String output = new String(process.getInputStream().readAllBytes());
            String errorOutput = new String(process.getErrorStream().readAllBytes());

            if (!errorOutput.isEmpty()) {
                System.out.println("error: \n" + errorOutput);
                return "Execution failed: " + errorOutput;
            } else {
                System.out.println("output: \n" + output);
                return output;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during code execution: " + e.getMessage();
        } finally {
            try {
                deleteDirectoryRecursively(tempDirectory);
            } catch (IOException ioException) {
                System.out.println("Failed to clean up directory: " + ioException.getMessage());
            }
        }
    } //TODO: clean

    public String pyExecutor(String code){
        code = code.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"");
        System.out.println(code);

        Path tempDirectory = null;
        try {
            tempDirectory = Files.createTempDirectory("codeExecution");

            Path pythonFilePath = tempDirectory.resolve("TempCode.py");

            Files.writeString(pythonFilePath, code);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "run", "--rm", "-v", tempDirectory + ":/app", "python:3",
                    "python", "/app/TempCode.py"
            );

            Process process = processBuilder.start();

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return "Execution timed out";
            }

            String output = new String(process.getInputStream().readAllBytes());
            String errorOutput = new String(process.getErrorStream().readAllBytes());

            if (!errorOutput.isEmpty()) {
                System.out.println("error: \n" + errorOutput);
                return "Execution failed: " + errorOutput;
            } else {
                System.out.println("output: \n" + output);
                return output;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during code execution: " + e.getMessage();
        } finally {
            try {
                deleteDirectoryRecursively(tempDirectory);
            } catch (IOException ioException) {
                System.out.println("Failed to clean up directory: " + ioException.getMessage());
            }
        }

    } //TODO: clean
}
