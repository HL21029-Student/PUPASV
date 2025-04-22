package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.TipoProductoBean;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.TipoProducto;

@Path("tipoProducto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoProductoResource {

    private static final Logger LOGGER = Logger.getLogger(TipoProductoResource.class.getName());

    @Inject
    TipoProductoBean tpBean;

    @GET
    public Response findRange(@QueryParam("first") @DefaultValue("0") int first,
                              @QueryParam("page_Size") @DefaultValue("50") int pageSize) {
        try {
            List<TipoProducto> lista = tpBean.findRange(first, pageSize);
            long total = tpBean.count();
            return Response.ok(lista)
                    .header(RestResourceHeaderPattern.TOTAL_REGISTROS, total)
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en findRange", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error interno del servidor")
                    .build();
        }
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") Long id) {
        try {
            TipoProducto tipo = tpBean.findById(id);
            return tipo != null
                    ? Response.ok(tipo).build()
                    : Response.status(Response.Status.NOT_FOUND)
                            .header(RestResourceHeaderPattern.DETALLE_ERROR, "TipoProducto no encontrado")
                            .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en findById", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/activos")
    public Response findAllActivos() {
        try {
            List<TipoProducto> activos = tpBean.findAllActivos();
            return Response.ok(activos).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en findAllActivos", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error al obtener activos: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/buscar")
    public Response buscarPorNombre(@QueryParam("nombre") String nombre) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, "Parámetro 'nombre' requerido")
                        .build();
            }
            List<TipoProducto> resultado = tpBean.buscarPorNombre(nombre);
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en buscarPorNombre", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error al buscar por nombre: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/existe")
    public Response existeNombre(@QueryParam("nombre") String nombre) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, "Parámetro 'nombre' requerido")
                        .build();
            }
            boolean existe = tpBean.isNombreRepetido(nombre);
            return Response.ok(existe).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en existeNombre", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error al verificar nombre: " + e.getMessage())
                    .build();
        }
    }

    @POST
    public Response create(TipoProducto tipoProducto) {
        try {
            if (tipoProducto == null) {
                return Response.status(RestResourceHeaderPattern.STATUS_PARAMETRO_FALTANTE)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, RestResourceHeaderPattern.DETALLE_PARAMETRO_FALTANTE)
                        .build();
            }
            tpBean.create(tipoProducto);
            return Response.status(Response.Status.CREATED).entity(tipoProducto).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en create", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Datos inválidos: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Long id, TipoProducto tipoProducto) {
        try {
            TipoProducto existing = tpBean.findById(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, "TipoProducto no encontrado")
                        .build();
            }
            tipoProducto.setIdTipoProducto(id);
            tpBean.update(tipoProducto);
            return Response.ok(tipoProducto).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en update", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error en actualización: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            TipoProducto existing = tpBean.findById(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, "TipoProducto no encontrado")
                        .build();
            }
            tpBean.delete(id);
            return Response.noContent().build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en delete", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error en eliminación: " + e.getMessage())
                    .build();
        }
    }
}

