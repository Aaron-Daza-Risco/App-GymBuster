package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.model.Categoria;
import com.version.gymModuloControl.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categoria")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Categoria> guardarCategoria(@RequestBody Categoria categoria) {
        Categoria categoriaGuardada = categoriaService.guardarCategoria(categoria);
        return ResponseEntity.ok(categoriaGuardada);
    }

    @GetMapping("/listar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    public ResponseEntity<?> listarCategorias(Authentication authentication) {
        return ResponseEntity.ok(categoriaService.listarCategoria());
    }

    // Dentro de CategoriaController

    @PutMapping("/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Categoria> actualizarCategoria(@RequestBody Categoria categoria) {
        Categoria categoriaActualizada = categoriaService.actualizarCategoria(categoria);
        return ResponseEntity.ok(categoriaActualizada);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstadoCategoria(@PathVariable Integer id, @RequestBody Boolean estado) {
        Categoria categoria = categoriaService.cambiarEstadoCategoria(id, estado);
        if (categoria != null) {
            return ResponseEntity.ok(categoria);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
