package com.example.boardservice.event;

public class BoardCreatedEvent {
  private Long userId;

  public BoardCreatedEvent(Long userId) {
    this.userId = userId;
  }

  public Long getUserId() {
    return userId;
  }
}
