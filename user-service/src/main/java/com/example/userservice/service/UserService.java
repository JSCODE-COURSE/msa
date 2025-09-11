package com.example.userservice.service;

import com.example.userservice.client.PointClient;
import com.example.userservice.domain.User;
import com.example.userservice.domain.UserRepository;
import com.example.userservice.dto.AddActivityScoreRequestDto;
import com.example.userservice.dto.DeductActivityScoreRequestDto;
import com.example.userservice.dto.LoginRequestDto;
import com.example.userservice.dto.LoginResponseDto;
import com.example.userservice.dto.SignUpRequestDto;
import com.example.userservice.dto.UserResponseDto;
import com.example.userservice.event.UserSignedUpEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PointClient pointClient;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final String jwtSecret;

  public UserService(
      UserRepository userRepository,
      PointClient pointClient,
      KafkaTemplate<String, String> kafkaTemplate,
      @Value("${jwt.secret}") String jwtSecret
  ) {
    this.userRepository = userRepository;
    this.pointClient = pointClient;
    this.kafkaTemplate = kafkaTemplate;
    this.jwtSecret = jwtSecret;
  }

  @Transactional
  public void signUp(SignUpRequestDto signUpRequestDto) {
    User user = new User(
      signUpRequestDto.getEmail(),
      signUpRequestDto.getName(),
      signUpRequestDto.getPassword()
    );

    User savedUser = this.userRepository.save(user);

    // 회원가입하면 포인트 1000점 적립
    pointClient.addPoints(savedUser.getUserId(), 1000);

    // '회원가입 완료' 이벤트 발행
    UserSignedUpEvent userSignedUpEvent = new UserSignedUpEvent(
        savedUser.getUserId(),
        savedUser.getName()
    );
    this.kafkaTemplate.send(
        "user.signed-up",
        toJsonString(userSignedUpEvent)
    );
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

  public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    User user = userRepository.findByEmail(loginRequestDto.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    if (!user.getPassword().equals(loginRequestDto.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    // JWT를 만들 때 사용하는 Key 생성 (공식 문서 방식)
    SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    // JWT 토큰 만들기
    String token = Jwts.builder()
        .subject(user.getUserId().toString())
        .signWith(secretKey)
        .compact();

    return new LoginResponseDto(
      token
    );
  }

  public UserResponseDto getUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    return new UserResponseDto(
        user.getUserId(),
        user.getEmail(),
        user.getName()
    );
  }

  public List<UserResponseDto> getUsersByIds(List<Long> ids) {
    List<User> users = userRepository.findAllById(ids);

    return users.stream()
        .map(user -> new UserResponseDto(
            user.getUserId(),
            user.getEmail(),
            user.getName()
        ))
        .collect(Collectors.toList());
  }

  @Transactional
  public void addActivityScore(
      AddActivityScoreRequestDto addActivityScoreRequestDto
  ) {
    User user = userRepository.findById(addActivityScoreRequestDto.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    user.addActivityScore(addActivityScoreRequestDto.getScore());

    userRepository.save(user);
    try {
      Thread.sleep(10000); // 10초 대기
    } catch (Exception e) {}

//    throw new RuntimeException("에러 발생");
  }

  @Transactional
  public void deductActivityScore(DeductActivityScoreRequestDto deductActivityScoreRequestDto) {
    User user = userRepository.findById(deductActivityScoreRequestDto.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    user.deductActivityScore(deductActivityScoreRequestDto.getScore());

    userRepository.save(user);
  }
}
