package com.example.userservice.event;

public class UserSignedUpEvent {
  private Long userId;
  private String name;

  public UserSignedUpEvent(Long userId, String name) {
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
