package io.github.jurch29.epargne.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée unique.
 *
 * <p>{@code @SpringBootApplication} = auto-configuration + scan de CE package et de ses sous-packages. Un composant
 * placé en dehors ne serait jamais découvert : c'est la cause n°1 des « 404 alors que mon controller existe ».
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
