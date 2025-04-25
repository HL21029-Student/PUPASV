/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;

import jakarta.validation.constraints.NotBlank; // Opcional: Para validación
import jakarta.validation.constraints.NotEmpty; // Opcional: Para validación
import jakarta.validation.constraints.Size;    // Opcional: Para validación
import java.util.List;

/**
 * DTO para transferir datos al crear o actualizar un Producto.
 * 
 * @author HL21029
 * 
 */
public class ProductoDTO {

    private String nombre;

    private String observaciones;

    private List<Long> idTiposProducto; // Lista de IDs de TipoProducto a asociar

    // Este campo es más relevante para la actualización, para indicar si el producto
    // debe quedar activo o inactivo. Para la creación, el bean lo setea a 'true'.
    private Boolean activo;

    // --- Constructores ---

    public ProductoDTO() {
        // Constructor por defecto necesario para JAX-RS/JSON-B
    }

    public ProductoDTO(String nombre, String observaciones, List<Long> idTiposProducto, Boolean activo) {
        this.nombre = nombre;
        this.observaciones = observaciones;
        this.idTiposProducto = idTiposProducto;
        this.activo = activo;
    }

    // --- Getters y Setters ---

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<Long> getIdTiposProducto() {
        return idTiposProducto;
    }

    public void setIdTiposProducto(List<Long> idTiposProducto) {
        this.idTiposProducto = idTiposProducto;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
