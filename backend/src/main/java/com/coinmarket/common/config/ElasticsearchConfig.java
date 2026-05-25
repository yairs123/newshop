package com.coinmarket.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.json.JsonpMapper;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public JsonpMapper jsonpMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new JacksonJsonpMapper(mapper);
    }
}
