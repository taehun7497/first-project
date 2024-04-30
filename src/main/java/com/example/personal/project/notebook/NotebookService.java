package com.example.personal.project.notebook;

import com.example.personal.project.note.NoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class NotebookService {
    private final NotebookRepository notebookRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public void deleteNotebook(Long notebookId) {
        Notebook notebook = notebookRepository.findById(notebookId)
                .orElseThrow(() -> new NoSuchElementException("Notebook not found"));

        if (notebook.getNoteList() != null) {
            notebook.getNoteList().forEach(noteRepository::delete);
        }

        if (notebook.getChildren() != null) {
            notebook.getChildren().forEach(child -> {
                deleteNotebook(child.getId());
            });
        }

        notebookRepository.delete(notebook);
    }

    @Transactional
    public void updateNotebookTitle(Long notebookId, String newTitle) {
        Notebook notebook = notebookRepository.findById(notebookId)
                .orElseThrow(() -> new NoSuchElementException("Notebook not found"));

        if (newTitle == null || newTitle.trim().isEmpty()) {
            newTitle = "제목 없음";
        }

        notebook.setName(newTitle);
        notebookRepository.save(notebook);
    }
}