package com.example.boardservice.dto;

public class SaveUserRequestDto {
  private Long userId;
  private String name;

  public SaveUserRequestDto(Long userId, String name) {
    this.userId = userId;
    this.name = name;
  }

  public Long getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }
}
