package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid; // Para validación de DTOs (opcional)
import jakarta.validation.constraints.NotNull; // Para validación de DTOs (opcional)
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.ProductoBean;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.TipoProductoBean; // Necesario para buscar TipoProducto por ID
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoPrecio;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.TipoProducto;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.PrecioDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.ProductoDTO;

@Path("productos")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoResource {

    private static final Logger LOGGER = Logger.getLogger(ProductoResource.class.getName());

    @Inject
    ProductoBean productoBean;

    @Inject
    TipoProductoBean tipoProductoBean; // Inyectar para buscar Tipos por ID

    @Context
    UriInfo uriInfo;

    @POST
    public Response crearProducto(@Valid ProductoDTO dto) { // @Valid activa validación si se usan annotations
        if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank() || dto.getIdTiposProducto() == null || dto.getIdTiposProducto().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Nombre y al menos un idTipoProducto son requeridos.")
                           .build();
        }
 
        try {
            List<TipoProducto> tipos = dto.getIdTiposProducto().stream()
                    .map(id -> tipoProductoBean.findById(id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (tipos.size() != dto.getIdTiposProducto().size()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Uno o más idTipoProducto no son válidos.")
                        .build();
            }

            Producto nuevoProducto = productoBean.crearProducto(dto.getNombre(), dto.getObservaciones(), tipos);
            URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(nuevoProducto.getIdProducto())).build();
            return Response.created(location).entity(nuevoProducto).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error al crear producto: {0}", e.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al crear producto", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al crear el producto.").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response actualizarProducto(@PathParam("id") Long idProducto, ProductoDTO dto) { // Elimina @Valid
        if (dto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Cuerpo de la solicitud no puede estar vacío.")
                           .build();
        }
        try {
            // Validar nombre manualmente si está presente
            if (dto.getNombre() != null && dto.getNombre().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("El nombre no puede estar vacío.")
                               .build();
            }

            Producto actualizado = productoBean.actualizarProducto(
                idProducto, 
                dto.getNombre(), 
                dto.getObservaciones(), 
                dto.getActivo()
            );

            if (actualizado != null) {
                return Response.ok(actualizado).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Producto no encontrado.").build();
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error al actualizar producto {0}: {1}", new Object[]{idProducto, e.getMessage()});
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al actualizar producto " + idProducto, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al actualizar el producto.").build();
        }
    }
    
    
    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long idProducto) {
        Producto producto = productoBean.findById(idProducto);
        if (producto != null) {
            return Response.ok(producto).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Producto no encontrado.").build();
        }
    }

    @GET
    public Response buscarProductos(@QueryParam("nombre") String patron) {
        List<Producto> productos;
        if (patron != null && !patron.isBlank()) {
            productos = productoBean.buscarPorNombre(patron);
        } else {
            productos = productoBean.findAll(); // Asume que findAll() existe en AbstractDataAccess o ProductoBean
        }
        return Response.ok(productos).build();
    }

    @GET
    @Path("/activos")
    public Response listarProductosActivos() {
        List<Producto> productos = productoBean.listarActivos();
        return Response.ok(productos).build();
    }

    @DELETE
    @Path("/{id}")
    public Response desactivarProducto(@PathParam("id") Long idProducto) {
        try {
             Producto producto = productoBean.findById(idProducto);
             if (producto == null) {
                 return Response.status(Response.Status.NOT_FOUND).entity("Producto no encontrado.").build();
             }
            productoBean.desactivarProducto(idProducto);
            return Response.noContent().build(); // 204 No Content
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Error inesperado al desactivar producto " + idProducto, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al desactivar el producto.").build();
        }
    }

    @PUT // O POST si se prefiere crear un sub-recurso
    @Path("/{id}/precio")
    public Response establecerPrecio(@PathParam("id") Long idProducto, @Valid PrecioDTO dto) {
         if (dto == null || dto.getPrecio() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Precio es requerido.").build();
        }
        try {
            ProductoPrecio nuevoPrecio = productoBean.establecerPrecio(idProducto, dto.getPrecio());
            return Response.ok(nuevoPrecio).build(); // Devuelve el objeto ProductoPrecio creado/actualizado
        } catch (IllegalArgumentException e) {
            // Captura "Producto no encontrado" del bean
            LOGGER.log(Level.WARNING, "Error al establecer precio para producto {0}: {1}", new Object[]{idProducto, e.getMessage()});
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Error inesperado al establecer precio para producto " + idProducto, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al establecer el precio.").build();
        }
    }

    @GET
    @Path("/{id}/precio/actual")
    public Response obtenerPrecioActual(@PathParam("id") Long idProducto) {
        try {
            BigDecimal precio = productoBean.obtenerPrecioActual(idProducto);
            // Envuelve el BigDecimal en un objeto JSON simple para la respuesta
            return Response.ok(Collections.singletonMap("precioActual", precio)).build();
        } catch (NoResultException e) {
            LOGGER.log(Level.WARNING, "No se encontró precio actual para producto {0}", idProducto);
            return Response.status(Response.Status.NOT_FOUND).entity("Precio actual no encontrado para este producto.").build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado al obtener precio actual para producto " + idProducto, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al obtener el precio.").build();
        }
    }

     // Endpoint para obtener todos los productos (llamando a findAll del bean)
    @GET
    @Path("/todos") // Ruta explícita para evitar ambigüedad con buscarPorNombre si no se pasa query param
    public Response obtenerTodos() {
        List<Producto> todos = productoBean.findAll();
        return Response.ok(todos).build();
    }
}