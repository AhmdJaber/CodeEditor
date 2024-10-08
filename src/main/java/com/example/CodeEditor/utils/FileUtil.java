package com.example.CodeEditor.utils;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileUtil {
    public void createFolder(String folderPath){
        System.out.println(folderPath);
        File folder = new File(folderPath);
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                System.out.println(folderPath);
                throw new IllegalStateException("Something Went Wrong While Creating The Folder");
            }
        }
    }

    public void createFile(String filePath, String content){
        Path fileFullPath = Paths.get(filePath);
        try{
            if (!Files.exists(fileFullPath)) {
                Files.createFile(fileFullPath);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not create the file " + fileFullPath);
        }

        try{
            Files.write(fileFullPath, content.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write on the file " + fileFullPath);
        }
    }

    public File[] getSubFiles(String folderPath){
        File folder = new File(folderPath);
        if (!folder.exists()) {
            return new File[]{};
        }
        return folder.listFiles();
    }

    public void deleteFile(String filePath){
        Path fileFullPath = Paths.get(filePath);
        if(!Files.exists(fileFullPath)){
            throw new IllegalArgumentException("No such file " + fileFullPath);
        }
        try{
            Files.delete(fileFullPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not delete the file " + fileFullPath);
        }
    }

    public void deleteFolder(String folderPath){
        File folder = new File(folderPath);

        if(folder.exists() && folder.isDirectory()){
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file.getAbsolutePath());
                    }
                    else if (!file.delete()) {
                        throw new IllegalStateException("Something Went Wrong While Deleting The File " + file.getAbsolutePath());
                    }
                }
            }

            if (!folder.delete()) {
                throw new IllegalStateException("Something Went Wrong While Deleting The Folder " + folder.getAbsolutePath());
            }
        }
    }

    public String readFileContents(String fullPath) throws IOException {
        Path filePath = Path.of(fullPath);
        return Files.readString(filePath);
    }
}
