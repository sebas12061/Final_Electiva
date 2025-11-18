package eamelectiva.microserviciolugar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MicroservicioLugarApplication {  // O el nombre real de tu clase
    public static void main(String[] args) {
        SpringApplication.run(MicroservicioLugarApplication.class, args);
    }
    // Agrega este bean para RestTemplate
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}