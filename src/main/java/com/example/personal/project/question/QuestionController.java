package com.example.personal.project.question;

import com.example.personal.project.answer.Answer;
import com.example.personal.project.answer.AnswerForm;
import com.example.personal.project.answer.AnswerService;
import com.example.personal.project.category.Category;
import com.example.personal.project.category.CategoryService;
import com.example.personal.project.comment.CommentForm;
import com.example.personal.project.user.SiteUser;
import com.example.personal.project.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final AnswerService answerService;

    @RequestMapping("/list/{category}")
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @PathVariable("category") String category,
                       @RequestParam(value = "kw", defaultValue = "") String kw) {

        Category category1 = this.categoryService.getCategoryByTitle(category);
        Page<Question> paging = this.questionService.getList(page, kw, category1);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("category", category);

        return "question_list";
    }

    @RequestMapping(value = "/detail/{id}")
    public String detail(Model model,
                         @RequestParam(value = "so", defaultValue = "recent") String so,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @PathVariable("id") Integer id,
                         AnswerForm answerForm,
                         CommentForm commentForm,
                         Principal principal) {

        Question question = this.questionService.getQuestion(id);

        if (principal != null && !question.getAuthor().getUsername().equals(principal.getName())) {
            this.questionService.incrementView(question);
        }

        Page<Answer> paging = this.answerService.getList(page, question, so);

        model.addAttribute("paging", paging);
        model.addAttribute("question", question);
        model.addAttribute("so", so);

        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create/{category}")
    public String questionCreate(Model model,
                                 @PathVariable("category") String category,
                                 QuestionForm questionForm) {

        model.addAttribute("category", category);
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{category}")
    public String questionCreate(Model model,
                                 @PathVariable("category") String category,
                                 @Valid QuestionForm questionForm,
                                 BindingResult bindingResult,
                                 Principal principal) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("category", category);
            return "question_form";
        }

        SiteUser author = this.userService.getUser(principal.getName());
        Category category1 = this.categoryService.getCategoryByTitle(category);
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), author, category1);
        return String.format("redirect:/question/list/%s", category);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(
            QuestionForm questionForm,
            @PathVariable("id") Integer id,
            Principal principal) {

        Question question = this.questionService.getQuestion(id);

        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(
            @Valid QuestionForm questionForm,
            @PathVariable("id") Integer id,
            Principal principal,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);

        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(
            Principal principal,
            @PathVariable("id") Integer id) {

        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }

        // 질문을 삭제합니다.
        this.questionService.delete(question);

        // 홈 페이지로 리다이렉트합니다.
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근할 수 있도록 설정합니다.
    @GetMapping("/vote/{id}")
    public String questionVote(
            // 현재 사용자의 정보를 가진 Principal 객체입니다.
            Principal principal,
            // URL 경로에서 추출한 질문의 ID를 받습니다.
            @PathVariable("id") Integer id,
            // HTTP 응답 객체입니다.
            HttpServletResponse response) throws IOException {

        // 주어진 ID에 해당하는 질문을 데이터베이스에서 조회합니다.
        Question question = this.questionService.getQuestion(id);

        // 현재 로그인한 사용자가 질문의 작성자가 아닌 경우에만 실행합니다.
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            // 현재 로그인한 사용자의 정보를 가져옵니다.
            SiteUser siteUser = this.userService.getUser(principal.getName());
            // 질문에 대한 투표를 처리합니다.
            this.questionService.vote(question, siteUser);
        } else {
            /**
             * alert 창 후 이동하지 않는 문제가 있음
             */
            // 질문의 작성자가 투표할 수 없음을 알리는 alert 창을 띄운 후, 질문의 상세 페이지로 리다이렉트합니다.
            // ScriptUtils.alertAndMovePage(response, "작성자는 추천이 불가합니다.", String.format("redirect:/question/detail/%s", id));
        }

        // 질문의 상세 페이지로 리다이렉트합니다.
        return String.format("redirect:/question/detail/%s", id);
    }
}