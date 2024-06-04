package co.kr.necohost.semi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@EnableCaching
public class MVCConfiguration implements WebMvcConfigurer {
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage error404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
            ErrorPage error500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
            ErrorPage runtimeError = new ErrorPage(RuntimeException.class, "/error-page/500");
            ErrorPage error400 = new ErrorPage(HttpStatus.BAD_REQUEST, "/error-page/400");
            ErrorPage error401 = new ErrorPage(HttpStatus.UNAUTHORIZED, "/error-page/401");

            factory.addErrorPages(error400, error401, error404, error500, runtimeError);
        };
    }

    @GetMapping("/redirected")
    public String redirected() {
        return "redirected";
    }

    @Value("${project.dir}")
    private String projectDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("file:"+projectDir+"/src/main/resources/static/").setCachePeriod(3600);
    }

    @Bean
    public ConcurrentMapCacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "categories",
                "cat_menus"
        );
    }
}
