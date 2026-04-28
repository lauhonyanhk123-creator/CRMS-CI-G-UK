package com.crms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Register serializers for proper formatting
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer("yyyy-MM-dd'T'HH:mm:ss"));
        javaTimeModule.addSerializer(LocalDate.class, 
                new LocalDateSerializer("yyyy-MM-dd"));
        javaTimeModule.addSerializer(YearMonth.class, 
                new YearMonthSerializer("yyyy-MM"));
        
        // Register deserializers
        javaTimeModule.addDeserializer(LocalDateTime.class, 
                new LocalDateTimeDeserializer("yyyy-MM-dd'T'HH:mm:ss"));
        javaTimeModule.addDeserializer(LocalDate.class, 
                new LocalDateDeserializer());
        javaTimeModule.addDeserializer(YearMonth.class, 
                new YearMonthDeserializer());
        
        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}
