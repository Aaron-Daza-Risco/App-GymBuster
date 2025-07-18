package com.version.gymModuloControl.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.version.gymModuloControl.repository.VentaRepository;

@Service
public class ReporteVentasService {

    @Autowired
    private VentaRepository ventaRepository;

    // ===== INGRESOS TOTALES =====
    public Map<String, Object> obtenerIngresosTotales(String periodo) {
        Map<String, Object> resultado = new HashMap<>();
        
        int meses = obtenerMesesPorPeriodo(periodo);
        
        // Para consistencia con productos más vendidos, usar suma de detalles de venta
        Double ingresosTotales;
        if ("mensual".equalsIgnoreCase(periodo)) {
            ingresosTotales = ventaRepository.sumIngresosPorDetallesEsteMes();
        } else {
            ingresosTotales = ventaRepository.sumIngresosPorDetallesPorPeriodo(meses);
        }
        if (ingresosTotales == null) ingresosTotales = 0.0;
        
        // Ingresos por mes
        List<Map<String, Object>> ingresosPorMes = ventaRepository.getIngresosPorMes(meses);
        
        // Ingresos del mes actual
        Double ingresosEsteMes = ventaRepository.sumIngresosPorDetallesEsteMes();
        if (ingresosEsteMes == null) ingresosEsteMes = 0.0;
        
        resultado.put("ingresosTotales", ingresosTotales);
        resultado.put("ingresosPorMes", ingresosPorMes);
        resultado.put("ingresosEsteMes", ingresosEsteMes);
        resultado.put("periodo", periodo);
        resultado.put("fechaActualizacion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        return resultado;
    }

    // ===== CRECIMIENTO VS PERIODO ANTERIOR =====
    public Map<String, Object> obtenerCrecimiento(String periodo) {
        Map<String, Object> resultado = new HashMap<>();
        
        int meses = obtenerMesesPorPeriodo(periodo);
        
        // Verificar si existen ventas en el período anterior
        Long ventasPeriodoAnterior = ventaRepository.countVentasPeriodoAnterior(meses);
        boolean tieneHistorial = ventasPeriodoAnterior != null && ventasPeriodoAnterior > 0;
        
        // Ventas período actual - usar el mismo método que ingresos totales (basado en detalles)
        Double ventasActuales;
        if ("mensual".equalsIgnoreCase(periodo)) {
            ventasActuales = ventaRepository.sumIngresosPorDetallesEsteMes();
        } else {
            ventasActuales = ventaRepository.sumIngresosPorDetallesPorPeriodo(meses);
        }
        if (ventasActuales == null) ventasActuales = 0.0;
        
        if (tieneHistorial) {
            // Ventas período anterior - usar el mismo método que ingresos totales (basado en detalles)
            Double ventasAnteriores;
            if ("mensual".equalsIgnoreCase(periodo)) {
                ventasAnteriores = ventaRepository.sumIngresosPorDetallesPeriodoAnterior(1);
            } else {
                ventasAnteriores = ventaRepository.sumIngresosPorDetallesPeriodoAnterior(meses);
            }
            if (ventasAnteriores == null) ventasAnteriores = 0.0;
            
            // Cálculo del crecimiento
            Double porcentajeCrecimiento = 0.0;
            if (ventasAnteriores > 0) {
                porcentajeCrecimiento = ((ventasActuales - ventasAnteriores) / ventasAnteriores) * 100;
            }
            
            // Comparativa mensual
            List<Map<String, Object>> comparativaMensual = ventaRepository.getComparativaMensual(meses);
            
            resultado.put("ventasActuales", ventasActuales);
            resultado.put("ventasAnteriores", ventasAnteriores);
            resultado.put("porcentajeCrecimiento", porcentajeCrecimiento);
            resultado.put("comparativaMensual", comparativaMensual);
            resultado.put("tendencia", porcentajeCrecimiento >= 0 ? "positiva" : "negativa");
            resultado.put("tieneHistorial", true);
            resultado.put("mensaje", "Comparación con período anterior disponible");
            
        } else {
            // No hay datos históricos suficientes
            String fechaPrimeraVenta = ventaRepository.getFechaprimeraVenta();
            
            resultado.put("ventasActuales", ventasActuales);
            resultado.put("ventasAnteriores", 0.0);
            resultado.put("porcentajeCrecimiento", 0.0);
            resultado.put("comparativaMensual", new ArrayList<>());
            resultado.put("tendencia", "neutral");
            resultado.put("tieneHistorial", false);
            resultado.put("mensaje", fechaPrimeraVenta != null ? 
                "Datos insuficientes para comparación. Primera venta: " + fechaPrimeraVenta :
                "No hay ventas registradas para comparar");
            resultado.put("fechaPrimeraVenta", fechaPrimeraVenta);
        }
        
        return resultado;
    }

    // ===== TOTAL DE TRANSACCIONES =====
    public Map<String, Object> obtenerTotalTransacciones(String periodo) {
        Map<String, Object> resultado = new HashMap<>();
        
        int meses = obtenerMesesPorPeriodo(periodo);
        
        // Total de transacciones
        Long totalTransacciones = ventaRepository.countVentasPorPeriodo(meses);
        if (totalTransacciones == null) totalTransacciones = 0L;
        
        // Transacciones por mes
        List<Map<String, Object>> transaccionesPorMes = ventaRepository.getTransaccionesPorMes(meses);
        
        // Promedio de transacciones por día
        Double promedioTransaccionesDiarias = ventaRepository.getPromedioTransaccionesDiarias(meses);
        if (promedioTransaccionesDiarias == null) promedioTransaccionesDiarias = 0.0;
        
        // Ticket promedio - usar el mismo método que ingresos totales (basado en detalles)
        Double ingresosTotalesParaTicket;
        if ("mensual".equalsIgnoreCase(periodo)) {
            ingresosTotalesParaTicket = ventaRepository.sumIngresosPorDetallesEsteMes();
        } else {
            ingresosTotalesParaTicket = ventaRepository.sumIngresosPorDetallesPorPeriodo(meses);
        }
        if (ingresosTotalesParaTicket == null) ingresosTotalesParaTicket = 0.0;
        
        Double ticketPromedio = totalTransacciones > 0 ? 
            ingresosTotalesParaTicket / totalTransacciones : 0.0;
        
        resultado.put("totalTransacciones", totalTransacciones);
        resultado.put("transaccionesPorMes", transaccionesPorMes);
        resultado.put("promedioTransaccionesDiarias", promedioTransaccionesDiarias);
        resultado.put("ticketPromedio", ticketPromedio);
        resultado.put("periodo", periodo);
        
        return resultado;
    }

    // ===== ANÁLISIS POR CATEGORÍA =====
    public Map<String, Object> obtenerAnalisisCategoria(String periodo) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            int meses = obtenerMesesPorPeriodo(periodo);
            
            // Ventas por categoría - usar el mismo método que ingresos totales (basado en detalles)
            List<Map<String, Object>> ventasPorCategoriaRaw;
            Double totalVentas;
            
            if ("mensual".equalsIgnoreCase(periodo)) {
                // Para período mensual, usar el mes actual completo para consistencia
                ventasPorCategoriaRaw = ventaRepository.getVentasPorCategoriaEsteMes();
                totalVentas = ventaRepository.sumIngresosPorDetallesEsteMes();
            } else {
                // Para otros períodos, usar el período móvil
                ventasPorCategoriaRaw = ventaRepository.getVentasPorCategoriaConPeriodo(meses);
                totalVentas = ventaRepository.sumIngresosPorDetallesPorPeriodo(meses);
            }
            
            if (totalVentas == null) totalVentas = 0.0;
            
            // Crear lista mutable con porcentajes calculados
            List<Map<String, Object>> ventasPorCategoria = new ArrayList<>();
            for (Map<String, Object> categoriaRaw : ventasPorCategoriaRaw) {
                // Crear un nuevo HashMap mutable
                Map<String, Object> categoria = new HashMap<>(categoriaRaw);
                
                Double ventasCategoria = ((Number) categoria.get("totalVentas")).doubleValue();
                Double porcentaje = totalVentas > 0 ? (ventasCategoria / totalVentas) * 100 : 0.0;
                categoria.put("porcentajeContribucion", porcentaje);
                
                ventasPorCategoria.add(categoria);
            }
            
            // Si no hay datos de rendimiento, crear datos mock básicos
            List<Map<String, Object>> rendimientoCategoria = new java.util.ArrayList<>();
            for (Map<String, Object> categoria : ventasPorCategoria) {
                Map<String, Object> rendimiento = new HashMap<>();
                rendimiento.put("categoria", categoria.get("categoria"));
                rendimiento.put("ventasActuales", categoria.get("totalVentas"));
                rendimiento.put("ventasAnteriores", 0.0);
                rendimientoCategoria.add(rendimiento);
            }
            
            resultado.put("ventasPorCategoria", ventasPorCategoria);
            resultado.put("rendimientoCategoria", rendimientoCategoria);
            resultado.put("totalVentas", totalVentas);
            resultado.put("numeroCategoriasActivas", ventasPorCategoria.size());
            
        } catch (Exception e) {
            // Si hay error, devolver datos básicos para evitar fallo completo
            System.err.println("Error en obtenerAnalisisCategoria: " + e.getMessage());
            e.printStackTrace();
            
            resultado.put("ventasPorCategoria", new java.util.ArrayList<>());
            resultado.put("rendimientoCategoria", new java.util.ArrayList<>());
            resultado.put("totalVentas", 0.0);
            resultado.put("numeroCategoriasActivas", 0);
        }
        
