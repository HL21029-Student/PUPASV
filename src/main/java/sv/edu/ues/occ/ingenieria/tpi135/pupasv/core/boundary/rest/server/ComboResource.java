package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.ComboDetalleDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.ComboDetalleRequestDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.ComboInputDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.ComboRequestDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.ComboUpdateRequestDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.PrecioResponseDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.ComboBean;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.ProductoBean; // Necesario para la firma actual de crearCombo
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Combo;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ComboDetalle;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Producto;


/**
 * Recurso REST para gestionar Combos.
 */
@Path("combos")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComboResource {

    private static final Logger LOG = Logger.getLogger(ComboResource.class.getName());

    @Inject
    ComboBean comboBean;

    @Inject
    ProductoBean productoBean; // Requerido para obtener entidades Producto para ComboDetalle

    @Context
    UriInfo uriInfo;

    /**
     * Endpoint para crear un nuevo combo.
     * IMPORTANTE: Requiere la inyección de ProductoBean debido a la firma actual de ComboBean.
     *
     * @param comboDTO DTO con los datos del combo a crear.
     * @return Response 201 (Created) con la ubicación o 400/409/500 en caso de error.
     */
    @POST
    public Response crearCombo(ComboRequestDTO comboDTO /* @Valid si se utiliza Bean Validation */) {
        if (comboDTO == null || comboDTO.getNombre() == null || comboDTO.getNombre().isBlank()
                || comboDTO.getDetalles() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Nombre y detalles son requeridos.").build();
        }

        List<ComboDetalle> detallesEntidad = new ArrayList<>();
        try {
            // 1. Preparar entidades ComboDetalle obteniendo entidades Producto
            for (ComboDetalleRequestDTO detDTO : comboDTO.getDetalles()) {
                if (detDTO.getIdProducto() == null || detDTO.getCantidad() <= 0) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("ID de producto inválido o cantidad no positiva en detalles.").build();
                }
                Producto producto = productoBean.findById(detDTO.getIdProducto());
                if (producto == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Producto no encontrado para ID: " + detDTO.getIdProducto()).build();
                }
                // Opcional: ¿Verificar si el producto está activo? Depende de los requerimientos.
                if (!producto.getActivo()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Producto inactivo no puede ser agregado al combo: ID " + detDTO.getIdProducto()).build();
                }

                ComboDetalle detalle = new ComboDetalle();
                detalle.setProducto(producto); // Establecer la entidad Producto obtenida
                detalle.setCantidad(detDTO.getCantidad());
                detalle.setActivo(true); // Asumiendo que los detalles están activos al crear
                // detalle.setCombo(??); // Esto será establecido por la gestión de relaciones en el bean/JPA
                detallesEntidad.add(detalle);
            }

            // 2. Llamar al método del bean para crear el combo
            Combo nuevoCombo = comboBean.crearCombo(
                    comboDTO.getNombre(),
                    comboDTO.getDescripcion(),
                    detallesEntidad // Pasar la lista con las entidades Producto obtenidas
            );

            // 3. Construir y retornar la respuesta de éxito
            if (nuevoCombo != null && nuevoCombo.getIdCombo() != null) {
                URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(nuevoCombo.getIdCombo())).build();
                return Response.created(location).entity(nuevoCombo).build();
            } else {
                // No debería ocurrir si la creación funcionó sin excepción, pero es buena práctica
                LOG.log(Level.SEVERE, "ComboBean.crearCombo retornó null o combo sin ID.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error interno al crear el combo.").build();
            }

        } catch (IllegalArgumentException e) { // Captura errores de validación de nombre único, cantidad
            LOG.log(Level.INFO, "Error de validación durante la creación del combo: {0}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build(); // 400 por validación
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inesperado al crear el combo: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error inesperado al crear el combo.").build();
        }
    }

    /**
     * Endpoint para actualizar parcialmente un combo existente.
     *
     * @param id        ID del combo a actualizar.
     * @param updateDTO DTO con los campos a actualizar (null/ausentes no se modifican).
     * @return Response 200 (OK) con el combo actualizado, 404 si no existe, o 400/409 si hay error de validación.
     */
    @PUT
    @Path("{id}")
    public Response actualizarCombo(@PathParam("id") Long id, ComboUpdateRequestDTO updateDTO /* @Valid */) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de combo no proporcionado.").build();
        }
        if (updateDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cuerpo de la solicitud vacío.").build();
        }

        try {
            // Buscar primero para asegurar que existe (proporciona un 404 limpio)
            Combo comboExistente = comboBean.findById(id);
            if (comboExistente == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Combo no encontrado para ID: " + id).build();
            }

            // Llamar al método específico de actualización en el bean
            Combo comboActualizado = comboBean.actualizarCombo(
                    id,
                    updateDTO.getNombre(),        // Pasar null si no se proporciona en el DTO
                    updateDTO.getDescripcion(), // Pasar null si no se proporciona en el DTO
                    updateDTO.getActivo()         // Pasar null si no se proporciona en el DTO
            );

            // actualizarCombo retorna null si no se encuentra, pero ya lo verificamos
            if (comboActualizado != null) {
                return Response.ok(comboActualizado).build();
            } else {
                // Este caso implica que el combo fue eliminado entre findById y update, u otro problema.
                LOG.log(Level.WARNING, "Combo con ID {0} no encontrado durante la actualización, aunque existía previamente.", id);
                return Response.status(Response.Status.NOT_FOUND).entity("Combo no encontrado para ID: " + id).build();
            }

        } catch (IllegalArgumentException e) { // Captura error de validación de nombre único en la actualización
            LOG.log(Level.INFO, "Error de validación durante la actualización del combo (ID: {0}): {1}", new Object[]{id, e.getMessage()});
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalStateException e) { // Captura errores de lógica de activación/desactivación
            LOG.log(Level.INFO, "Error de transición de estado durante la actualización del combo (ID: {0}): {1}", new Object[]{id, e.getMessage()});
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build(); // 409 Conflict es adecuado
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inesperado al actualizar el combo ID " + id + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error inesperado al actualizar el combo.").build();
        }
    }

    /**
     * Endpoint para obtener la lista de combos activos.
     *
     * @return Lista de combos activos.
     */
    @GET
    public Response listarCombosActivos() {
        try {
            List<Combo> combos = comboBean.listarCombosActivos();
            return Response.ok(combos).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al listar combos activos: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener la lista de combos.").build();
        }
    }

    /**
     * Endpoint para obtener un combo específico por su ID.
     *
     * @param id ID del combo.
     * @return Response 200 (OK) con el combo o 404 (Not Found).
     */
    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de combo no proporcionado.").build();
        }
        try {
            Combo combo = comboBean.findById(id);
            if (combo != null) {
                return Response.ok(combo).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Combo no encontrado para ID: " + id).build();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar combo por ID " + id + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al buscar el combo.").build();
        }
    }

    /**
     * Endpoint para buscar combos activos por nombre (coincidencia parcial).
     *
     * @param nombre Texto a buscar en el nombre del combo.
     * @return Lista de combos activos que coinciden.
     */
    @GET
    @Path("buscar")
    public Response buscarCombosActivosPorNombre(@QueryParam("nombre") String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Parámetro 'nombre' es requerido para la búsqueda.").build();
        }
        try {
            List<Combo> combos = comboBean.buscarCombosActivosPorNombre(nombre);
            return Response.ok(combos).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar combos por nombre '" + nombre + "': " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al realizar la búsqueda.").build();
        }
    }

    /**
     * Endpoint para calcular el precio actual de un combo basado en sus detalles activos y precios de productos.
     * No persiste el precio calculado.
     *
     * @param id ID del combo.
     * @return Response 200 (OK) con el precio calculado o 404 si el combo no existe.
     */
    @GET
    @Path("{id}/precio")
    public Response getPrecioCalculadoCombo(@PathParam("id") Long id) {
          if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de combo no proporcionado.").build();
        }
        try {
            Combo combo = comboBean.findById(id);
            if (combo == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Combo no encontrado para ID: " + id).build();
            }
            // Opcional: ¿Verificar si el combo está activo? Depende si el cálculo de precio tiene sentido para los inactivos.
            // if (!combo.getActivo()) {
            //    return Response.status(Response.Status.CONFLICT).entity("No se puede calcular precio de combo inactivo.").build();
            // }

            BigDecimal precio = comboBean.calcularPrecioCombo(combo);
            return Response.ok(new PrecioResponseDTO(precio)).build(); // Envolver en DTO para JSON limpio

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al calcular precio para combo ID " + id + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al calcular el precio del combo.").build();
        }
    }

    /**
     * Endpoint para eliminar un combo. Considerar desactivar en lugar de eliminar.
     *
     * @param id ID del combo a eliminar.
     * @return Response 204 (No Content) si se elimina, 404 si no existe.
     */
    @DELETE
    @Path("{id}")
    public Response removeCombo(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de combo no proporcionado.").build();
        }
        try {
            Combo combo = comboBean.findById(id);
            if (combo == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Combo no encontrado para ID: " + id).build();
            }
            // Añadir aquí comprobaciones de lógica de negocio si es necesario (ej., no eliminar si se usó en pedidos recientes)
            comboBean.remove(combo); // Usando el remove heredado
            return Response.noContent().build();
        } catch (Exception e) {
            // Capturar posibles violaciones de restricciones si el combo está referenciado en otro lado y las cascadas no están configuradas
            LOG.log(Level.SEVERE, "Error al eliminar combo ID " + id + ": " + e.getMessage(), e);
            // Podrías retornar 409 Conflict si la eliminación falla debido a restricciones
            if (e.getCause() instanceof jakarta.persistence.PersistenceException) { // Chequeo básico
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede eliminar el combo, puede estar en uso.").build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar el combo.").build();
        }
    }

    // Opcional: Endpoint para obtener todos los combos (incluyendo inactivos)
    @GET
    @Path("all")
    public Response findAllCombos() {
        try {
            List<Combo> combos = comboBean.findAll();
            return Response.ok(combos).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al encontrar todos los combos: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener todos los combos.").build();
        }
    }

}