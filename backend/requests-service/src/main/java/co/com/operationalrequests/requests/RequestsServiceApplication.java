package co.com.operationalrequests.requests;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication @EnableScheduling public class RequestsServiceApplication { public static void main(String[] args) { SpringApplication.run(RequestsServiceApplication.class, args); } }
