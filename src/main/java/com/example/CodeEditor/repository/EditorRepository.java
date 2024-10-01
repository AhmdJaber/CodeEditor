package com.example.CodeEditor.repository;

import com.example.CodeEditor.model.users.editor.Editor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditorRepository extends JpaRepository<Editor, Long> {
    Editor findByEmail(String email);
}
