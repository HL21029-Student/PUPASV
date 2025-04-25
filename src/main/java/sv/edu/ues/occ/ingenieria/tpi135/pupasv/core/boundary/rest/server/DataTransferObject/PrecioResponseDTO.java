package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;


import java.math.BigDecimal;

public class PrecioResponseDTO {
    private BigDecimal precioCalculado;

    public PrecioResponseDTO(BigDecimal precioCalculado) {
        this.precioCalculado = precioCalculado;
    }

    // Getter
    public BigDecimal getPrecioCalculado() {
        return precioCalculado;
    }
   
}
