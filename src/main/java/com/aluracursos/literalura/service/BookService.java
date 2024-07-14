package com.aluracursos.literalura.service;

import com.aluracursos.literalura.model.Author;
import com.aluracursos.literalura.model.Book;
import com.aluracursos.literalura.repository.AuthorRepository;
import com.aluracursos.literalura.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private final String API_URL = "https://gutendex.com/books?search=";

    public Book fetchBookByTitle(String title) {

        RestTemplate restTemplate = new RestTemplate();
        String url = API_URL + title;
        String response = restTemplate.getForObject(url, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode bookData = root.path("results").get(0); // Asumiendo que tomamos el primer resultado

            if (bookData.isMissingNode()) {
                System.out.println("No se encontró el libro.");
                return null;
            }

            String bookTitle = bookData.path("title").asText();
            String language = bookData.path("languages").get(0).asText(); // Aseguramos que tomamos el primer idioma de la lista
            int downloads = bookData.path("download_count").asInt();
            JsonNode authorData = bookData.path("authors").get(0);
            String authorName = authorData.path("name").asText();

            // Verificar si el libro ya existe en la base de datos
            if (bookRepository.existsByTitle(bookTitle)) {
                System.out.println("El libro ya está registrado en la base de datos.");
                return null;
            }

            // Asignar datos del autor
            String[] authorParts = authorName.split(", ");
            String lastName = authorParts[0];
            String firstName = authorParts.length > 1 ? authorParts[1] : "";

            // Obtener fechas de nacimiento y fallecimiento
            String birthDateStr = authorData.path("birth_year").asText(null);
            String deathDateStr = authorData.path("death_year").asText(null);
            LocalDate birthDate = birthDateStr != null ? LocalDate.of(Integer.parseInt(birthDateStr), 1, 1) : null;
            LocalDate deathDate = deathDateStr != null ? LocalDate.of(Integer.parseInt(deathDateStr), 1, 1) : null;

            Optional<Author> authorOptional = authorRepository.findByFirstNameAndLastName(firstName, lastName);
            Author author;
            if (authorOptional.isPresent()) {
                author = authorOptional.get();
            } else {
                author = new Author();
                author.setFirstName(firstName);
                author.setLastName(lastName);
                author.setBirthDate(birthDate);
                author.setDeathDate(deathDate);
                authorRepository.save(author);
            }

            Book book = new Book();
            book.setTitle(bookTitle);
            book.setLanguage(language);
            book.setDownloads(downloads);
            book.setAuthor(author);

            Book savedBook = bookRepository.save(book);

            // Mostrar los datos del libro en la consola
            System.out.println("********* LIBRO *********");
            System.out.println("Título: " + savedBook.getTitle());
            System.out.println("Autor: " + savedBook.getAuthor().getLastName() + ", " + savedBook.getAuthor().getFirstName());
            System.out.println("Idioma: " + savedBook.getLanguage());
            System.out.println("Número de Descargas: " + savedBook.getDownloads());
            System.out.println("*************************");
            //System.out.println();

            return savedBook;
        } catch (Exception e) {
            System.out.println("Error al procesar la respuesta de la API: " + e.getMessage());
            return null;
        }
    }

    public void listBooks() {
        bookRepository.findAll().forEach(book -> {
            System.out.println("********* LIBRO *********");
            System.out.println("Título: " + book.getTitle());
            System.out.println("Autor: " + book.getAuthor().getLastName() + ", " + book.getAuthor().getFirstName());
            System.out.println("Idioma: " + book.getLanguage());
            System.out.println("Descargas: " + book.getDownloads());
            System.out.println();
        });
    }

    public void listAuthors() {
        authorRepository.findAll().forEach(author -> {
            System.out.println("********* AUTOR *********");
            System.out.println("Autor: " + author.getLastName() + ", " + author.getFirstName());
            System.out.println("Fecha de Nacimiento: " + (author.getBirthDate() != null ? author.getBirthDate() : "Desconocida"));
            System.out.println("Fecha de Fallecimiento: " + (author.getDeathDate() != null ? author.getDeathDate() : "Desconocida"));
            System.out.println("Libros Registrados:");
            author.getBooks().forEach(book -> {
                System.out.println("\tTítulo: " + book.getTitle());
                //System.out.println("\tIdioma: " + book.getLanguage());
                //System.out.println("\tDescargas: " + book.getDownloads());
            });
            System.out.println();
        });
    }

    public void listAuthorsAliveInYear(int year) {
        authorRepository.findAll().forEach(author -> {
            if ((author.getBirthDate() == null || author.getBirthDate().getYear() <= year) &&
                    (author.getDeathDate() == null || author.getDeathDate().getYear() >= year)) {
                System.out.println("********* AUTOR *********");
                System.out.println("Autor: " + author.getLastName() + ", " + author.getFirstName());
                System.out.println("Fecha de Nacimiento: " + (author.getBirthDate() != null ? author.getBirthDate() : "Desconocida"));
                System.out.println("Fecha de Fallecimiento: " + (author.getDeathDate() != null ? author.getDeathDate() : "Desconocida"));
                System.out.println("Libros Registrados:");
                author.getBooks().forEach(book -> {
                    System.out.println("\tTítulo: " + book.getTitle());
                    //System.out.println("\tIdioma: " + book.getLanguage());
                    //System.out.println("\tDescargas: " + book.getDownloads());
                });
                System.out.println();
            }
        });
    }

    public void listBooksByLanguage(String language) {
        bookRepository.findAll().forEach(book -> {
            if (book.getLanguage().equalsIgnoreCase(language)) {
                System.out.println("********* LIBRO *********");
                System.out.println("Título: " + book.getTitle());
                System.out.println("Autor: " + book.getAuthor().getLastName() + ", " + book.getAuthor().getFirstName());
                System.out.println("Idioma: " + book.getLanguage());
                System.out.println("Descargas: " + book.getDownloads());
                System.out.println();
            }
        });
    }
}

