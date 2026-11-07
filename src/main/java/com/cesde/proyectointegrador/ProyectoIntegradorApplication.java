package com.cesde.proyectointegrador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class ProyectoIntegradorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProyectoIntegradorApplication.class, args);
        System.out.println("\n=======================================================");
        System.out.println("🚀 BACKEND SPRING BOOT (JAVA) INICIADO EN PUERTO 3000");
        System.out.println("📡 URL Base API: http://localhost:3000/api");
        System.out.println("=======================================================\n");
    }
}
