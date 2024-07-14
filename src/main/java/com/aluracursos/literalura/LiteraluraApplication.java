package com.aluracursos.literalura;

import com.aluracursos.literalura.service.BookService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.InputMismatchException;
import java.util.Scanner;

@SpringBootApplication
public class LiteraluraApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiteraluraApplication.class, args);
    }

    @Bean
    CommandLineRunner run(BookService bookService) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println();
                System.out.println("*********************************************");
                System.out.println("1. Buscar libro por título");
                System.out.println("2. Listar libros registrados");
                System.out.println("3. Listar autores registrados");
                System.out.println("4. Listar autores vivos en un año determinado");
                System.out.println("5. Listar libros por idioma");
                System.out.println("0. Salir");
                System.out.print("Selecciona una opción: ");

                int option;
                try {
                    option = scanner.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Por favor, ingrese un número válido.");
                    scanner.nextLine(); // Limpiar entrada inválida
                    continue;
                }
                scanner.nextLine(); // Consumir nueva línea

                switch (option) {
                    case 1:
                        System.out.print("Ingrese el título del libro: ");
                        String title = scanner.nextLine().trim();
                        if (!title.isEmpty()) {
                            bookService.fetchBookByTitle(title);
                        } else {
                            System.out.println("El título del libro no puede estar vacío.");
                        }
                        break;
                    case 2:
                        bookService.listBooks();
                        break;
                    case 3:
                        bookService.listAuthors();
                        break;
                    case 4:
                        System.out.print("Ingrese el año: ");
                        if (scanner.hasNextInt()) {
                            int year = scanner.nextInt();
                            scanner.nextLine(); // Consumir nueva línea
                            bookService.listAuthorsAliveInYear(year);
                        } else {
                            System.out.println("Año inválido. Debe ser un número.");
                            scanner.nextLine(); // Limpiar entrada inválida
                        }
                        break;
                    case 5:
                        System.out.print("Ingrese el idioma (ES, EN, FR, PT): ");
                        String language = scanner.nextLine().trim().toUpperCase();
                        if (language.matches("ES|EN|FR|PT")) {
                            bookService.listBooksByLanguage(language);
                        } else {
                            System.out.println("Idioma inválido. Debe ser uno de los siguientes: ES, EN, FR, PT.");
                        }
                        break;
                    case 0:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            }
        };
    }
}


