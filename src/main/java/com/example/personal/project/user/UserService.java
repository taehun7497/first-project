package com.example.personal.project.user;

import com.example.personal.project.error.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(String username,
                           String email,
                           String password) {

        SiteUser user = new SiteUser();
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(username);
        user.setEmail(email);
        this.userRepository.save(user);
        return user;
    }

    public void modifyPassword(SiteUser modifyUser,
                               String password) {

        modifyUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(modifyUser);
    }

    public boolean isSamePassword(SiteUser user,
                                  String password) {

        return passwordEncoder.matches(password, user.getPassword());
    }


    public SiteUser getUser(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByUsername(username);

        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteUser not found");
        }
    }

    public SiteUser getUserByEmail(String email) {
        Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("Email not found!!");
        }
    }

    public SiteUser update(SiteUser user,
                           String newPassword) {

        user.setPassword(this.passwordEncoder.encode(newPassword));
        this.userRepository.save(user);
        return user;
    }

    public boolean isMatch(String rawPassword,
                           String encodedPassword) {

        return this.passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
