package com.example.boardservice.controller;

import com.example.boardservice.dto.BoardResponseDto;
import com.example.boardservice.dto.CreateBoardRequestDto;
import com.example.boardservice.service.BoardService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
  private final BoardService boardService;

  public BoardController(BoardService boardService) {
    this.boardService = boardService;
  }

  @PostMapping
  public ResponseEntity<Void> create(
      @RequestBody CreateBoardRequestDto createBoardRequestDto,
      @RequestHeader("X-User-Id") Long userId
  ) {
    boardService.create(createBoardRequestDto, userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{boardId}")
  public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long boardId) {
//    BoardResponseDto boardResponseDto = boardService.getBoard(boardId);
    BoardResponseDto boardResponseDto = boardService.getBoard2(boardId);
    return ResponseEntity.ok(boardResponseDto);
  }

  @GetMapping()
  public ResponseEntity<List<BoardResponseDto>> getBoards() {
    // List<BoardResponseDto> boardResponseDtos = boardService.getBoards();
    List<BoardResponseDto> boardResponseDtos = boardService.getBoards2();
    return ResponseEntity.ok(boardResponseDtos);
  }
}
