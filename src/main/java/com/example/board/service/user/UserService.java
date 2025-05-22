package com.example.board.service.user;

import com.example.board.dto.user.AddUserRequest;
import com.example.board.entity.user.User;
import com.example.board.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void save(AddUserRequest request) {
        userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build());
        System.out.println("SAVE 회원가입 시도 - email: " + request.getEmail());
        System.out.println("SAVE 회원가입 시도 - password: " + request.getPassword());
    }
}
