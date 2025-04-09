package org.example.educheck.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.educheck.domain.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenUtil {
    public static final long REFRESH_TOKEN_VALIDITY_MILLISECONDS = 1000L * 60 * 60 * 24 * 30;
    private static final long ACCESS_TOKEN_VALIDITY_MILLISECONDS = 1000L * 60 * 30;
    @Value("${JWT_SECRET}")
    private String secretKey;

    @PostConstruct
    protected void init() {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            secretKey = Base64.getEncoder().encodeToString(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    private String createToken(Authentication authentication, long validityMilliSeconds) {


/*        String email = null;
        try {
            Object principal = authentication.getPrincipal();
            Field field = principal.getClass().getDeclaredField("email");
            field.setAccessible(true);
            email = field.get(principal).toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new LoginValidationException();
        }*/
        Member member = (Member) authentication.getPrincipal();
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("roles", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityMilliSeconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {

        return createToken(authentication, REFRESH_TOKEN_VALIDITY_MILLISECONDS);
    }

    public String createAccessToken(Authentication authentication) {

        return createToken(authentication, ACCESS_TOKEN_VALIDITY_MILLISECONDS);
    }

    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}