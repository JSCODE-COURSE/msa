package com.example.userservice.controller;

import com.example.userservice.dto.AddActivityScoreRequestDto;
import com.example.userservice.dto.DeductActivityScoreRequestDto;
import com.example.userservice.dto.UserResponseDto;
import com.example.userservice.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class UserInternalController {
  private final UserService userService;

  public UserInternalController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("{userId}")
  public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
    UserResponseDto userResponseDto = userService.getUser(userId);
    return ResponseEntity.ok(userResponseDto);
  }

  @GetMapping()
  public ResponseEntity<List<UserResponseDto>> getUsersByIds(
      @RequestParam List<Long> ids
  ) {
    List<UserResponseDto> userResponseDtos = userService.getUsersByIds(ids);
    return ResponseEntity.ok(userResponseDtos);
  }

  @PostMapping("activity-score/add")
  public ResponseEntity<Void> addActivityScore(
      @RequestBody AddActivityScoreRequestDto addActivityScoreRequestDto
  ) {
    userService.addActivityScore(addActivityScoreRequestDto);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("activity-score/deduct")
  public ResponseEntity<Void> deductActivityScore(
      @RequestBody DeductActivityScoreRequestDto deductActivityScoreRequestDto
  ) {
    userService.deductActivityScore(deductActivityScoreRequestDto);
    return ResponseEntity.noContent().build();
  }
}

