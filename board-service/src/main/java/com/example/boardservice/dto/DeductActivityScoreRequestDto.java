package com.example.boardservice.dto;

public class DeductActivityScoreRequestDto {
  private Long userId;
  private int score;

  public DeductActivityScoreRequestDto(Long userId, int score) {
    this.userId = userId;
    this.score = score;
  }

  public Long getUserId() {
    return userId;
  }

  public int getScore() {
    return score;
  }
}
