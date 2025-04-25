package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;


import java.util.List;
// import jakarta.validation.constraints.*; // Optional Bean Validation
// import jakarta.validation.Valid;

public class ComboRequestDTO {

    // @NotBlank
    private String nombre;

    private String descripcion; // Optional

    // @NotEmpty
    // @Size(min = ComboBean.MIN_PRODUCTOS_COMBO, max = ComboBean.MAX_PRODUCTOS_COMBO) // Reflect bean constraints
    // @Valid // Cascade validation
    private List<ComboDetalleRequestDTO> detalles;

    // Getters and Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<ComboDetalleRequestDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<ComboDetalleRequestDTO> detalles) {
        this.detalles = detalles;
    }
}