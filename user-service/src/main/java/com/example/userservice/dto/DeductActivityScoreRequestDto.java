package com.example.userservice.dto;

public class DeductActivityScoreRequestDto {
  private Long userId;
  private int score;

  public Long getUserId() {
    return userId;
  }

  public int getScore() {
    return score;
  }
}
