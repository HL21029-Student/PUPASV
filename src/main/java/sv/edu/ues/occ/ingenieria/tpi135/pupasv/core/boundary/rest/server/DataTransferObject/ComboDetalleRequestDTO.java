package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;


// import jakarta.validation.constraints.*; // Optional Bean Validation

public class ComboDetalleRequestDTO {

    // @NotNull
    private Long idProducto;

    // @Min(1)
    private int cantidad;

    // Getters and Setters
    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
