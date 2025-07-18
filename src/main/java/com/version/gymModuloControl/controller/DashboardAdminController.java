package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.service.DashboardAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard-admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DashboardAdminController {

    @Autowired
    private DashboardAdminService dashboardAdminService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerDatosDashboard() {
        try {
            Map<String, Object> datosDashboard = dashboardAdminService.obtenerDatosDashboard();
            return ResponseEntity.ok(datosDashboard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/estadisticas-ventas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasVentas(@RequestParam(defaultValue = "mensual") String periodo) {
        try {
            Map<String, Object> estadisticas = dashboardAdminService.obtenerEstadisticasVentas(periodo);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/estadisticas-clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasClientes() {
        try {
            Map<String, Object> estadisticas = dashboardAdminService.obtenerEstadisticasClientes();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/productos-bajo-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerProductosBajoStock() {
        try {
            Map<String, Object> productos = dashboardAdminService.obtenerProductosBajoStock();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/actividades-recientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerActividadesRecientes() {
        try {
            Map<String, Object> actividades = dashboardAdminService.obtenerActividadesRecientes();
            return ResponseEntity.ok(actividades);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/horarios-hoy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerHorariosHoy() {
        try {
            Map<String, Object> horarios = dashboardAdminService.obtenerHorariosEmpleadosHoy();
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/debug/horarios-todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerTodosLosHorarios() {
        try {
            Map<String, Object> horarios = dashboardAdminService.obtenerTodosLosHorarios();
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
