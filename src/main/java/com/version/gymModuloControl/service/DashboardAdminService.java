package com.version.gymModuloControl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.version.gymModuloControl.repository.AsistenciaRepository;
import com.version.gymModuloControl.repository.EmpleadoRepository;
import com.version.gymModuloControl.repository.InscripcionRepository;
import com.version.gymModuloControl.repository.PersonaRepository;
import com.version.gymModuloControl.repository.ProductoRepository;
import com.version.gymModuloControl.repository.VentaRepository;

@Service
public class DashboardAdminService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public Map<String, Object> obtenerDatosDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Estadísticas básicas
        dashboard.put("empleados", empleadoRepository.countByEstadoTrue());
        dashboard.put("clientes", personaRepository.countClientesActivos());
        dashboard.put("ventasHoy", ventaRepository.countVentasHoy());
        dashboard.put("ventasTotales", ventaRepository.sumTotalVentas());
        dashboard.put("productosAgotados", productoRepository.countProductosBajoStock());
        dashboard.put("nuevasInscripciones", inscripcionRepository.countInscripcionesHoy());
        
        return dashboard;
    }

    public Map<String, Object> obtenerEstadisticasVentas() {
        return obtenerEstadisticasVentas("mensual");
    }
    
    public Map<String, Object> obtenerEstadisticasVentas(String periodo) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Determinar número de meses según el período
        int meses = switch (periodo.toLowerCase()) {
            case "trimestral" -> 3;
            case "anual" -> 12;
            default -> 6; // mensual por defecto
        };
        
        // Ventas por mes con período específico
        List<Map<String, Object>> ventasPorMes = ventaRepository.getVentasPorMesConPeriodo(meses);
        estadisticas.put("ventasPorMes", ventasPorMes);
        
        // Inscripciones por mes
        List<Map<String, Object>> inscripcionesPorMes = inscripcionRepository.getInscripcionesPorMes();
        estadisticas.put("inscripcionesPorMes", inscripcionesPorMes);
        
        // Productos más vendidos con período específico
        List<Map<String, Object>> productosMasVendidos = ventaRepository.getProductosMasVendidosConPeriodo(meses);
        estadisticas.put("productosMasVendidos", productosMasVendidos);
        
        // Ventas por categoría con período específico
        List<Map<String, Object>> ventasPorCategoria = ventaRepository.getVentasPorCategoriaConPeriodo(meses);
        estadisticas.put("ventasPorCategoria", ventasPorCategoria);
        
        // Agregar información del período
        estadisticas.put("periodo", periodo);
        estadisticas.put("mesesAnalisis", meses);
        
        return estadisticas;
    }

    public Map<String, Object> obtenerEstadisticasClientes() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Asistencias por día de la semana
        List<Map<String, Object>> asistenciasPorDia = asistenciaRepository.getAsistenciasPorDiaSemana();
        estadisticas.put("asistenciasPorDia", asistenciasPorDia);
        
        // Distribución de clientes por plan
        List<Map<String, Object>> clientesPorPlan = inscripcionRepository.getClientesPorPlan();
        estadisticas.put("clientesPorPlan", clientesPorPlan);
        
        // Nuevos clientes por mes
        List<Map<String, Object>> nuevosClientesPorMes = personaRepository.getNuevosClientesPorMes();
        estadisticas.put("nuevosClientesPorMes", nuevosClientesPorMes);
        
        return estadisticas;
    }

    public Map<String, Object> obtenerProductosBajoStock() {
        Map<String, Object> productos = new HashMap<>();
        
        // Lista de productos con bajo stock
        List<Map<String, Object>> productosBajoStock = productoRepository.getProductosBajoStock();
        productos.put("productosBajoStock", productosBajoStock);
        
        // Resumen de inventario
        Map<String, Object> resumenInventario = new HashMap<>();
        resumenInventario.put("totalProductos", productoRepository.countByEstadoTrue());
        resumenInventario.put("productosAgotados", productoRepository.countByStockTotalAndEstado(0, true));
        resumenInventario.put("productosBajoStock", productoRepository.countProductosBajoStock());
        productos.put("resumenInventario", resumenInventario);
        
        return productos;
    }

    public Map<String, Object> obtenerActividadesRecientes() {
        Map<String, Object> actividades = new HashMap<>();
        
        // Últimas ventas
        List<Map<String, Object>> ultimasVentas = ventaRepository.getUltimasVentas();
        actividades.put("ultimasVentas", ultimasVentas);
        
        // Últimas inscripciones
        List<Map<String, Object>> ultimasInscripciones = inscripcionRepository.getUltimasInscripciones();
        actividades.put("ultimasInscripciones", ultimasInscripciones);
        
        // Empleados trabajando hoy
        List<Map<String, Object>> empleadosHoy = empleadoRepository.getEmpleadosTrabajandonHoy();
        actividades.put("empleadosHoy", empleadosHoy);
        
        return actividades;
    }
    
    public Map<String, Object> obtenerHorariosEmpleadosHoy() {
        Map<String, Object> horarios = new HashMap<>();
        
        // Empleados trabajando hoy con sus horarios detallados
        List<Map<String, Object>> empleadosHoy = empleadoRepository.getEmpleadosTrabajandonHoy();
        horarios.put("empleadosHoy", empleadosHoy);
        
        // Resumen de empleados
        Long totalEmpleados = empleadoRepository.countByEstadoTrue();
        horarios.put("totalEmpleados", totalEmpleados);
        horarios.put("empleadosActivosHoy", empleadosHoy.size());
        
        return horarios;
    }
    
    public Map<String, Object> obtenerTodosLosHorarios() {
        Map<String, Object> horarios = new HashMap<>();
        
        // Todos los horarios de empleados para debugging
        List<Map<String, Object>> empleados = empleadoRepository.getAllHorariosEmpleados();
        horarios.put("empleados", empleados);
        horarios.put("total", empleados.size());
        
        return horarios;
    }
}
