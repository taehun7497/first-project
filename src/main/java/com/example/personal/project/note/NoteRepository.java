package com.example.personal.project.note;

import com.example.personal.project.notebook.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByNotebook(Notebook notebook);
}
