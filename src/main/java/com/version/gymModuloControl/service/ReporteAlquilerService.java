package com.version.gymModuloControl.service;

import com.version.gymModuloControl.model.EstadoAlquiler;
import com.version.gymModuloControl.repository.AlquilerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReporteAlquilerService {

    @Autowired
    private AlquilerRepository alquilerRepository;


    // 1. Estado de alquileres
    public List<Map<String, Object>> obtenerEstadoAlquileresMesActual() {
        return convertirResultadosAListaMap(alquilerRepository.contarAlquileresPorEstadoMesActual());
    }

    public List<Map<String, Object>> obtenerEstadoAlquileresTrimestreActual() {
        return convertirResultadosAListaMap(alquilerRepository.contarAlquileresPorEstadoTrimestreActual());
    }

    public List<Map<String, Object>> obtenerEstadoAlquileresAnioActual() {
        return convertirResultadosAListaMap(alquilerRepository.contarAlquileresPorEstadoAnioActual());
    }

    private List<Map<String, Object>> convertirResultadosAListaMap(List<Object[]> resultados) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Object[] fila : resultados) {
            Map<String, Object> map = new HashMap<>();
            map.put("estado", fila[0]);
            map.put("cantidad", fila[1]);
            lista.add(map);
        }
        return lista;
    }

    // 2. Top 10 piezas más alquiladas
    public List<Map<String, Object>> obtenerTop10PiezasMasAlquiladasMesActual() {
        return convertirRankingAListaMap(
                alquilerRepository.rankingPiezasMasAlquiladasTop10MesActual(PageRequest.of(0,10))
        );
    }

    public List<Map<String, Object>> obtenerTop10PiezasMasAlquiladasTrimestreActual() {
        return convertirRankingAListaMap(
                alquilerRepository.rankingPiezasMasAlquiladasTop10TrimestreActual(PageRequest.of(0,10))
        );
    }

    public List<Map<String, Object>> obtenerTop10PiezasMasAlquiladasAnioActual() {
        return convertirRankingAListaMap(
                alquilerRepository.rankingPiezasMasAlquiladasTop10AnioActual(PageRequest.of(0,10))
        );
    }

    private List<Map<String, Object>> convertirRankingAListaMap(List<Object[]> resultados) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Object[] fila : resultados) {
            Map<String, Object> map = new HashMap<>();
            map.put("pieza", fila[0]);
            map.put("unidadesAlquiladas", fila[1]);
            map.put("ingresos", fila[2]);
            lista.add(map);
        }
        return lista;
    }

    // 3. Alquileres con mora
    public List<Map<String, Object>> obtenerAlquileresConPagosPendientesOMora() {
        List<Object[]> resultados = alquilerRepository.alquileresVencidos();
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Object[] fila : resultados) {
            Map<String, Object> map = new HashMap<>();
            map.put("pieza", fila[0]);   // nombre de la pieza
            map.put("estado", fila[1]);  // estado del alquiler
            map.put("mora", fila[2]);    // monto de la mora
            lista.add(map);
        }
        return lista;
    }

    // 4. Ingresos mensuales
    public BigDecimal obtenerIngresosMesActual() {
        Double total = alquilerRepository.sumTotalAlquileresEsteMes();
        return BigDecimal.valueOf(total != null ? total : 0.0);
    }

    // 5. Ingresos trimestrales
    public BigDecimal obtenerIngresosTrimestreActual() {
        Double total = alquilerRepository.sumTotalAlquileresTrimestreActual();
        return BigDecimal.valueOf(total != null ? total : 0.0);
    }

    // 6. Ingresos anuales
    public BigDecimal obtenerIngresosAnioActual() {
        Double total = alquilerRepository.sumTotalAlquileresAnioActual();
        return BigDecimal.valueOf(total != null ? total : 0.0);
    }


    // 7. Tendencias de alquileres últimos 6 meses
    public List<Map<String, Object>> obtenerTendenciaAlquileresUltimosMeses(int meses) {
        LocalDate fechaInicio = LocalDate.now().minusMonths(meses);
        List<Object[]> resultados = alquilerRepository.alquileresPorMes(fechaInicio);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Object[] fila : resultados) {
            Map<String, Object> map = new HashMap<>();
            map.put("mes", fila[0]);
            map.put("total", fila[1]);
            lista.add(map);
        }
        return lista;
    }
}