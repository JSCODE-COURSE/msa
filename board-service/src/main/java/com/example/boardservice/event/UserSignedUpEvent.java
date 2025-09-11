package com.example.boardservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserSignedUpEvent {
  private Long userId;
  private String name;

  // 역직렬화(String 형태의 카프카 메시지 -> Java 객체)시 빈생성자 필요함
  public UserSignedUpEvent() {
  }

  // Json 값을 UserSignedUpEvent로 역직렬화하는 메서드
  public static UserSignedUpEvent fromJson(String json) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(json, UserSignedUpEvent.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 파싱 실패");
    }
  }


  public Long getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }
}
