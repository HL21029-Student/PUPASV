package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional; // Para potencial control de transacciones
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.OrdenDetalleRequestDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server.DataTransferObject.OrdenRequestDTO;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.OrdenBean;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.PagoBean; // Necesario para la estructura actual del bean
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Orden;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.OrdenDetalle;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.OrdenDetallePK;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Pago;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.PagoDetalle; // Para la creación de Pago


/**
 * Recurso REST para gestionar Órdenes.
 */
@Path("ordenes")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdenResource {

    private static final Logger LOG = Logger.getLogger(OrdenResource.class.getName());

    @Inject
    OrdenBean ordenBean;

    @Inject
    PagoBean pagoBean; // Inyecta PagoBean para crear Pago primero, basado en la firma actual de OrdenBean

    @Context
    UriInfo uriInfo;

    /**
     * Endpoint para crear una nueva orden completa (incluyendo detalles y pago).
     * IMPORTANTE: Se necesita considerar la transaccionalidad. Ver comentarios.
     *
     * @param ordenDTO DTO con los datos de la orden a crear.
     * @return Response con estado 201 (Created) y la ubicación del nuevo recurso,
     * o 400 (Bad Request)/409 (Conflict)/500 (Internal Server Error) en caso de error.
     */
    @POST
    // Considerar añadir @Transactional aquí SI se utiliza JTA con beans CDI configurados correctamente,
    // de lo contrario, refactorizar el EJB para una mejor gestión de transacciones.
    // @Transactional(Transactional.TxType.REQUIRED)
    public Response crearOrdenCompleta(OrdenRequestDTO ordenDTO /* Usar @Valid si se utiliza Bean Validation */) {
        if (ordenDTO == null || ordenDTO.getDetalles() == null || ordenDTO.getDetalles().isEmpty() || ordenDTO.getPago() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El cuerpo de la solicitud está incompleto (faltan detalles o información de pago).").build();
        }

    
        Pago pagoCreado = null;
        try {
            // 1. Crear el Pago utilizando PagoBean basado en la información del DTO
            pagoCreado = pagoBean.crearPago(
                    ordenDTO.getPago().getMetodoPago(),
                    ordenDTO.getPago().getMonto(),
                    ordenDTO.getPago().getMontoRecibido() // Null si no es EFECTIVO, manejado por pagoBean
            );
            // Chequeo básico si la creación del Pago tuvo éxito
            if (pagoCreado == null || pagoCreado.getIdPago() == null) {
                throw new RuntimeException("Falló la creación de la entidad Pago."); // Será capturado abajo
            }

            // 2. Preparar las entidades OrdenDetalle desde los DTOs
            List<OrdenDetalle> detallesEntidad = new ArrayList<>();
            for (OrdenDetalleRequestDTO detDTO : ordenDTO.getDetalles()) {
                OrdenDetalle detEntidad = new OrdenDetalle();
                // Crear la clave compuesta - idOrden será establecido por la gestión de relaciones de JPA
                OrdenDetallePK pk = new OrdenDetallePK();
                pk.setIdProductoPrecio(detDTO.getIdProductoPrecio());
                // pk.setIdOrden(??); // No establecer esto aquí, JPA lo maneja a través de la relación
                detEntidad.setOrdenDetallePK(pk);
                detEntidad.setCantidad(detDTO.getCantidad());
                // El precio se calcula dentro de ordenBean.crearOrdenCompleta
                detallesEntidad.add(detEntidad);
            }

            // 3. Llamar a OrdenBean para crear la Orden con los detalles y el Pago *ya creado*
            Orden nuevaOrden = ordenBean.crearOrdenCompleta(
                    ordenDTO.getSucursal(),
                    detallesEntidad,
                    pagoCreado // Pasar la entidad Pago creada en el paso 1
            );

            // 4. Construir y retornar la respuesta de éxito
            if (nuevaOrden != null && nuevaOrden.getIdOrden() != null) {
                URI location = uriInfo.getBaseUriBuilder()
                        .path(OrdenResource.class)
                        .path(String.valueOf(nuevaOrden.getIdOrden()))
                        .build();
                return Response.created(location).entity(nuevaOrden).build();
            } else {
                // Este caso podría indicar un problema dentro de la lógica del bean si no se lanzó ninguna excepción
                LOG.log(Level.SEVERE, "OrdenBean.crearOrdenCompleta retornó null o una Orden sin ID después de que se creó el Pago (ID: {0}). Potencial inconsistencia.", pagoCreado.getIdPago());
                // Intentar compensar es complejo aquí - resalta la necesidad de una única transacción.
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno al finalizar la creación de la orden.").build();
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Estas excepciones son errores esperados de validación/estado de los beans
            LOG.log(Level.INFO, "Error de validación o estado durante la creación de la orden: {0}", e.getMessage());
            // Potencialmente hacer rollback del Pago si es necesario, pero difícil sin una gestión de transacciones adecuada
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build(); // O CONFLICT (409) dependiendo de la naturaleza de IllegalStateException
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inesperado al crear la orden completa: " + e.getMessage(), e);
            // Potencialmente hacer rollback del Pago si es necesario
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error inesperado procesando la solicitud.").build();
        }
    }


    /**
     * Endpoint para obtener todas las órdenes.
     *
     * @return Lista de todas las órdenes.
     */
    @GET
    public Response findAll() {
        try {
            List<Orden> ordenes = ordenBean.findAll();
            return Response.ok(ordenes).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar todas las órdenes: " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener las órdenes.").build();
        }
    }

    /**
     * Endpoint para obtener una orden por su ID.
     *
     * @param id ID de la orden a buscar.
     * @return Response con la orden encontrada (200 OK) o 404 (Not Found).
     */
    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de orden no proporcionado.").build();
        }
        try {
            Orden orden = ordenBean.findById(id);
            if (orden != null) {
                return Response.ok(orden).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Orden no encontrada para ID: " + id).build();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al buscar orden por ID " + id + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al buscar la orden.").build();
        }
    }

    /**
     * Endpoint para obtener los detalles de una orden específica.
     *
     * @param idOrden ID de la orden cuyos detalles se quieren obtener.
     * @return Lista de OrdenDetalle o 404 si la orden no existe.
     */
    @GET
    @Path("{id}/detalles")
    public Response findDetallesByOrden(@PathParam("id") Long idOrden) {
          if (idOrden == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de orden no proporcionado.").build();
        }
          // Verificar si la orden existe primero para un 404 limpio
        if (ordenBean.findById(idOrden) == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Orden no encontrada para ID: " + idOrden).build();
        }
        try {
            List<OrdenDetalle> detalles = ordenBean.findDetallesByOrden(idOrden);
            return Response.ok(detalles).build(); // OK incluso si la lista está vacía
        } catch (Exception e) { // Debería ser capturado por el bean, pero por seguridad
            LOG.log(Level.SEVERE, "Error buscando detalles para orden ID " + idOrden + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al buscar detalles de la orden.").build();
        }
    }

    /**
     * Endpoint para anular una orden.
     *
     * @param id ID de la orden a anular.
     * @return Response 204 (No Content) si se anula, 404 si no se encuentra.
     */
    @POST // O PUT, POST está bien para acciones
    @Path("{id}/anular")
    public Response anularOrden(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("ID de orden no proporcionado.").build();
        }
        try {
            // Verificar si la orden existe primero
            Orden ordenExistente = ordenBean.findById(id);
            if (ordenExistente == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Orden no encontrada para ID: " + id).build();
            }
            // Verificar si ya está anulada (opcional, el bean podría manejarlo)
            if(ordenExistente.getAnulada()){
                // O retornar 204 ya que ya está en el estado deseado
                return Response.status(Response.Status.CONFLICT).entity("La orden ya está anulada.").build();
            }

            ordenBean.anularOrden(id);
            return Response.noContent().build(); // 204 No Content para acción exitosa
        } catch (Exception e) {
            // Capturar posibles problemas durante la actualización, aunque anularOrden es simple
            LOG.log(Level.SEVERE, "Error inesperado al anular orden ID " + id + ": " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error inesperado al anular la orden.").build();
        }
    }

    
}