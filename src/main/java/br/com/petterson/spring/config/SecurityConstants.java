package br.com.petterson.spring.config;

public class SecurityConstants {

    static final String SECRET = "TestJWTAuthentication";
    static final String TOKEN_PRIFIX = "Bearer ";
    static final String HEADER_STRING = "Authorization";
    static final String SIGN_UP_URL = "/users/sign-up";
    static final long EXPIRATION_TIME = 86400000L;
}
