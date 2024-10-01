package com.example.CodeEditor.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/snippet")
public class SnippetController {
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
    }

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
    }

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
    }

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

    }
}
