//package com.anran.partner.config;
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.service.Contact;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
//
///**
// * 自定义 Swagger 接口文档的配置
// */
//@Configuration
//@EnableSwagger2WebMvc
//public class Swagger2Config {
//    /**
//     * 创建API应用
//     * apiInfo() 增加API相关信息
//     * 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现，
//     * 指定扫描的包路径来定义指定要建立API的目录。
//     * @return
//     */
//    @Bean
//    public Docket createRestApi(){
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo())
////                .groupName("adminApi")
//                .select()
//                // 标注控制器的位置
//                .apis(RequestHandlerSelectors.basePackage("com.anran.partner.admin.controller"))
//                .paths(PathSelectors.any())
//                .build();
//    }
//
//    /**
//     * api 信息
//     * @return
//     */
//    private ApiInfo apiInfo(){
//        return new ApiInfoBuilder()
//                .title("双人行")
//                .description("双人行接口文档")
//                .termsOfServiceUrl("http://localhost")
//                .contact(new Contact("anran","http://baidu.com","15804292449@163.com"))
//                .version("1.0")
//                .build();
//    }
//}

package com.anran.partner.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j整合Swagger3 Api接口文档配置类
 *
 */
@Configuration
public class Swagger2Config {

    /**
     * 创建了一个api接口的分组
     * 除了配置文件方式创建分组，也可以通过注册bean创建分组
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                // 分组名称
                .group("partner-api")
                // 接口请求路径规则
                .pathsToMatch("/**")
                .build();
    }

    /**
     * 配置基本信息
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        // 标题
                        .title("双人行接口文档")
                        // 描述Api接口文档的基本信息
                        .description("双人行后端接口信息")
                        // 版本
                        .version("v1.0.0")
                        // 设置OpenAPI文档的联系信息，姓名，邮箱。
                        .contact(new Contact().name("Anran").email("15804292449@163.com"))
                        // 设置OpenAPI文档的许可证信息，包括许可证名称为"Apache 2.0"，许可证URL为"http://springdoc.org"。
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                );
    }
}




