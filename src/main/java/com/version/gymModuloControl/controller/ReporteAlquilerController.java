package com.version.gymModuloControl.controller;

import com.version.gymModuloControl.service.ReporteAlquilerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes/alquileres")
public class ReporteAlquilerController {

    @Autowired
    private ReporteAlquilerService reporteAlquilerService;

    // Estado de alquileres - mensual
    @GetMapping("/estados/mes-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> obtenerEstadoAlquileresMesActual() {
        return reporteAlquilerService.obtenerEstadoAlquileresMesActual();
    }

    // Estado de alquileres - trimestral
    @GetMapping("/estados/trimestre-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> obtenerEstadoAlquileresTrimestreActual() {
        return reporteAlquilerService.obtenerEstadoAlquileresTrimestreActual();
    }

    // Estado de alquileres - anual
    @GetMapping("/estados/anio-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> obtenerEstadoAlquileresAnioActual() {
        return reporteAlquilerService.obtenerEstadoAlquileresAnioActual();
    }


    // 2. Top 10 piezas más alquiladas
// Top 10 piezas más alquiladas - mensual
    @GetMapping("/top10-piezas/mes-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> obtenerTop10PiezasMasAlquiladasMesActual() {
        return reporteAlquilerService.obtenerTop10PiezasMasAlquiladasMesActual();
    }

    // Top 10 piezas más alquiladas - trimestral
    @GetMapping("/top10-piezas/trimestre-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> obtenerTop10PiezasMasAlquiladasTrimestreActual() {
        return reporteAlquilerService.obtenerTop10PiezasMasAlquiladasTrimestreActual();
    }

    // Top 10 piezas más alquiladas - anual
    @GetMapping("/top10-piezas/anio-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> obtenerTop10PiezasMasAlquiladasAnioActual() {
        return reporteAlquilerService.obtenerTop10PiezasMasAlquiladasAnioActual();
    }

    // 3. Alquileres con mora pendiente
    @GetMapping("/pendientes-mora")
    @PreAuthorize("hasRole('ADMIN')")
    public List<?> obtenerAlquileresConPagosPendientesOMora() {
        return reporteAlquilerService.obtenerAlquileresConPagosPendientesOMora();
    }

    // 4. Ganancia mensual por alquileres
    @GetMapping("/ingresos-mes-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal obtenerIngresosMesActual() {
        return reporteAlquilerService.obtenerIngresosMesActual();
    }

    // 5. Ganancias del trimestre actual
    @GetMapping("/ingresos-trimestre-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal obtenerIngresosTrimestreActual() {
        return reporteAlquilerService.obtenerIngresosTrimestreActual();
    }

    // 6. Ganancias del año actual
    @GetMapping("/ingresos-anio-actual")
    @PreAuthorize("hasRole('ADMIN')")
    public BigDecimal obtenerIngresosAnioActual() {
        return reporteAlquilerService.obtenerIngresosAnioActual();
    }


    // 7. Tendencias de alquileres últimos 6 meses
    @GetMapping("/tendencia")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> obtenerTendenciaAlquileresUltimosMeses(
            @RequestParam(defaultValue = "6") int meses
    ) {
        return reporteAlquilerService.obtenerTendenciaAlquileresUltimosMeses(meses);
    }
}