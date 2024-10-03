package com.example.CodeEditor.model.component.files;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class Folder extends File implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Folder(String name, Long parentId, Long id) {
        super.setId(id);
        super.setName(name);
        super.setParentId(parentId);
        super.setIsFolder(true); //TODO: remove
    }

    @Override
    public String toString() {
        return "Folder{" +
                "name='" + super.getName() + '\'' +
                ", parentId=" + super.getParentId() +
                ", id=" + super.getId() + '\'' +
                '}';
    }
}

//TODO: what to do if the user created a folder?
//TODO: do we need to create an ID for each folder?
//TODO: if we don't, how can we know the parent of the folder to be added?
//TODO: as we are adding the folder from the front-end, can we know the parent id from there (front-end)?
//TODO: if yes, can we simply return the id of the parent to the folder?
//TODO: but how could folders have an ID stored in the front-end?
//TODO: can we return it from the here (back-end)?

