/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;

import java.util.List;
// import jakarta.validation.constraints.*; // Si se utiliza Bean Validation
// import jakarta.validation.Valid;         // Si se utiliza Bean Validation

public class OrdenRequestDTO {

    // @NotBlank // Ejemplo de validación
    private String sucursal;

    // @NotEmpty // Ejemplo de validación
    // @Valid      // Ejemplo de validación (cascada la validación a objetos anidados)
    private List<OrdenDetalleRequestDTO> detalles;

    // @NotNull // Ejemplo de validación
    // @Valid    // Ejemplo de validación
    private PagoRequestDTO pago; // Reutiliza el DTO de Pago

    // Getters y Setters
    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public List<OrdenDetalleRequestDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<OrdenDetalleRequestDTO> detalles) {
        this.detalles = detalles;
    }

    public PagoRequestDTO getPago() {
        return pago;
    }

    public void setPago(PagoRequestDTO pago) {
        this.pago = pago;
    }
}