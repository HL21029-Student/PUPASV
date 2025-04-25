package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.PagoRequestDTO; // Importa el DTO
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.PagoBean;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Pago;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.PagoDetalle;

/**
 * Recurso REST para gestionar Pagos.
 * @author HL21029 (Bean) - Adaptado a JAX-RS por AI
 */
@Path("pagos")
@RequestScoped // Ciclo de vida por solicitud HTTP
@Produces(MediaType.APPLICATION_JSON) // Produce JSON por defecto
@Consumes(MediaType.APPLICATION_JSON) // Consume JSON por defecto
public class PagoResource {

    private static final Logger LOG = Logger.getLogger(PagoResource.class.getName());

    @Inject // Inyección de dependencias (CDI)
    PagoBean pagoBean;

    @Context
    UriInfo uriInfo; // Información sobre la URI de la solicitud actual

    /**
     * Endpoint para crear un nuevo pago usando la lógica específica del bean.
     *
     * @param pagoDTO DTO con los datos del pago a crear.
     * @return Response con estado 201 (Created) y la ubicación del nuevo recurso,
     * o 400 (Bad Request) si los datos son inválidos.
     */
    @POST
    @Path("nuevo") // Ruta específica para la creación simplificada
    // @Valid // Descomenta si usas Bean Validation en el DTO
    public Response crearNuevoPago(PagoRequestDTO pagoDTO) {
        if (pagoDTO == null) {
             return Response.status(Response.Status.BAD_REQUEST).entity("Request body is missing.").build();
        }
        try {
            Pago nuevoPago = pagoBean.crearPago(pagoDTO.getMetodoPago(), pagoDTO.getMonto(), pagoDTO.getMontoRecibido());
            if (nuevoPago != null && nuevoPago.getIdPago() != null) {
                // Construye la URI del nuevo recurso creado
                URI location = uriInfo.getBaseUriBuilder()
                                      .path(PagoResource.class) // Clase del resource
                                      .path(String.valueOf(nuevoPago.getIdPago())) // ID del nuevo pago
                                      .build();
                return Response.created(location).entity(nuevoPago).build();
            } else {
                 LOG.log(Level.SEVERE, "PagoBean.crearPago retornó null o un pago sin ID.");
                 return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al crear el pago.").build();
            }
        } catch (IllegalArgumentException e) {
            LOG.log(Level.INFO, "Error de validación al crear pago: {0}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inesperado al crear pago: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error inesperado procesando la solicitud.").build();
        }
    }

    /**
     * Endpoint para obtener todos los pagos.
     *
     * @return Lista de todos los pagos.
     */
    @GET
    public Response findAll() {
        try {
            List<Pago> pagos = pagoBean.findAll();
            return Response.ok(pagos).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar todos los pagos: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener los pagos.").build();
        }
    }

    /**
     * Endpoint para obtener un pago por su ID.
     *
     * @param id ID del pago a buscar.
     * @return Response con el pago encontrado (200 OK) o 404 (Not Found).
     */
    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") Long id) {
         if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de pago no proporcionado.").build();
        }
        try {
            Pago pago = pagoBean.findById(id);
            if (pago != null) {
                return Response.ok(pago).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Pago no encontrado para ID: " + id).build();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar pago por ID " + id + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al buscar el pago.").build();
        }
    }

     /**
     * Endpoint para obtener los detalles de un pago específico.
     *
     * @param idPago ID del pago cuyos detalles se quieren obtener.
     * @return Lista de PagoDetalle o 404 si el pago no existe.
     */
    @GET
    @Path("{id}/detalles")
    public Response findDetallesByPago(@PathParam("id") Long idPago) {
        if (idPago == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de pago no proporcionado.").build();
        }
        // Primero verifica si el pago existe para dar un 404 adecuado
        if (pagoBean.findById(idPago) == null) {
             return Response.status(Response.Status.NOT_FOUND).entity("Pago no encontrado para ID: " + idPago).build();
        }
        try {
            List<PagoDetalle> detalles = pagoBean.findDetallesByPago(idPago);
            // El método del bean ya maneja la excepción y devuelve lista vacía,
            // así que simplemente devolvemos OK con la lista (puede estar vacía).
            return Response.ok(detalles).build();
        } catch (Exception e) { // Captura por si acaso, aunque el bean ya lo hace
            LOG.log(Level.SEVERE, "Error buscando detalles para pago ID " + idPago + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al buscar detalles del pago.").build();
        }
    }

    /**
     * Endpoint para anular un pago.
     *
     * @param id ID del pago a anular.
     * @return Response 204 (No Content) si se anula, 404 si no se encuentra,
     * o 409 (Conflict) si no se puede anular.
     */
    @POST // Usar POST para acciones que cambian estado (como anular) es común
    @Path("{id}/anular")
    public Response anularPago(@PathParam("id") Long id) {
         if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de pago no proporcionado.").build();
        }
        try {
            // FindById ya está dentro de anularPago, pero llamarlo antes permite un 404 más limpio
             Pago pagoExistente = pagoBean.findById(id);
            if (pagoExistente == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Pago no encontrado para ID: " + id).build();
            }
            pagoBean.anularPago(id);
            return Response.noContent().build(); // 204 No Content es apropiado para acciones exitosas sin cuerpo de respuesta
        } catch (IllegalStateException e) {
             LOG.log(Level.INFO, "Intento de anular pago no permitido para ID {0}: {1}", new Object[]{id, e.getMessage()});
            // 409 Conflict es adecuado cuando la acción no se puede realizar debido al estado actual del recurso
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inesperado al anular pago ID " + id + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error inesperado al anular el pago.").build();
        }
    }

    /**
     * Endpoint para calcular el cambio (confirmar pago en efectivo).
     *
     * @param monto Monto total del pago.
     * @param montoRecibido Monto entregado por el cliente.
     * @return El cambio calculado o 400 si los montos son inválidos.
     */
    @GET
    @Path("calcular-cambio")
    public Response calcularCambio(@QueryParam("monto") BigDecimal monto,
                                   @QueryParam("montoRecibido") BigDecimal montoRecibido) {
        try {
            BigDecimal cambio = pagoBean.calcularCambio(monto, montoRecibido);
            // Devuelve un objeto JSON simple con el cambio
            return Response.ok(new CambioResponse(cambio)).build();
        } catch (IllegalArgumentException e) {
             LOG.log(Level.INFO, "Error en parámetros para calcular cambio: {0}", e.getMessage());
             return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inesperado al calcular cambio: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error inesperado al calcular cambio.").build();
        }
    }

    // Clase interna simple para la respuesta del cálculo de cambio
    private static class CambioResponse {
        public BigDecimal cambio;
        public CambioResponse(BigDecimal cambio) {
            this.cambio = cambio;
        }
    }

}