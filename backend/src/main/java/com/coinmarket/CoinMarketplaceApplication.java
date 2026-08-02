package com.coinmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CoinMarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoinMarketplaceApplication.class, args);
    }
}
