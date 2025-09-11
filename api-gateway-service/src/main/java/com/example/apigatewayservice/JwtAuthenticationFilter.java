package com.example.apigatewayservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Override
  public GatewayFilter apply(Object config) {
    return (exchange, chain) -> {
      // Reqeuest Header에서 토큰 가져오기
      String token = exchange.getRequest()
          .getHeaders()
          .getFirst("Authorization");
      System.out.println("토큰 : " + token);

      // 토큰이 없을 경우 401 Unauthorized로 응답
      if (token == null) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      // SecretKey로 토큰 검증 및 Payload(userId 담겨있음) 가져오기
      SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
      String subject = Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token)
          .getPayload()
          .getSubject();
      System.out.println("userId : " + subject);

      // Payload를 X-User-Id 헤더에 담아서 Request 전달
      // (= 다른 마이크로서비스에 요청 전달할 때 userId 정보를 담아서 보냄)
      return chain.filter(
          exchange.mutate()
              .request(
                  exchange.getRequest()
                      .mutate()
                      .header("X-User-Id", subject)
                      .build()
              )
              .build()
      );
    };
  }
}