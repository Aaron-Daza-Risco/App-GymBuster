package com.version.gymModuloControl.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.version.gymModuloControl.model.UsuarioRol;
import java.util.List;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Integer> {
    List<UsuarioRol> findByUsuario_Id(Integer idUsuario);
}


