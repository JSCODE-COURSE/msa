package com.example.boardservice.dto;

public class DeductPointsRequestDto {
  private Long userId;
  private int amount;

  public DeductPointsRequestDto(Long userId, int amount) {
    this.userId = userId;
    this.amount = amount;
  }

  public Long getUserId() {
    return userId;
  }

  public int getAmount() {
    return amount;
  }
}
