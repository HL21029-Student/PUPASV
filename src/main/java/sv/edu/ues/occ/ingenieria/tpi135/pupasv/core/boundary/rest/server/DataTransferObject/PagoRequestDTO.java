package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;

import java.math.BigDecimal;

// Puedes añadir anotaciones de validación si usas Bean Validation (jakarta.validation.constraints.*)
// @XmlRootElement // Descomenta si necesitas soporte XML además de JSON
public class PagoRequestDTO {

    private String metodoPago;
    private BigDecimal monto;
    private BigDecimal montoRecibido; // Requerido solo si metodoPago es EFECTIVO

    // Getters y Setters
    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }
}