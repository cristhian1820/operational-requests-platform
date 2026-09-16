package co.com.operationalrequests.indicators.config;
import io.swagger.v3.oas.models.*; import io.swagger.v3.oas.models.info.Info; import io.swagger.v3.oas.models.security.*; import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig { @Bean OpenAPI indicatorsApi(){return new OpenAPI().info(new Info().title("Operational Indicators API").version("v1")).components(new Components().addSecuritySchemes("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));} }
