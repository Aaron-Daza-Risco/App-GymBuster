package com.version.gymModuloControl.dto;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleAlquilerDTO {
    private Integer idDetalleAlquiler;
    private Integer piezaId;
    private String piezaNombre;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;

}
