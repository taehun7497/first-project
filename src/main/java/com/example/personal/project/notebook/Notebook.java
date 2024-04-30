package com.example.personal.project.notebook;

import com.example.personal.project.note.Note;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Notebook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    private Notebook parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notebook> children = new ArrayList<>();

    @OneToMany(mappedBy = "notebook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> noteList = new ArrayList<>();

    public void addChild(Notebook child) {
        child.setParent(this);
        children.add(child);
    }

    public void addNote(Note note) {
        note.setNotebook(this);
        noteList.add(note);
    }

    public void removeChild(Notebook child) {
        if (children.remove(child)) {
            child.setParent(null);
        }
    }
}