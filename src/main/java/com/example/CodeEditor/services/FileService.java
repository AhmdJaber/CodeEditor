package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.File;
import com.example.CodeEditor.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    public File getFileById(Long id){
        return fileRepository.findById(id).orElseThrow(
                () -> new RuntimeException("File with id " + id + " not found")
        );
    }

    public File createFile(File file){
        return fileRepository.save(file);
    }
}