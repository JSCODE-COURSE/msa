package com.example.userservice.consumer;

import com.example.userservice.dto.AddActivityScoreRequestDto;
import com.example.userservice.event.BoardCreatedEvent;
import com.example.userservice.service.UserService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BoardCreatedEventConsumer {
  private final UserService userService;

  public BoardCreatedEventConsumer(UserService userService) {
    this.userService = userService;
  }

  @KafkaListener(
      topics = "board.created",
      groupId = "user-service"
  )
  public void consume(String message) {
    BoardCreatedEvent boardCreatedEvent
        = BoardCreatedEvent.fromJson(message);

    // 게시글 작성 시 활동 점수 10점 추가
    AddActivityScoreRequestDto addActivityScoreRequestDto
        = new AddActivityScoreRequestDto(
            boardCreatedEvent.getUserId(),
            10
        );
    userService.addActivityScore(addActivityScoreRequestDto);
    System.out.println("활동 점수 적립 완료");
  }
}
