package com.version.gymModuloControl.service;

import com.version.gymModuloControl.model.Desempeno;
import com.version.gymModuloControl.model.Cliente;
import com.version.gymModuloControl.model.Empleado;
import com.version.gymModuloControl.model.TipoInstructor;
import com.version.gymModuloControl.model.TipoPlan;
import com.version.gymModuloControl.repository.DesempenoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.version.gymModuloControl.model.EstadoInscripcion;
import com.version.gymModuloControl.model.Inscripcion;
import com.version.gymModuloControl.model.DetalleInscripcion;
import com.version.gymModuloControl.model.HorarioEmpleado;

@Service
public class DesempenoService {
    @Autowired
    private DesempenoRepository desempenoRepository;

    // Validar elegibilidad: solo clientes inscritos en planes premium
    public boolean esElegibleParaDesempeno(Cliente cliente) {
        // Buscar la inscripción activa del cliente
        List<Inscripcion> inscripciones = cliente.getInscripciones();
        for (Inscripcion insc : inscripciones) {
            if (insc.getEstado() == EstadoInscripcion.ACTIVO &&
                insc.getPlan() != null &&
                insc.getPlan().getTipoPlan() == TipoPlan.PREMIUM) {
                return true;
            }
        }
        return false;
    }

    // Validar acceso: solo el cliente puede ver su desempeño
    public boolean puedeVerDesempeno(Desempeno desempeno, Cliente usuario) {
        return desempeno.getCliente().equals(usuario);
    }

    // Validar acceso: solo entrenadores premium pueden modificar desempeño de clientes con plan premium
    public boolean puedeModificarDesempeno(Desempeno desempeno, Empleado usuario) {
        Cliente cliente = desempeno.getCliente();
        List<Inscripcion> inscripciones = cliente.getInscripciones();
        for (Inscripcion insc : inscripciones) {
            if (insc.getEstado() == EstadoInscripcion.ACTIVO &&
                insc.getPlan() != null &&
                insc.getPlan().getTipoPlan() == TipoPlan.PREMIUM &&
                usuario.getTipoInstructor() == TipoInstructor.PREMIUM) {
                return true;
            }
        }
        return false;
    }

    // Método auxiliar para calcular IMC e indicador
    private void calcularImcEIndicador(Desempeno desempeno) {
        Double peso = desempeno.getPeso();
        Double estatura = desempeno.getEstatura();
        if (peso != null && estatura != null && estatura > 0) {
            double imc = peso / (estatura * estatura);
            imc = Math.round(imc * 10.0) / 10.0; // Redondear a un decimal
            desempeno.setImc(imc);
            // Calcular indicador según IMC
            if (imc <= 18.5) {
                desempeno.setIndicador("Peso Insuficiente");
            } else if (imc > 18.5 && imc <= 24.9) {
                desempeno.setIndicador("Normopeso");
            } else if (imc >= 25 && imc <= 29.9) {
                desempeno.setIndicador("Sobrepeso I y II");
            } else if (imc >= 30 && imc <= 39.9) {
                desempeno.setIndicador("Obesidad I y II");
            } else if (imc >= 40) {
                desempeno.setIndicador("Obesidad III y IV");
            } else {
                desempeno.setIndicador(null);
            }
        } else {
            desempeno.setImc(null);
            desempeno.setIndicador(null);
        }
    }

    // Registrar desempeño
    public Desempeno registrarDesempeno(Desempeno desempeno, String username) {
        // Validar que no exista ya un desempeño para el cliente y la inscripción activa
        Cliente cliente = desempeno.getCliente();
        Inscripcion inscripcion = desempeno.getInscripcion();
        if (cliente != null && inscripcion != null) {
            List<Desempeno> existentes = desempenoRepository.findByClienteAndInscripcion(cliente, inscripcion);
            if (existentes != null && !existentes.isEmpty()) {
                throw new IllegalStateException("Ya existe un desempeño registrado para este cliente en la inscripción actual. Solo puede actualizar el desempeño existente.");
            }
        }
        calcularImcEIndicador(desempeno);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        desempeno.setCreadoPor(username);
        desempeno.setFechaCreacion(now);
        desempeno.setFechaModificacion(now);
        return desempenoRepository.save(desempeno);
    }

    // Actualizar desempeño
    public Desempeno actualizarDesempeno(Desempeno desempeno, String username) {
        calcularImcEIndicador(desempeno);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        desempeno.setFechaModificacion(now);
        return desempenoRepository.save(desempeno);
    }

    // Consultar desempeño actual por cliente (solo inscripción activa premium)
    public List<Desempeno> obtenerDesempenosPorCliente(Cliente cliente) {
        // Buscar inscripción activa premium
        if (cliente == null || cliente.getInscripciones() == null) {
            return List.of();
        }
        for (Inscripcion insc : cliente.getInscripciones()) {
            if (insc.getEstado() == EstadoInscripcion.ACTIVO &&
                insc.getPlan() != null &&
                insc.getPlan().getTipoPlan() == TipoPlan.PREMIUM) {
                // Buscar desempeño asociado a esta inscripción activa premium
                return desempenoRepository.findByClienteAndInscripcion(cliente, insc);
            }
        }
        return List.of(); // No hay inscripción activa premium
    }

    // Obtener desempeño por ID
    public Desempeno obtenerPorId(Integer id) {
        return desempenoRepository.findById(id).orElse(null);
    }

    // Eliminar desempeño por ID
    public void eliminarDesempeno(Integer id) {
        desempenoRepository.deleteById(id);
    }

    // Obtener historial de desempeños por cliente (todas las inscripciones)
    public List<Desempeno> obtenerHistorialDesempenosPorCliente(Cliente cliente) {
        return desempenoRepository.findByCliente(cliente);
    }

    // Validar que el entrenador es premium y está asignado al cliente según el horario
    public boolean esEntrenadorAsignadoAlCliente(Empleado entrenador, Cliente cliente) {
        if (entrenador == null || entrenador.getTipoInstructor() != TipoInstructor.PREMIUM) {
            return false;
        }
        List<Inscripcion> inscripciones = cliente.getInscripciones();
        for (Inscripcion insc : inscripciones) {
            if (insc.getEstado() == EstadoInscripcion.ACTIVO &&
                insc.getPlan() != null &&
                insc.getPlan().getTipoPlan() == TipoPlan.PREMIUM &&
                insc.getDetallesInscripcion() != null) {
                for (DetalleInscripcion detalle : insc.getDetallesInscripcion()) {
                    HorarioEmpleado horario = detalle.getHorarioEmpleado();
                    if (horario != null && horario.getEmpleado() != null &&
                        horario.getEmpleado().getIdEmpleado().equals(entrenador.getIdEmpleado())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Validar si el entrenador está asignado al horario de la inscripción del cliente
    public boolean esEntrenadorAsignadoAlCliente(Empleado entrenador, Inscripcion inscripcion) {
        if (inscripcion == null || inscripcion.getDetallesInscripcion() == null) {
            return false;
        }
        for (DetalleInscripcion detalle : inscripcion.getDetallesInscripcion()) {
            HorarioEmpleado horarioEmpleado = detalle.getHorarioEmpleado();
            if (horarioEmpleado != null && horarioEmpleado.getEmpleado() != null &&
                horarioEmpleado.getEmpleado().getIdEmpleado().equals(entrenador.getIdEmpleado())) {
                return true;
            }
        }
        return false;
    }
}
