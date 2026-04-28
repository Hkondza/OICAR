package hr.team16.booksy.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info=@Info(
        title="Booksy Api",
        version="1.0",
        description="REST API for Booksy- property booking platform"
                ),
  servers = @Server(url = "http://localhost:8080" ,description = "Local server")


)
@SecurityScheme(
        name = "bearerAuth",
        type= SecuritySchemeType.HTTP,
        scheme="bearer",
        bearerFormat="JWT"
)


public class OpenApiConfig {
}
