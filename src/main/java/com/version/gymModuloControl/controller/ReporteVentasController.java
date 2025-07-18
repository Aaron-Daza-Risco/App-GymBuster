package com.version.gymModuloControl.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.version.gymModuloControl.service.ReporteVentasService;

@RestController
@RequestMapping("/api/reportes-ventas")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReporteVentasController {

    @Autowired
    private ReporteVentasService reporteVentasService;

    // ===== INGRESOS TOTALES =====
    @GetMapping("/ingresos-totales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerIngresosTotales(@RequestParam(defaultValue = "mensual") String periodo) {
        try {
            Map<String, Object> ingresos = reporteVentasService.obtenerIngresosTotales(periodo);
            return ResponseEntity.ok(ingresos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== CRECIMIENTO VS PERIODO ANTERIOR =====
    @GetMapping("/crecimiento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerCrecimiento(@RequestParam(defaultValue = "mensual") String periodo) {
        try {
            Map<String, Object> crecimiento = reporteVentasService.obtenerCrecimiento(periodo);
            return ResponseEntity.ok(crecimiento);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== TOTAL DE TRANSACCIONES =====
    @GetMapping("/total-transacciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerTotalTransacciones(@RequestParam(defaultValue = "mensual") String periodo) {
        try {
            Map<String, Object> transacciones = reporteVentasService.obtenerTotalTransacciones(periodo);
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== ANÁLISIS POR CATEGORÍA =====
    @GetMapping("/analisis-categoria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerAnalisisCategoria(@RequestParam(defaultValue = "mensual") String periodo) {
        try {
            Map<String, Object> analisis = reporteVentasService.obtenerAnalisisCategoria(periodo);
            return ResponseEntity.ok(analisis);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== PRODUCTOS MÁS VENDIDOS =====
    @GetMapping("/productos-mas-vendidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerProductosMasVendidos(@RequestParam(defaultValue = "mensual") String periodo) {
        try {
            Map<String, Object> productos = reporteVentasService.obtenerProductosMasVendidos(periodo);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== TENDENCIAS =====
    @GetMapping("/tendencias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerTendencias(@RequestParam(defaultValue = "mensual") String periodo) {
        try {
            Map<String, Object> tendencias = reporteVentasService.obtenerTendencias(periodo);
            return ResponseEntity.ok(tendencias);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== RENTABILIDAD =====
    @GetMapping("/rentabilidad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerRentabilidad(@RequestParam(defaultValue = "mensual") String periodo) {
        try {
            Map<String, Object> rentabilidad = reporteVentasService.obtenerRentabilidad(periodo);
            return ResponseEntity.ok(rentabilidad);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
