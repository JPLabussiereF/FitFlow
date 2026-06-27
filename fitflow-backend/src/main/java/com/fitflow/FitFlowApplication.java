package com.fitflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication é um atalho para três anotações:
//   @Configuration      — esta classe pode definir beans (objetos gerenciados pelo Spring)
//   @EnableAutoConfiguration — o Spring detecta as libs no classpath e configura automaticamente
//                              (ex: viu PostgreSQL + JPA → configura DataSource sozinho)
//   @ComponentScan      — escaneia este pacote e subpacotes buscando @Service, @Controller, etc.
@SpringBootApplication
public class FitFlowApplication {

    public static void main(String[] args) {
        // Inicializa o contexto do Spring, sobe o servidor Tomcat embutido
        // e deixa a API pronta para receber requisições
        SpringApplication.run(FitFlowApplication.class, args);
    }
}
