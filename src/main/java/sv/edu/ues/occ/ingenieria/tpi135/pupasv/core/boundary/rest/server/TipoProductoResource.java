package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.TipoProductoBean;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.TipoProducto;

/**
 *
 * @author lf22004
 */
@Path("tipoProducto")
public class TipoProductoResource {

    private static final Logger LOGGER = Logger.getLogger(TipoProductoResource.class.getName());

    @Inject
    TipoProductoBean tpBean;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("page_Size") @DefaultValue("50") int pageSize) {
        try {
            List<TipoProducto> lista = tpBean.findRange(first, pageSize);
            long total = tpBean.count(); // Usar long para coincidir con el tipo de count()
            LOGGER.log(Level.INFO, "Consultando tipos de producto: desde {0}, tamaño página {1}", new Object[]{first, pageSize});
            return Response.ok(lista)
                    .header(RestResourceHeaderPattern.TOTAL_REGISTROS, total)
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al consultar tipos de producto. Parámetros: first={0}, pageSize={1}", new Object[]{first, pageSize});
            LOGGER.log(Level.SEVERE, "Detalle del error: ", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error interno del servidor")
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") Long id) { // Cambiado a Long
        try {
            TipoProducto tipo = tpBean.findById(id);
            if (tipo != null) {
                LOGGER.log(Level.INFO, "TipoProducto encontrado ID: {0}", id);
                return Response.ok(tipo).build();
            } else {
                LOGGER.log(Level.WARNING, "TipoProducto no encontrado ID: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, "TipoProducto no encontrado")
                        .build();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar TipoProducto ID: {0}", id);
            LOGGER.log(Level.SEVERE, "Detalle del error: ", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, e.getMessage())
                    .build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(TipoProducto tipoProducto) {
        try {
            if (tipoProducto == null) {
                LOGGER.log(Level.WARNING, "Intento de crear TipoProducto con datos nulos");
                return Response.status(RestResourceHeaderPattern.STATUS_PARAMETRO_FALTANTE)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, RestResourceHeaderPattern.DETALLE_PARAMETRO_FALTANTE)
                        .build();
            }
            tpBean.create(tipoProducto);
            LOGGER.log(Level.INFO, "TipoProducto creado exitosamente ID: {0}", tipoProducto.getIdTipoProducto());
            return Response.status(Response.Status.CREATED).entity(tipoProducto).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear TipoProducto: ");
            LOGGER.log(Level.SEVERE, "Detalle del error: ", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Datos inválidos: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, TipoProducto tipoProducto) { // Cambiado a Long
        try {
            TipoProducto existing = tpBean.findById(id);
            if (existing == null) {
                LOGGER.log(Level.WARNING, "Intento de actualizar TipoProducto inexistente ID: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, "TipoProducto no encontrado")
                        .build();
            }
            tipoProducto.setIdTipoProducto(id); // Asegurar consistencia del ID
            tpBean.update(tipoProducto);
            LOGGER.log(Level.INFO, "TipoProducto actualizado ID: {0}", id);
            return Response.ok(tipoProducto).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar TipoProducto ID: {0}", id);
            LOGGER.log(Level.SEVERE, "Detalle del error: ", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error en actualización: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) { // Cambiado a Long
        try {
            TipoProducto tipo = tpBean.findById(id);
            if (tipo == null) {
                LOGGER.log(Level.WARNING, "Intento de eliminar TipoProducto inexistente ID: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestResourceHeaderPattern.DETALLE_ERROR, "TipoProducto no encontrado")
                        .build();
            }
            tpBean.delete(tipo);
            LOGGER.log(Level.INFO, "TipoProducto eliminado ID: {0}", id);
            return Response.noContent().build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar TipoProducto ID: {0}", id);
            LOGGER.log(Level.SEVERE, "Detalle del error: ", e);
            return Response.serverError()
                    .header(RestResourceHeaderPattern.DETALLE_ERROR, "Error interno: " + e.getMessage())
                    .build();
        }
    }
}
