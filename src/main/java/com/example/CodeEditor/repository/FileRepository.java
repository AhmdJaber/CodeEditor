package com.example.CodeEditor.repository;

import com.example.CodeEditor.model.component.files.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
