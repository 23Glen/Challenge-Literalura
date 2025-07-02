package com.gonzalezglenda.challengeLiteralura.principal;

import com.gonzalezglenda.challengeLiteralura.model.*;
import com.gonzalezglenda.challengeLiteralura.repository.AutorRepository;
import com.gonzalezglenda.challengeLiteralura.service.ConsumoApi;
import com.gonzalezglenda.challengeLiteralura.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private AutorRepository repository;

    public Principal(AutorRepository repository) {
        this.repository = repository;
    }

    public void muestraElMenu() {

        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    ▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼
                    \n   BIBLIOTECA LITERALURA \n
                    1 - Buscar libro por título
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma
                    0 - Salir
                    ▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼
                    """;

            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> listarLibrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivos();
                case 5 -> listarLibrosPorIdioma();
                case 0 -> System.out.println("Cerrando aplicación ... \n");
                default -> System.out.println("Opción inválida");
            }
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Ingresa el título del libro que desea buscar: ");
        var titulo = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + "?search=" + titulo.replace(" ", "+").toLowerCase());

        if (json.isEmpty() || json.contains("\"count\":0,\"next\":null,\"previous\":null,\"results\":[]")) {
            System.out.println("Libro no encontrado");
            return;
        }

        var datos = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibro> libroBuscado = datos.resultados().stream().findFirst();

        if (libroBuscado.isEmpty()) {
            System.out.println("Libro no encontrado");
            return;
        }

        DatosLibro datosLibro = libroBuscado.get();

        System.out.println(
                "\n------------- LIBRO --------------" +
                        "\nTítulo: " + datosLibro.titulo() +
                        "\nAutor: " + datosLibro.autores().stream()
                        .map(DatosAutor::nombre).collect(Collectors.joining(", ")) +
                        "\nIdioma: " + String.join(", ", datosLibro.idiomas()) +
                        "\nNúmero de descargas: " + datosLibro.numeroDeDescargas() +
                        "\n--------------------------------------\n"
        );

        Optional<Libro> libroExistente = repository.buscarLibroPorNombre(datosLibro.titulo());

        if (libroExistente.isPresent()) {
            System.out.println("El libro ya está guardado en la base de datos.");
            return;
        }

        // Crear autor y libro, enlazarlos
        Autor autor = new Autor(datosLibro.autores().get(0));
        Libro libro = new Libro(datosLibro);
        libro.setAutor(autor);
        autor.setLibros(List.of(libro));

        repository.save(autor);
        System.out.println("Libro y autor guardados exitosamente.");
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = repository.librosRegistrados();
        libros.forEach(System.out::println);
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = repository.findAll();
        autores.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(System.out::println);
    }

    private void listarAutoresVivos() {
        System.out.println("Ingrese un año para verificar el autor(es) que desea buscar: ");

        try {
            var fecha = Integer.parseInt(teclado.nextLine());
            List<Autor> autores = repository.listarAutoresVivos(fecha);

            if (!autores.isEmpty()) {
                autores.stream()
                        .sorted(Comparator.comparing(Autor::getNombre))
                        .forEach(System.out::println);
            } else {
                System.out.println("Ningún autor vivo encontrado en este año.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese un año válido. " + e.getMessage());
        }
    }

    private void listarLibrosPorIdioma() {
        var menuIdiomas = """
                *****************************
                Elija una opción: 
                
                1 - Inglés
                2 - Español
                3 - Francés
                4 - Portugués
                5 - Alemán
                
                0 - Regresar
                *****************************
                """;
        System.out.println(menuIdiomas);

        try {
            var opcionIdioma = Integer.parseInt(teclado.nextLine());

            switch (opcionIdioma) {
                case 1 -> buscarLibrosPorIdioma("en");
                case 2 -> buscarLibrosPorIdioma("es");
                case 3 -> buscarLibrosPorIdioma("fr");
                case 4 -> buscarLibrosPorIdioma("pt");
                case 5 -> buscarLibrosPorIdioma("de");
                case 0 -> System.out.println("Regresando ...");
                default -> System.out.println("Ingrese una opción válida: ");
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida: " + e.getMessage());
        }
    }

    private void buscarLibrosPorIdioma(String idioma) {
        try {
            Idioma idiomaEnum = Idioma.valueOf(idioma.toUpperCase());
            List<Libro> libros = repository.librosPorIdioma(idiomaEnum);

            if (!libros.isEmpty()) {
                libros.forEach(System.out::println);
            } else {
                System.out.println("No hay libros registrados en ese idioma.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Introduce un idioma válido en el formato especificado.");
        }
    }
}
