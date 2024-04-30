package com.example.personal.project;

import com.example.personal.project.note.Note;
import com.example.personal.project.note.NoteRepository;
import com.example.personal.project.note.NoteService;
import com.example.personal.project.notebook.Notebook;
import com.example.personal.project.notebook.NotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
@RequiredArgsConstructor
@Controller
public class MainController {
    private final NotebookRepository notebookRepository;
    private final NoteRepository noteRepository;
    private final NoteService noteService;
    @RequestMapping("/")
    public String main(Model model, Principal principal) {
        if (principal != null) {

            List<Notebook> notebookList = notebookRepository.findAll();
            if (notebookList.isEmpty()) {
                Notebook notebook = new Notebook();
                notebook.setName("새노트");
                notebookRepository.save(notebook);
                return "redirect:/";
            }
            Notebook targetNotebook = notebookList.get(0);
            List<Note> noteList = noteRepository.findByNotebook(targetNotebook);

            if (noteList.isEmpty()) {
                Note note = noteService.saveDefault();
                targetNotebook.addNote(note);
                notebookRepository.save(targetNotebook);
                return "redirect:/";
            }

            model.addAttribute("noteList", noteList);
            model.addAttribute("targetNote", noteList.get(0));
            model.addAttribute("notebookList", notebookList);
            model.addAttribute("targetNotebook", targetNotebook);
            return "main";
        } else {
            return "redirect:/user/login";
        }
    }
}
