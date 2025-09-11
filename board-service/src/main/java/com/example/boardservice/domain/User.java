package com.example.boardservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

  // Auto Increment 옵션을 사용하지 않을 거기 때문에
  // @GeneratedValue() 어노테이션 작성하지 않음
  @Id
  private Long userId;

  private String name;

  public User() {
  }

  public User(Long userId, String name) {
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
