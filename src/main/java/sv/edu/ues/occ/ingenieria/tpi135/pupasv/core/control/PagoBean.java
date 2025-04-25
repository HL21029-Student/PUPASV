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
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Pago;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.PagoDetalle;

/**
 *
 * @author HL21029
 */

@Stateless
@LocalBean
public class PagoBean extends AbstractDataAccess<Pago> implements Serializable{
    
    @PersistenceContext(unitName="PupaSV-PU")
    EntityManager em;
    
    
    public PagoBean(){
        super(Pago.class);
    } 
    
    
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
        public List<PagoDetalle> findDetallesByPago(Long idPago){
        try{
            return em.createNamedQuery("PagoDetalle.findByIdPago", PagoDetalle.class).setParameter("idPago", idPago).getResultList();

        }catch(Exception e){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return List.of();
    }
    
        
    public Pago crearPago(String metodoPago, BigDecimal monto, BigDecimal montoRecibido) {
        validarMetodoPago(metodoPago);
        validarMontos(monto, montoRecibido, metodoPago);
        
        Pago nuevoPago = new Pago();
        nuevoPago.setFecha(new Date());
        nuevoPago.setMetodoPago(metodoPago);
        nuevoPago.setReferencia(generarReferencia(metodoPago));
        
        PagoDetalle detalle = new PagoDetalle();
        detalle.setMonto(monto);
        
        if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
            BigDecimal cambio = montoRecibido.subtract(monto);
            if (cambio.compareTo(BigDecimal.ZERO) > 0) {
                detalle.setObservaciones("Cambio: " + cambio.toString());
            }
        }
        
        detalle.setIdPago(nuevoPago);
        nuevoPago.getPagoDetalleList().add(detalle);
        
        this.create(nuevoPago);
        return nuevoPago;
    }
    
    private void validarMetodoPago(String metodoPago) {
        if (metodoPago == null || !List.of("EFECTIVO", "TARJETA", "TRANSFE").contains(metodoPago.toUpperCase())) {
            throw new IllegalArgumentException("Método de pago no válido");
        }
    }
    
    private void validarMontos(BigDecimal monto, BigDecimal montoRecibido, String metodoPago) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monto debe ser positivo");
        }
        
        if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
            if (montoRecibido == null || montoRecibido.compareTo(monto) < 0) {
                throw new IllegalArgumentException("Monto recibido insuficiente");
            }
        } else {
            if (montoRecibido != null && montoRecibido.compareTo(monto) != 0) {
                throw new IllegalArgumentException("Monto debe coincidir con el total");
            }
        }
    }
    
    private String generarReferencia(String metodoPago) {
        String referencia = "";
        switch (metodoPago.toUpperCase()) {
            case "EFECTIVO":
                referencia = "E-" + System.currentTimeMillis();
                break;
            case "TARJETA":
                referencia = "TARJ-" + System.currentTimeMillis();
                break;
            case "TRANSFE":
                referencia = "TRANSF-" + System.currentTimeMillis();
                break;
            default:
                referencia = "REF-" + System.currentTimeMillis();
                break;
        }
        return referencia;
    }
    
    public void anularPago(Long idPago) {
        Pago pago = this.findById(idPago);
        if (pago != null) {
            if (pago.getIdOrden() != null && !pago.getIdOrden().getAnulada()) {
                throw new IllegalStateException("No se puede anular pago de orden activa");
            }
            
            pago.setReferencia("ANULADO-" + pago.getReferencia());
            this.update(pago);
        }
    }
    
    public BigDecimal calcularCambio(BigDecimal monto, BigDecimal montoRecibido) {
        if (monto == null || montoRecibido == null) {
            throw new IllegalArgumentException("Montos no pueden ser nulos");
        }
        
        return montoRecibido.subtract(monto).max(BigDecimal.ZERO);
    }
}
