package com.thisispmb.bushraat.config;

import jakarta.servlet.ServletContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public final class ThymeleafConfig {

    private ThymeleafConfig() {
    }

    public static TemplateEngine createTemplateEngine(
            ServletContext servletContext) {

        IWebApplication application =
                JakartaServletWebApplication.buildApplication(servletContext);

        WebApplicationTemplateResolver resolver =
                new WebApplicationTemplateResolver(application);

        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        TemplateEngine templateEngine = new TemplateEngine();

        templateEngine.setTemplateResolver(resolver);

        return templateEngine;
    }
}
