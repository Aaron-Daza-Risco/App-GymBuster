package com.version.gymModuloControl.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleAlquilerDTO {
    private Integer idDetalleAlquiler;
    private Integer piezaId;
    private String piezaNombre;
    private Integer cantidad;
    private Double precioUnitario; // Precio diario por pieza
    private Double subtotal; // precio_diario × cantidad × días
    private Integer diasAlquiler; // Número de días del alquiler
}
