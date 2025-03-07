/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server;

/**
 *
 * @author lf22004
 */

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.ProductoBean;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Combo;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoPrecio;

/**
 *
 * @author lf22004
 */
@Path("producto")
public class ProductoResource implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ProductoResource.class.getName());

    @Inject
    ProductoBean pBean;

    // CRUD Básico
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("page_Size") @DefaultValue("50") int pageSize) {
        try {
            List<Producto> lista = pBean.findRange(first, pageSize);
            int total = pBean.count().intValue();
            LOGGER.log(Level.INFO, "Consultando productos: desde {0}, tamaño página {1}", new Object[]{first, pageSize});
            return Response.ok(lista)
                    .header("total-registros", total)
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al consultar productos. Parámetros: first={0}, pageSize={1}", new Object[]{first, pageSize});
            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") Long id) {
        try {
            Producto producto = pBean.findById(id);
            if (producto != null) {
                LOGGER.log(Level.INFO, "Producto encontrado ID: {0}", id);
                return Response.ok(producto).build();
            } else {
                LOGGER.log(Level.WARNING, "Producto no encontrado ID: {0}", id);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar producto ID: {0}", id);
            return Response.serverError()
                    .entity("Error interno: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Producto producto) {
        try {
            if (producto == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Datos del producto requeridos")
                        .build();
            }
            pBean.create(producto);
            LOGGER.log(Level.INFO, "Producto creado ID: {0}", producto.getIdProducto());
            return Response.status(Response.Status.CREATED).entity(producto).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear producto");
            return Response.serverError()
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, Producto producto) {
        try {
            Producto existente = pBean.findById(id);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            producto.setIdProducto(id); // Mantener consistencia del ID
            pBean.update(producto);
            LOGGER.log(Level.INFO, "Producto actualizado ID: {0}", id);
            return Response.ok(producto).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar producto ID: {0}", id);
            return Response.serverError()
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            Producto producto = pBean.findById(id);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            pBean.delete(producto);
            LOGGER.log(Level.INFO, "Producto eliminado ID: {0}", id);
            return Response.noContent().build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar producto ID: {0}", id);
            return Response.serverError()
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }

    // Métodos específicos de Producto
    @GET
    @Path("activos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarActivos() {
        try {
            List<Producto> activos = pBean.listarActivos();
            return Response.ok(activos).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al listar productos activos");
            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    @GET
    @Path("buscar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorNombre(@QueryParam("keyword") String keyword) {
        try {
            if (keyword == null || keyword.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Parámetro 'keyword' requerido")
                        .build();
            }
            List<Producto> resultados = pBean.buscarPorNombre(keyword);
            return Response.ok(resultados).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en búsqueda por nombre: {0}", keyword);
            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    @GET
    @Path("{id}/precios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPrecios(@PathParam("id") Long id) {
        try {
            Producto producto = pBean.findById(id);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<ProductoPrecio> precios = pBean.obtenerPrecios(producto);
            return Response.ok(precios).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener precios del producto ID: {0}", id);
            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    @GET
    @Path("{id}/precio-actual")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerPrecioActual(@PathParam("id") Long id) {
        try {
            Producto producto = pBean.findById(id);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ProductoPrecio precio = pBean.obtenerPrecioActual(producto);
            return precio != null 
                    ? Response.ok(precio).build() 
                    : Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener precio actual del producto ID: {0}", id);
            return Response.serverError().build();
        }
    }

    @POST
    @Path("{id}/precios")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response agregarPrecio(
            @PathParam("id") Long id,
            @QueryParam("precio") BigDecimal precio,
            @QueryParam("fecha") Date fechaDesde) {
        try {
            Producto producto = pBean.findById(id);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            pBean.agregarPrecio(producto, precio, fechaDesde);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al agregar precio al producto ID: {0}", id);
            return Response.serverError()
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("{id}/combos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerCombos(@PathParam("id") Long id) {
        try {
            Producto producto = pBean.findById(id);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<Combo> combos = pBean.obtenerCombos(producto);
            return Response.ok(combos).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener combos del producto ID: {0}", id);
            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }
}

