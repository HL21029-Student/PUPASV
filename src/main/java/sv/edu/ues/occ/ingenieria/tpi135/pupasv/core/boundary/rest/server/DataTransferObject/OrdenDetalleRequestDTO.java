package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;


// import jakarta.validation.constraints.*; // Si se utiliza Bean Validation

public class OrdenDetalleRequestDTO {

    // @NotNull // Ejemplo de validación
    private Long idProductoPrecio; // Corresponde a OrdenDetallePK.idProductoPrecio

    // @Min(1) // Ejemplo de validación
    private int cantidad;

    // Getters y Setters
    public Long getIdProductoPrecio() {
        return idProductoPrecio;
    }

    public void setIdProductoPrecio(Long idProductoPrecio) {
        this.idProductoPrecio = idProductoPrecio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
