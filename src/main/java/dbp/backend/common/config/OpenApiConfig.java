package dbp.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    public static final String SESSION_COOKIE_SECURITY_SCHEME = "sessionCookie";

    @Bean
    public OpenAPI databaseProgrammingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Database Programming API")
                        .description("로그인이 필요한 API는 먼저 로그인 API를 호출해 JSESSIONID 세션 쿠키를 발급받아야 합니다.")
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080"))
                )
                .components(new Components().addSecuritySchemes(
                        SESSION_COOKIE_SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("로그인 성공 후 서버가 발급하는 HTTP 세션 쿠키입니다.")));
    }
}
