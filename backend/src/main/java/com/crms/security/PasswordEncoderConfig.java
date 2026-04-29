package com.crms.security;

import com.password4j.Argon2;
import com.password4j.Hash;
import com.password4j.types.Argon2Version;
import com.password4j.types.Argon2id;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    private static final int MEMORY_KIB = 64 * 1024;  // 64 MB
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 4;
    private static final int HASH_LENGTH = 32;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder();
    }

    public static class Argon2PasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            Hash hash = Argon2.Argon2id()
                    .memory(MEMORY_KIB)
                    .iterations(ITERATIONS)
                    .parallelism(PARALLELISM)
                    .hash(HASH_LENGTH, rawPassword.toString());
            return hash.getResult();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return Argon2
                    .from(encodedPassword)
                    .with(Argon2Version.VERSION_19)
                    .<Hash>addParameter(Argon2id.class, memory -> MEMORY_KIB)
                    .verify(rawPassword.toString());
        }
    }
}
