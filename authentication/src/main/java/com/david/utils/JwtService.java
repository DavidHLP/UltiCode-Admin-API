package com.david.utils;

import com.david.mapper.TokenMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

//  @Value("${application.security.jwt.secret-key}")
  private String secretKey = "L25xa3JpanBoc3Zkc3J4b2N4a2tnaG9wZ2wzaG9iNXN0bXN0Y21kbXN0Y21k";

//  @Value("${application.security.jwt.expiration}")
  private long jwtExpiration = 1000 * 60 * 24;

//  @Value("${application.security.jwt.refresh-token.expiration}")
  private long refreshExpiration = 1000 * 60 * 60 * 24 * 7;
  private final TokenMapper tokenMapper;

  /**
   * 从令牌中提取用户名。
   *
   * @param token JWT 令牌。
   * @return 用户名。
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * 从令牌中提取指定声明信息。
   *
   * @param token JWT 令牌。
   * @param claimsResolver 声明解析函数。
   * @return 声明信息。
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    return claimsResolver.apply(extractAllClaims(token));
  }

  /**
   * 生成 JWT 令牌。
   *
   * @param username 用户详细信息。
   * @return JWT 令牌。
   */
  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24小时
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * 生成带额外声明的 JWT 令牌。
   *
   * @param extraClaims 额外声明。
   * @param userDetails 用户详细信息。
   * @return JWT 令牌。
   */
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  /**
   * 生成刷新令牌。
   *
   * @param userDetails 用户详细信息。
   * @return 刷新令牌。
   */
  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration);
  }

  /**
   * 验证令牌是否有效。
   *
   * @param jwt JWT 令牌。
   * @param userDetails 用户详细信息。
   * @return 是否有效。
   */
  public boolean isTokenValid(String jwt, UserDetails userDetails) {
    final String username = extractUsername(jwt);
    boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(jwt);

    if (!isValid) {
      log.warn("Token验证失败：用户名不匹配或Token已过期，用户名: {}", username);
    }

    return isValid;
  }

  /**
   * 检查令牌是否已过期。
   *
   * @param token JWT 令牌。
   * @return 是否过期。
   */
  private boolean isTokenExpired(String token) {
    Date expiration = extractExpiration(token);
    boolean isExpired = expiration.before(new Date());

    if (isExpired) {
      log.warn("Token已过期，过期时间: {}", expiration);
    }

    return isExpired;
  }

  /**
   * 提取令牌的过期时间。
   *
   * @param token JWT 令牌。
   * @return 过期时间。
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * 构建 JWT 令牌。
   *
   * @param extraClaims 额外声明。
   * @param userDetails 用户详细信息。
   * @param expiration 有效期（毫秒）。
   * @return JWT 令牌。
   */
  private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * 提取令牌中的所有声明。
   *
   * @param token JWT 令牌。
   * @return 声明。
   */
  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parserBuilder()
              .setSigningKey(getSignInKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
    } catch (ExpiredJwtException e) {
      log.warn("解析过期的Token: {}", e.getMessage());
      throw e;
    } catch (UnsupportedJwtException | MalformedJwtException  | IllegalArgumentException e) {
      log.error("Token解析失败: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * 获取签名秘钥。
   *
   * @return 签名秘钥。
   */
  private Key getSignInKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
  }
}

