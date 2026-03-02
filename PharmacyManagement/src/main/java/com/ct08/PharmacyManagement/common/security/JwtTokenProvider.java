package com.ct08.PharmacyManagement.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-milliseconds}")
    private long jwtExpirationDate;

    public String generateToken(Authentication authentication, Integer employeeId, String fullName) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("employeeId", employeeId)
                .claim("fullName", fullName)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            // Token hết hạn là chuyện bình thường -> Dùng log.info hoặc log.debug
            // Đừng dùng log.error kẻo spam log
            log.debug("JWT token expired: {}", ex.getMessage());

        } catch (SecurityException | MalformedJwtException | SignatureException ex) {
            // Đây là lỗi nghiêm trọng (nghi vấn hack) -> Dùng log.error
            log.error("Invalid JWT signature/token: {}", ex.getMessage());

        } catch (IllegalArgumentException ex) {
            // Lỗi do request thiếu thông tin -> Dùng log.warn
            log.debug("JWT claims string is empty: {}", ex.getMessage());

        } catch (UnsupportedJwtException ex) {
            log.debug("Unsupported JWT token: {}", ex.getMessage());
        }

        return false;
    }
}
