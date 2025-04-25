/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Orden;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.OrdenDetalle;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Pago;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.PagoDetalle;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoPrecio;

/**
 *
 * @author HL21029
 */
@Stateless
@LocalBean
public class OrdenBean extends AbstractDataAccess<Orden> implements Serializable{

    @PersistenceContext(unitName="PupaSV-PU")
    EntityManager em;
    
    
    public OrdenBean(){
        super(Orden.class);
    } 
    
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
    
    public List<OrdenDetalle> findDetallesByOrden(Long idOrden){
        try{
            return em.createNamedQuery("OrdenDetalle.findByIdOrden", OrdenDetalle.class).setParameter("idOrden", idOrden).getResultList();

        }catch(Exception e){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return List.of();
    }
    
    public Orden crearOrdenCompleta(String sucursal, List<OrdenDetalle> detalles, Pago pago) {
        Orden nuevaOrden = new Orden();
        nuevaOrden.setFecha(new Date());
        nuevaOrden.setSucursal(sucursal);
        nuevaOrden.setAnulada(false);
        
        // Validar y crear detalles de orden
        for (OrdenDetalle detalle : detalles) {
            validarDetalleOrden(detalle);
            detalle.setOrden(nuevaOrden);
            calcularPrecioDetalle(detalle);
            nuevaOrden.getOrdenDetalleList().add(detalle);
        }
        
        // Validar y asociar pago
        validarPago(pago, calcularTotalOrden(nuevaOrden));
        pago.setIdOrden(nuevaOrden);
        nuevaOrden.getPagoList().add(pago);
        
        // Persistir en base de datos
        this.create(nuevaOrden);
        
        return nuevaOrden;
    }

    private void validarDetalleOrden(OrdenDetalle detalle) {
        ProductoPrecio precio = em.find(ProductoPrecio.class, 
                detalle.getOrdenDetallePK().getIdProductoPrecio());
        
        if (precio == null || precio.getFechaHasta() != null) {
            throw new IllegalStateException("Precio no válido o inactivo");
        }
        
        if (detalle.getCantidad() <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor a cero");
        }
    }

    private void calcularPrecioDetalle(OrdenDetalle detalle) {
        ProductoPrecio precio = em.find(ProductoPrecio.class,
                detalle.getOrdenDetallePK().getIdProductoPrecio());
        
        BigDecimal precioUnitario = precio.getPrecioSugerido();
        detalle.setPrecio(precioUnitario.multiply(new BigDecimal(detalle.getCantidad())));
    }

    private BigDecimal calcularTotalOrden(Orden orden) {
        return orden.getOrdenDetalleList().stream()
                .map(OrdenDetalle::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarPago(Pago pago, BigDecimal totalOrden) {
        BigDecimal totalPagos = pago.getPagoDetalleList().stream()
                .map(PagoDetalle::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPagos.compareTo(totalOrden) < 0) {
            throw new IllegalStateException("El pago no cubre el total de la orden");
        }
        
        if (pago.getFecha() == null) {
            pago.setFecha(new Date());
        }
    }
    
    public void anularOrden(Long idOrden) {
        Orden orden = this.findById(idOrden);
        if (orden != null) {
            if (!orden.getAnulada()) {
                orden.setAnulada(true);
                this.update(orden);
                
                // Reversar inventario si es necesario
                // (Implementar lógica específica según requerimientos)
            }
        }
    }

}