        return resultado;
    }

    // ===== PRODUCTOS MÁS VENDIDOS =====
    public Map<String, Object> obtenerProductosMasVendidos(String periodo) {
        Map<String, Object> resultado = new HashMap<>();
        
        int meses = obtenerMesesPorPeriodo(periodo);
        
        // Top 10 productos más vendidos - usar el mismo período que ingresos totales
        List<Map<String, Object>> top10Productos;
        Double totalVentasGeneral;
        
        if ("mensual".equalsIgnoreCase(periodo)) {
            // Para período mensual, usar el mes actual completo para consistencia
            top10Productos = ventaRepository.getProductosMasVendidosEsteMes();
            totalVentasGeneral = ventaRepository.sumIngresosPorDetallesEsteMes();
        } else {
            // Para otros períodos, usar el período móvil
            top10Productos = ventaRepository.getProductosMasVendidosConPeriodo(meses);
            totalVentasGeneral = ventaRepository.sumIngresosPorDetallesPorPeriodo(meses);
        }
        
        if (totalVentasGeneral == null) totalVentasGeneral = 0.0;
        
        // Distribución de ventas - Top productos
        Double totalVentasTopProductos = 0.0;
        for (Map<String, Object> producto : top10Productos) {
            totalVentasTopProductos += ((Number) producto.get("totalVentas")).doubleValue();
        }
        
        Double porcentajeTopProductos = totalVentasGeneral > 0 ? 
            (totalVentasTopProductos / totalVentasGeneral) * 100 : 0.0;
        
        resultado.put("top10Productos", top10Productos);
        resultado.put("totalVentasTopProductos", totalVentasTopProductos);
        resultado.put("porcentajeTopProductos", porcentajeTopProductos);
        resultado.put("totalVentasGeneral", totalVentasGeneral);
        
        return resultado;
    }

    // ===== TENDENCIAS =====
    public Map<String, Object> obtenerTendencias(String periodo) {
        Map<String, Object> resultado = new HashMap<>();
        
        int meses = obtenerMesesPorPeriodo(periodo);
        
        // Evolución de ventas (siempre últimos 6 meses para mostrar tendencia completa)
        List<Map<String, Object>> evolucionVentas = ventaRepository.getEvolucionVentas(6);
        
        // Evolución para cálculos de predicción (basada en el período seleccionado)
        List<Map<String, Object>> evolucionParaCalculo = ventaRepository.getEvolucionVentasConPeriodo(meses);
        
        // Comparativa con período anterior
        Map<String, Object> comparativa = obtenerCrecimiento(periodo);
        
        // Predicción múltiples períodos (basada en tendencia del período seleccionado)
        Double tendenciaMensual = calcularTendenciaMensual(evolucionParaCalculo);
        List<Map<String, Object>> prediccionesMeses = calcularPrediccionesMeses(evolucionParaCalculo, 3);
        
        Map<String, Object> prediccion = new HashMap<>();
        prediccion.put("tendenciaMensual", tendenciaMensual);
        prediccion.put("prediccionProximoMes", calcularPrediccionProximoMes(evolucionParaCalculo));
        prediccion.put("prediccionesMeses", prediccionesMeses);
        prediccion.put("confiabilidad", calcularConfiabilidadPrediccion(evolucionParaCalculo));
        
        resultado.put("evolucionVentas", evolucionVentas);
        resultado.put("comparativa", comparativa);
        resultado.put("prediccion", prediccion);
        resultado.put("analisisTendencia", analizarTendencia(evolucionParaCalculo));
        
        return resultado;
    }

    // ===== RENTABILIDAD =====
    public Map<String, Object> obtenerRentabilidad(String periodo) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            int meses = obtenerMesesPorPeriodo(periodo);
            
            // Intentar obtener datos de rentabilidad, si falla usar datos mock
            List<Map<String, Object>> rentabilidadProductos = new java.util.ArrayList<>();
            List<Map<String, Object>> margenProductos = new java.util.ArrayList<>();
            Map<String, Object> costosIngresos = new HashMap<>();
            
            try {
                // Usar el método específico para el mes actual cuando el período es mensual
                if ("mensual".equalsIgnoreCase(periodo)) {
                    rentabilidadProductos = ventaRepository.getRentabilidadProductosEsteMes();
                } else {
                    rentabilidadProductos = ventaRepository.getRentabilidadProductos(meses);
                }
            } catch (Exception e) {
                System.err.println("Error al obtener rentabilidad productos: " + e.getMessage());
            }
            
            try {
                margenProductos = ventaRepository.getMargenProductos(meses);
            } catch (Exception e) {
                System.err.println("Error al obtener margen productos: " + e.getMessage());
            }
            
            try {
                List<Map<String, Object>> costosIngresosResult = ventaRepository.getCostosVsIngresos(meses);
                costosIngresos = !costosIngresosResult.isEmpty() ? 
                    costosIngresosResult.get(0) : new HashMap<>();
            } catch (Exception e) {
                System.err.println("Error al obtener costos vs ingresos: " + e.getMessage());
                costosIngresos.put("ingresosTotales", 0.0);
                costosIngresos.put("costosTotales", 0.0);
                costosIngresos.put("utilidadBruta", 0.0);
                costosIngresos.put("margenBruto", 0.0);
            }
            
            // Resumen de rentabilidad - usar el mismo método que ingresos totales (basado en detalles)
            Double ingresosTotales;
            if ("mensual".equalsIgnoreCase(periodo)) {
                ingresosTotales = ventaRepository.sumIngresosPorDetallesEsteMes();
            } else {
                ingresosTotales = ventaRepository.sumIngresosPorDetallesPorPeriodo(meses);
            }
            if (ingresosTotales == null) ingresosTotales = 0.0;
            
            Double costosTotales = ((Number) costosIngresos.getOrDefault("costosTotales", 0.0)).doubleValue();
            Double utilidadBruta = ingresosTotales - costosTotales;
            Double margenBruto = ingresosTotales > 0 ? (utilidadBruta / ingresosTotales) * 100 : 0.0;
            
            Map<String, Object> resumenRentabilidad = new HashMap<>();
            resumenRentabilidad.put("ingresosTotales", ingresosTotales);
            resumenRentabilidad.put("costosTotales", costosTotales);
            resumenRentabilidad.put("utilidadBruta", utilidadBruta);
            resumenRentabilidad.put("margenBruto", margenBruto);
            
            resultado.put("rentabilidadProductos", rentabilidadProductos);
            resultado.put("margenProductos", margenProductos);
            resultado.put("costosIngresos", costosIngresos);
            resultado.put("resumenRentabilidad", resumenRentabilidad);
            
        } catch (Exception e) {
            // Si hay error, devolver datos básicos para evitar fallo completo
            System.err.println("Error en obtenerRentabilidad: " + e.getMessage());
            e.printStackTrace();
            
            resultado.put("rentabilidadProductos", new java.util.ArrayList<>());
            resultado.put("margenProductos", new java.util.ArrayList<>());
            resultado.put("costosIngresos", new HashMap<>());
            
            Map<String, Object> resumenRentabilidad = new HashMap<>();
            resumenRentabilidad.put("ingresosTotales", 0.0);
            resumenRentabilidad.put("costosTotales", 0.0);
            resumenRentabilidad.put("utilidadBruta", 0.0);
            resumenRentabilidad.put("margenBruto", 0.0);
            resultado.put("resumenRentabilidad", resumenRentabilidad);
        }
        
        return resultado;
    }

    // ===== MÉTODOS AUXILIARES =====
    private int obtenerMesesPorPeriodo(String periodo) {
        return switch (periodo.toLowerCase()) {
            case "semanal" -> 0; // Última semana
            case "mensual" -> 1;
            case "trimestral" -> 3;
            case "semestral" -> 6;
            case "anual" -> 12;
            default -> 1;
        };
    }

    private Double calcularTendenciaMensual(List<Map<String, Object>> evolucionVentas) {
        if (evolucionVentas.size() < 2) return 0.0;
        
        double sumaX = 0, sumaY = 0, sumaXY = 0, sumaX2 = 0;
        int n = evolucionVentas.size();
        
        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = ((Number) evolucionVentas.get(i).get("totalVentas")).doubleValue();
            
            sumaX += x;
            sumaY += y;
            sumaXY += x * y;
            sumaX2 += x * x;
        }
        
        // Cálculo de la pendiente de la regresión lineal
        return (n * sumaXY - sumaX * sumaY) / (n * sumaX2 - sumaX * sumaX);
    }

    private Double calcularPrediccionProximoMes(List<Map<String, Object>> evolucionVentas) {
        if (evolucionVentas.isEmpty()) return 0.0;
        
        Double ultimoMes = ((Number) evolucionVentas.get(evolucionVentas.size() - 1).get("totalVentas")).doubleValue();
        Double tendencia = calcularTendenciaMensual(evolucionVentas);
        
        return ultimoMes + tendencia;
    }

    private List<Map<String, Object>> calcularPrediccionesMeses(List<Map<String, Object>> evolucionVentas, int numeroMeses) {
        List<Map<String, Object>> predicciones = new ArrayList<>();
        
        if (evolucionVentas.isEmpty()) {
            // Si no hay datos históricos, devolver predicciones vacías
            for (int i = 1; i <= numeroMeses; i++) {
                Map<String, Object> prediccion = new HashMap<>();
                prediccion.put("mes", "Mes " + i);
                prediccion.put("prediccion", 0.0);
                prediccion.put("confianza", "Baja");
                predicciones.add(prediccion);
            }
            return predicciones;
        }
        
        Double tendenciaMensual = calcularTendenciaMensual(evolucionVentas);
        Double ultimoValor = ((Number) evolucionVentas.get(evolucionVentas.size() - 1).get("totalVentas")).doubleValue();
        String confiabilidad = calcularConfiabilidadPrediccion(evolucionVentas);
        
        // Generar nombres de meses futuros
        String[] nombresMeses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        int mesActual = java.time.LocalDate.now().getMonthValue() - 1; // 0-indexed
        
        for (int i = 1; i <= numeroMeses; i++) {
            Map<String, Object> prediccion = new HashMap<>();
            
            // Calcular el valor predicho
            Double valorPrediccion = ultimoValor + (tendenciaMensual * i);
            
            // Asegurar que no sea negativo
            if (valorPrediccion < 0) valorPrediccion = 0.0;
            
            // Nombre del mes futuro
            int indiceMes = (mesActual + i) % 12;
            String nombreMes = nombresMeses[indiceMes];
            
            prediccion.put("mes", nombreMes);
            prediccion.put("prediccion", Math.round(valorPrediccion * 100.0) / 100.0);
            prediccion.put("confianza", confiabilidad);
            prediccion.put("tendencia", tendenciaMensual > 0 ? "Crecimiento" : tendenciaMensual < 0 ? "Decrecimiento" : "Estable");
            
            predicciones.add(prediccion);
        }
        
        return predicciones;
    }

    private String calcularConfiabilidadPrediccion(List<Map<String, Object>> evolucionVentas) {
        if (evolucionVentas.size() < 2) return "Muy Baja";
        if (evolucionVentas.size() < 4) return "Baja";
        if (evolucionVentas.size() < 6) return "Media";
        
        // Calcular variabilidad de los datos
        double[] valores = evolucionVentas.stream()
            .mapToDouble(v -> ((Number) v.get("totalVentas")).doubleValue())
            .toArray();
        
        double promedio = java.util.Arrays.stream(valores).average().orElse(0.0);
        double varianza = java.util.Arrays.stream(valores)
            .map(v -> Math.pow(v - promedio, 2))
            .average().orElse(0.0);
        
        double coeficienteVariacion = promedio > 0 ? Math.sqrt(varianza) / promedio : 1.0;
        
        if (coeficienteVariacion < 0.15) return "Alta";
        if (coeficienteVariacion < 0.30) return "Media";
        return "Baja";
    }

    private String analizarTendencia(List<Map<String, Object>> evolucionVentas) {
        Double tendencia = calcularTendenciaMensual(evolucionVentas);
        
        if (tendencia > 1000) return "Crecimiento fuerte";
        else if (tendencia > 0) return "Crecimiento moderado";
        else if (tendencia > -1000) return "Decrecimiento moderado";
        else return "Decrecimiento fuerte";
    }
}
