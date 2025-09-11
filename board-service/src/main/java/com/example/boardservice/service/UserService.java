package com.example.boardservice.service;

import com.example.boardservice.domain.User;
import com.example.boardservice.domain.UserRepository;
import com.example.boardservice.dto.SaveUserRequestDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public void save(SaveUserRequestDto saveUserRequestDto) {
    User user = new User(
        saveUserRequestDto.getUserId(),
        saveUserRequestDto.getName()
    );

    this.userRepository.save(user);
  }
}
