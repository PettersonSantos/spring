package br.com.petterson.spring.docs;

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket apiDoc() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("br.com.petterson.spring.endpoint"))
            .paths(PathSelectors.regex("/v1.*"))
            .build()
            .globalOperationParameters(Collections.singletonList(new ParameterBuilder()
                .name("Authorization")
                .description("Bearer Token")
                .modelRef(new ModelRef("struing"))
                .parameterType("header")
                .required(true)
                .build()))
            .apiInfo(metaData());
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
            .title("Aprendizado de Spring Boot")
            .description("Ficou Bom")
            .version("1.0")
            .contact(new Contact("Petterson", "http://localhost:8080/kkk",
                "petterson.santos@outlook.com.br"))
            .license("Apache License Version 2.0")
            .licenseUrl("https://www.apache.org/license/LICENSE-2.0")
            .build();
    }
}
