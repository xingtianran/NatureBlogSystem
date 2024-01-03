package net.nature.blog;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.nature.blog.utils.IdWorker;
import net.nature.blog.utils.RedisUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.text.SimpleDateFormat;
import java.util.Random;

@Slf4j
@MapperScan("net.nature.blog.mapper")
@EnableSwagger2
@SpringBootApplication
public class BlogApplication {
    public static void main(String[] args) {
        log.info("BlogApplication run...");
        SpringApplication.run(BlogApplication.class, args);
    }
    @Bean
    public IdWorker createIdWorker(){
        return new IdWorker(0,0);
    }

    @Bean
    public BCryptPasswordEncoder createPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RedisUtil createRedisUtil(){
        return new RedisUtil();
    }

    @Bean
    public Random createRandom(){
        return new Random();
    }

    @Bean
    public SimpleDateFormat createDataFormat(){
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    @Bean
    public Gson createGson(){
        return new Gson();
    }
}
