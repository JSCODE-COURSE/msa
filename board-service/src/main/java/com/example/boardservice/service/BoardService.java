package com.example.boardservice.service;

import com.example.boardservice.client.PointClient;
import com.example.boardservice.client.UserClient;
import com.example.boardservice.domain.Board;
import com.example.boardservice.domain.BoardRepository;
import com.example.boardservice.dto.BoardResponseDto;
import com.example.boardservice.dto.CreateBoardRequestDto;
import com.example.boardservice.dto.UserDto;
import com.example.boardservice.dto.UserResponseDto;
import com.example.boardservice.event.BoardCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BoardService {
  private final BoardRepository boardRepository;
  private final UserClient userClient;
  private final PointClient pointClient;

  private final KafkaTemplate<String, String> kafkaTemplate;

  public BoardService(
      BoardRepository boardRepository,
      UserClient userClient,
      PointClient pointClient,
      KafkaTemplate<String, String> kafkaTemplate
  ) {
    this.boardRepository = boardRepository;
    this.userClient = userClient;
    this.pointClient = pointClient;
    this.kafkaTemplate = kafkaTemplate;
  }

  public void create(CreateBoardRequestDto createBoardRequestDto, Long userId) {
    // 게시글 저장을 성공했는 지 판단하는 플래그
    boolean isBoardCreated = false;
    Long savedBoardId = null;

    // 포인트 차감을 성공했는 지 판단하는 플래그
    boolean isPointDeducted = false;

    try {
      // 게시글 작성 전 100 포인트 차감
      pointClient.deductPoints(userId, 100);
      isPointDeducted = true; // 포인트 차감 성공 플래그
      System.out.println("포인트 차감 성공");

      // 게시글 작성
      Board board = new Board(
          createBoardRequestDto.getTitle(),
          createBoardRequestDto.getContent(),
          userId
      );

      Board savedBoard = this.boardRepository.save(board);
      savedBoardId = savedBoard.getBoardId();
      isBoardCreated = true; // 게시글 저장 성공 플래그
      System.out.println("게시글 저장 성공");

      // '게시글 작성 완료' 이벤트 발행
      BoardCreatedEvent boardCreatedEvent
          = new BoardCreatedEvent(userId);
      this.kafkaTemplate.send("board.created", toJsonString(boardCreatedEvent));

    } catch (Exception e) {
      if (isBoardCreated) {
        // 게시글 작성 보상 트랜잭션 => 게시글 삭제
        this.boardRepository.deleteById(savedBoardId);
        System.out.println("[보상 트랜잭션] 게시글 삭제");
      }
      if (isPointDeducted) {
        // 포인트 차감 보상 트랜잭션 => 포인트 적립
        pointClient.addPoints(userId, 100);
        System.out.println("[보상 트랜잭션] 포인트 적립");
      }

      // 실패 응답으로 처리하기 위해 예외 던지기
      throw e;
    }
  }

  // 객체를 Json 형태의 String으로 만들어주는 메서드
  // (클래스로 분리하면 더 좋지만 편의를 위해 메서드로만 분리)
  private String toJsonString(Object object) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String message = objectMapper.writeValueAsString(object);
      return message;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Json 직렬화 실패");
    }
  }

  public BoardResponseDto getBoard(Long boardId) {
    // 게시글 불러오기
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    // user-service로부터 사용자 정보 불러오기
    Optional<UserResponseDto> optionalUserResponseDto = userClient.fetchUser(board.getUserId());

    // UserDto 생성
    UserDto userDto = null;
    if (optionalUserResponseDto.isPresent()) {
      UserResponseDto userResponseDto = optionalUserResponseDto.get();
      userDto = new UserDto(
          userResponseDto.getUserId(),
          userResponseDto.getName()
      );
    }

    // BoardResponseDto 생성
    BoardResponseDto boardResponseDto = new BoardResponseDto(
        board.getBoardId(),
        board.getTitle(),
        board.getContent(),
        userDto
    );

    return boardResponseDto;
  }

  // 게시글 전체 조회
  public List<BoardResponseDto> getBoards() {
    List<Board> boards = boardRepository.findAll();

    // userId 목록 추출
    List<Long> userIds = boards.stream()
        .map(Board::getUserId)
        .distinct() // 중복 제거
        .toList();

    // user-service로부터 사용자 정보 불러오기
    List<UserResponseDto> userResponseDtos = userClient.fetchUsersByIds(userIds);

    // userId를 Key로 하는 Map 생성
    Map<Long, UserDto> userMap = new HashMap<>();
    for (UserResponseDto userResponseDto : userResponseDtos) {
      Long userId = userResponseDto.getUserId();
      String name = userResponseDto.getName();
      userMap.put(userId, new UserDto(userId, name));
    }

    // 게시글 정보와 사용자 정보를 조합해서 BoardResponseDto 만들기
    return boards.stream()
        .map(board -> new BoardResponseDto(
            board.getBoardId(),
            board.getTitle(),
            board.getContent(),
            userMap.get(board.getUserId()) // 맵에서 UserDto 가져오기
        ))
        .toList();
  }

  // 연관관계를 활용한 게시글 조회
  public BoardResponseDto getBoard2(Long boardId) {
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

    // BoardResponseDto 생성
    BoardResponseDto boardResponseDto = new BoardResponseDto(
        board.getBoardId(),
        board.getTitle(),
        board.getContent(),
        new UserDto(
            board.getUser().getUserId(),
            board.getUser().getName()
        )
    );

    return boardResponseDto;
  }

  // 연관관계를 활용한 게시글 전체 조회
  public List<BoardResponseDto> getBoards2() {
    List<Board> boards = boardRepository.findAll();

    return boards.stream()
        .map(board -> new BoardResponseDto(
            board.getBoardId(),
            board.getTitle(),
            board.getContent(),
            new UserDto(
                board.getUser().getUserId(),
                board.getUser().getName()
            )
        ))
        .toList();
  }
}
