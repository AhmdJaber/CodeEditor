package com.example.CodeEditor.model.component.files;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class Snippet extends File implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String content;

    public Snippet(String name, Long parentId, Long id) {
        super.setId(id);
        super.setName(name);
        super.setParentId(parentId);
        this.content = initContent(name);
        super.setIsFolder(false); //TODO: remove
    }


    private String initContent(String name) {
        String extension = name.substring(name.lastIndexOf(".") + 1);
        if (extension.equals("cpp")) {
            return "#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n\t//Start Coding\n\tcout << \"Hello World!\";\n}";
        }
        else if (extension.equals("java")) {
            return "public class HelloWorld {\n\n\tpublic static void main(String[] args) {\n\t\t//Start Coding\n\t\tSystem.out.println(\"Hello World!\");\n\t}\n}";
        }
        else if (extension.equals("py")) {
            return "#Start Coding\nprint(\"Hello World!\")";
        }

        return "Nothing\n\n Extension " + extension + " not allowed";
    }

    @Override
    public String toString() {
        return "Snippet{" +
                "name='" + super.getName() + '\'' +
                "parentId='" + super.getParentId() + '\'' +
                "id='" + super.getId() + '\'' +
                "content='" + content + '\'' +
                '}';
    }
}