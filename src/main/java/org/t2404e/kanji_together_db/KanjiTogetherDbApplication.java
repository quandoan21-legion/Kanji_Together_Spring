package org.t2404e.kanji_together_db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KanjiTogetherDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(KanjiTogetherDbApplication.class, args);
    }

}
