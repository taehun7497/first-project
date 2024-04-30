package com.example.personal.project.user;

import com.example.personal.project.Message;
import com.example.personal.project.answer.Answer;
import com.example.personal.project.answer.AnswerService;
import com.example.personal.project.error.DataNotFoundException;
import com.example.personal.project.question.Question;
import com.example.personal.project.question.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final JavaMailSender mailSender;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public String getInfo(Model model,
                          Principal principal) {

        SiteUser user = this.userService.getUser(principal.getName());
        List<Question> questionList = this.questionService.getQuestions(user);
        List<Answer> answerList = this.answerService.getListByAuthor(user);

        model.addAttribute("answers", answerList);
        model.addAttribute("questions", questionList);
        model.addAttribute("user", user);
        return "user_info";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/password")
    public String modifyPassword(PasswordModifyForm passwordModifyForm) {
        return "modify_password_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/password")
    public String modifyPassword(@Valid PasswordModifyForm passwordModifyForm,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 Model model) {

        SiteUser user = this.userService.getUser(principal.getName());

        if (bindingResult.hasErrors()) {
            return "modify_password_form";
        }

        if (!this.userService.isSamePassword(user, passwordModifyForm.getBeforePassword())) {
            bindingResult.rejectValue("beforePassword", "notBeforePassword", "이전 비밀번호와 일치하지 않습니다. ");
            return "modify_password_form";
        }

        if (!passwordModifyForm.getPassword1().equals(passwordModifyForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "modify_password_form";
        }

        try {
            userService.modifyPassword(user, passwordModifyForm.getPassword1());
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("modifyPasswordFailed", e.getMessage());
            return "modify_password_form";
        }

        model.addAttribute("data", new Message("비밀번호 변경 되었습니다.", "/user/info"));
        return "message";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        return "redirect:/";
    }

    @GetMapping("/find-account")
    public String findAccount(Model model) {
        model.addAttribute("sendConfirm", false);
        model.addAttribute("error", false);
        return "find_account";
    }

    @PostMapping("/find-account")
    public String findAccount(Model model,
                              @RequestParam(value = "email") String email) {

        try {
            SiteUser siteUser = this.userService.getUserByEmail(email);
            model.addAttribute("sendConfirm", true);
            model.addAttribute("userEmail", email);
            model.addAttribute("error", false);
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(email);
            simpleMailMessage.setSubject("계정 정보입니다.");
            StringBuffer sb = new StringBuffer();

            String newPassword = PasswordGenerator.generateRandomPassword();
            sb.append(siteUser.getUsername()).append("계정의 비밀번호를 새롭게 초기화 했습니다..\n").append("새 비밀번호는 ")
                    .append(newPassword).append("입니다.\n")
                    .append("로그인 후 내 정보에서 새로 비밀번호를 지정해주세요.");
            simpleMailMessage.setText(sb.toString());
            this.userService.update(siteUser, newPassword);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mailSender.send(simpleMailMessage);
                }
            }).start();
        } catch (DataNotFoundException e) {
            model.addAttribute("sendConfirm", false);
            model.addAttribute("error", true);
        }
        return "find_account";
    }

    public static class PasswordGenerator {
        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";

        private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
        private static final int PASSWORD_LENGTH = 12;

        public static String generateRandomPassword() {
            if (PASSWORD_LENGTH < 1) throw new IllegalArgumentException("Password length must be at least 1");

            StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
            Random random = new SecureRandom();

            for (int i = 0; i < PASSWORD_LENGTH; i++) {
                int rndCharAt = random.nextInt(PASSWORD_ALLOW_BASE.length());
                char rndChar = PASSWORD_ALLOW_BASE.charAt(rndCharAt);
                sb.append(rndChar);
            }
            return sb.toString();
        }
    }
}