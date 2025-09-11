package com.example.userservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BoardCreatedEvent {
  private Long userId;

  // 역직렬화(String 형태의 카프카 메시지 -> Java 객체)시 빈생성자 필요함
  public BoardCreatedEvent() {
  }

  // Json 값을 BoardCreatedEvent로 역직렬화하는 메서드
  public static BoardCreatedEvent fromJson(String json) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(json, BoardCreatedEvent.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 파싱 실패");
    }
  }

  public Long getUserId() {
    return userId;
  }
}