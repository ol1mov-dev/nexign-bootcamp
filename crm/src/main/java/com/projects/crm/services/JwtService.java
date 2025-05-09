package com.projects.crm.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKey = "dcd7793ad4b04c9981e78ad04909ae61136ef926ea40d7d1e97bcbfffdf35d1f99de52bda939c7a8865c072e33f6de2b8f259d05758224ccf9051b30b9852785b7d0fd30f061c6681b8232beea7826bf84b392553f611b730fc8e7519702fa35861c6dde9206c0888cc55b316e2fa25c42c28c93746f3f7f46d69f4fdaa14057bf6cc7f2296507ebf32fc355a22aa1c8707f8088f09570480a61275a962a1238e6fc956dbebd2c1223c4b7571aba8b2cb8eafd1c9b7624296822df162c82644acf795fda198e1cdf0376455bd6de0083d9da9de79d35b119cfeefad6612e208f7a8d6ea2d70c5871575085da911d5cc161258826b658f779f1fbe7b5c32ec94a";
    private final int tokenExpiration = 604800000;

    public String generateToken(String subject) {
        Map<String, Object> claims = new HashMap<String, Object>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .and()
                .signWith(getKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private String getSecretKey(){
        return secretKey;
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = getEmailFromToken(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}