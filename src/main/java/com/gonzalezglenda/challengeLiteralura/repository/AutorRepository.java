package com.gonzalezglenda.challengeLiteralura.repository;

import com.gonzalezglenda.challengeLiteralura.model.Autor;
import com.gonzalezglenda.challengeLiteralura.model.Idioma;
import com.gonzalezglenda.challengeLiteralura.model.Libro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {


    @Query("SELECT l FROM Libro l JOIN l.autor a WHERE l.titulo LIKE %:titulo%")
    Optional<Libro> buscarLibroPorNombre(@Param("titulo") String titulo);

    @Query("SELECT l FROM Autor a JOIN a.libros l ORDER BY l.titulo")
    List<Libro> librosRegistrados();

    @Query("SELECT a FROM Autor a WHERE a.fechaDeNacimiento <= :fecha AND a.fechaDeFallecimiento > :fecha")
    List<Autor> listarAutoresVivos(@Param("fecha") Integer fecha);

    @Query("SELECT l FROM Autor a JOIN a.libros l WHERE l.idioma = :idioma")
    List<Libro> librosPorIdioma(@Param("idioma") Idioma idioma);

}
