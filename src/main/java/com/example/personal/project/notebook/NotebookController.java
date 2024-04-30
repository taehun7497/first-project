package com.example.personal.project.notebook;

import com.example.personal.project.note.Note;
import com.example.personal.project.note.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class NotebookController {
    private final NotebookRepository notebookRepository;
    private final NoteService noteService;
    private final  NotebookService notebookService;

    @PostMapping("/books/write")
    public String write() {
        Notebook notebook = new Notebook();
        notebook.setName("새노트북");

        Note note = noteService.saveDefault();
        notebook.addNote(note);

        notebookRepository.save(notebook);
        return "redirect:/";

    }
    @PostMapping("/groups/{notebookId}/books/write")
    public String groupWrite(@PathVariable("notebookId") Long notebookId) {
        Notebook parent = notebookRepository.findById(notebookId).orElseThrow();

        Notebook child = new Notebook();
        child.setName("새노트북");

        Note note = noteService.saveDefault();
        child.addNote(note);
        notebookRepository.save(child);

        parent.addChild(child);
        notebookRepository.save(parent);
        return "redirect:/";
    }
    @GetMapping("/books/{id}")
    public String detail(@PathVariable("id") Long id) {
        Notebook notebook = notebookRepository.findById(id).orElseThrow();
        Note note = notebook.getNoteList().get(0);
        return "redirect:/books/%d/notes/%d".formatted(id, note.getId());
    }

    @PostMapping("/books/{id}/delete")
    public String deleteNotebook(@PathVariable("id") Long id) {
        notebookService.deleteNotebook(id);
        return "redirect:/";
    }

    @PostMapping("/books/{id}/update")
    public String updateNotebookTitle(@PathVariable("id") Long id, @RequestParam("title") String title) {
        notebookService.updateNotebookTitle(id, title);
        return "redirect:/books/{id}";
    }
}