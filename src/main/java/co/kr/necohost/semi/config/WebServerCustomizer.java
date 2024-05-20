package co.kr.necohost.semi.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
abstract class WebServerCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {

        ErrorPage error404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
        ErrorPage error500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
        ErrorPage runtimeError = new ErrorPage(RuntimeException.class, "/error-page/500");


        factory.addErrorPages(error404, error500, runtimeError);
    }
}
