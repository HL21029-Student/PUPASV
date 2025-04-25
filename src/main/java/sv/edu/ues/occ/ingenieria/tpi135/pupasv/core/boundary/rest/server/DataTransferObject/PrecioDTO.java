/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;

import jakarta.validation.constraints.DecimalMin; // Opcional: Para validación
import jakarta.validation.constraints.NotNull;    // Opcional: Para validación
import java.math.BigDecimal;

/**
 * DTO para transferir el nuevo precio de un Producto.
 */
public class PrecioDTO {

    @NotNull(message = "El precio no puede ser nulo") // Ejemplo validación
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero") // Ejemplo validación
    private BigDecimal precio;

    // --- Constructores ---

    public PrecioDTO() {
        // Constructor por defecto 
    }

    public PrecioDTO(BigDecimal precio) {
        this.precio = precio;
    }

    // --- Getters y Setters ---

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}