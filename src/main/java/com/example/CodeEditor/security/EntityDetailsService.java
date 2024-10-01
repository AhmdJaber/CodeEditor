package com.example.CodeEditor.security;

import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.model.users.editor.EditorDetails;
import com.example.CodeEditor.repository.EditorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EntityDetailsService implements UserDetailsService {
    @Autowired
    private EditorRepository editorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Editor editor = editorRepository.findByEmail(username);
        if (editor == null) {
            System.out.println("Editor not found");
            throw new UsernameNotFoundException(username);
        }
        System.out.println(editor);
        return new EditorDetails(editor);
    }
}