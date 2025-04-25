/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List; 

/**
 * DTO para recibir datos al crear o actualizar la información básica de un Combo.
 */
public class ComboInputDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;

    // Para la creación, esta lista define los productos y cantidades.
    // Para la actualización (PUT actual), esta lista NO se usa, ya que el bean no modifica detalles.
    @NotEmpty(message = "El combo debe tener al menos un detalle")
    @Valid // Valida cada objeto ComboDetalleDTO en la lista
    private List<ComboDetalleDTO> detalles;

    // Solo relevante para la actualización (PUT)
    private Boolean activo;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public List<ComboDetalleDTO> getDetalles() { return detalles; }
    public void setDetalles(List<ComboDetalleDTO> detalles) { this.detalles = detalles; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
