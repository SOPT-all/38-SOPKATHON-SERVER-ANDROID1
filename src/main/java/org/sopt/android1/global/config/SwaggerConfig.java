package org.sopt.android1.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		Server devServer = new Server().url("http://localhost:8080/").description("개발 서버");
		Server prodServer = new Server().url("https://sopkathon.o-r.kr/").description("운영 서버");

		return new OpenAPI()
			.servers(List.of(devServer, prodServer))
			.info(getInfo());
	}

	private Info getInfo() {
		return new Info()
			.version("0.1.0")
			.title("SOPT 38th SOPKATHON API")
			.description("SOPT 안드 1팀 솝커톤 API 명세서");
	}

}
