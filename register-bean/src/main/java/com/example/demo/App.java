package com.example.demo;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

public class App {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        PostRepository posts = new PostRepository();
        PostHandler postHandler = new PostHandler(posts);
        Routes routesBean = new Routes(postHandler);

        context.registerBean(PostRepository.class, () -> posts);
        context.registerBean(PostHandler.class, () -> postHandler);
        context.registerBean(Routes.class, () -> routesBean);
        context.registerBean(WebHandler.class, () -> RouterFunctions.toWebHandler(routesBean.routes(), HandlerStrategies.builder().build()));
        context.refresh();

        HttpHandler handler = WebHttpHandlerBuilder
            //.webHandler(context.getBean(WebHandler.class))
            .applicationContext(context)
            .build();

        // Tomcat and Jetty (also see notes below)
        ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(handler);

        Tomcat tomcatServer = new Tomcat();
        tomcatServer.setHostname(DEFAULT_HOST);
        tomcatServer.setPort(DEFAULT_PORT);
        Context rootContext = tomcatServer.addContext("", System.getProperty("java.io.tmpdir"));
        Tomcat.addServlet(rootContext, "httpHandlerServlet", servlet);
        rootContext.addServletMapping("/", "httpHandlerServlet");
        tomcatServer.start();

        // Reactor Netty
//        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
//        HttpServer.create(DEFAULT_HOST, DEFAULT_PORT).newHandler(adapter).block();
        // Undertow
//        UndertowHttpHandlerAdapter undertowAdapter = new UndertowHttpHandlerAdapter(handler);
//        Undertow server = Undertow.builder().addHttpListener(DEFAULT_PORT, DEFAULT_HOST).setHandler(undertowAdapter).build();
//        server.start();
        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

}
