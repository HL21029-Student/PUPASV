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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoDetalle;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.TipoProducto;

/**
 *
 * @author HL21029
 */
@Stateless
@LocalBean
public class TipoProductoBean extends AbstractDataAccess<TipoProducto> implements Serializable {

    @PersistenceContext(unitName = "PupaSV-PU")
    EntityManager em;

    public TipoProductoBean() {
        super(TipoProducto.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<Object[]> findProductsInTipoProducto(Long idTipoProducto) {
        try {
            return em.createNamedQuery("Producto.findProductsInTipoProducto")
                    .setParameter("idTipoProducto", idTipoProducto)
                    .getResultList();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
            return List.of();
        }
    }
    
    public TipoProducto crearTipoProducto(String nombre, String observaciones) {
        validarNombre(nombre);
        validarNombreUnico(nombre);
        
        TipoProducto nuevo = new TipoProducto();
        nuevo.setNombre(nombre.trim());
        nuevo.setObservaciones(observaciones);
        nuevo.setActivo(true);
        
        this.create(nuevo);
        return nuevo;
    }

    public TipoProducto actualizarTipoProducto(Long idTipo, String nuevoNombre, String nuevasObservaciones, Boolean activo) {
        TipoProducto tipo = this.findById(idTipo);
        
        if (tipo != null) {
            if (nuevoNombre != null && !nuevoNombre.isBlank()) {
                validarNombre(nuevoNombre);
                validarNombreUnico(nuevoNombre);
                tipo.setNombre(nuevoNombre.trim());
            }
            
            if (nuevasObservaciones != null) {
                tipo.setObservaciones(nuevasObservaciones);
            }
            
            if (activo != null) {
                validarCambioEstado(tipo, activo);
                tipo.setActivo(activo);
            }
            
            return this.update(tipo);
        }
        return null;
    }

    public void desactivarTipoProducto(Long idTipo) {
        TipoProducto tipo = this.findById(idTipo);
        if (tipo != null && tipo.getActivo()) {
            validarRelacionesActivas(tipo);
            tipo.setActivo(false);
            this.update(tipo);
        }
    }

    public List<TipoProducto> buscarPorNombre(String patron) {
        return em.createQuery(
            "SELECT t FROM TipoProducto t WHERE LOWER(t.nombre) LIKE LOWER(:patron)", TipoProducto.class)
            .setParameter("patron", "%" + patron + "%")
            .getResultList();
    }

    public List<TipoProducto> listarTiposActivos() {
        return em.createQuery(
            "SELECT t FROM TipoProducto t WHERE t.activo = TRUE ORDER BY t.nombre", TipoProducto.class)
            .getResultList();
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del tipo es requerido");
        }
        if (nombre.length() > 155) {
            throw new IllegalArgumentException("El nombre no puede exceder los 155 caracteres");
        }
    }

    private void validarNombreUnico(String nombre) {
        Long count = em.createQuery(
            "SELECT COUNT(t) FROM TipoProducto t WHERE LOWER(t.nombre) = LOWER(:nombre)", Long.class)
            .setParameter("nombre", nombre.trim())
            .getSingleResult();

        if (count > 0) {
            throw new IllegalArgumentException("Ya existe un tipo de producto con ese nombre");
        }
    }

    private void validarCambioEstado(TipoProducto tipo, Boolean nuevoEstado) {
        if (tipo.getActivo() && !nuevoEstado) {
            validarRelacionesActivas(tipo);
        }
    }

    private void validarRelacionesActivas(TipoProducto tipo) {
        Long relacionesActivas = em.createQuery(
            "SELECT COUNT(pd) FROM ProductoDetalle pd "
            + "WHERE pd.tipoProducto.idTipoProducto = :idTipo AND pd.activo = TRUE", Long.class)
            .setParameter("idTipo", tipo.getIdTipoProducto())
            .getSingleResult();

        if (relacionesActivas > 0) {
            throw new IllegalStateException("No se puede desactivar un tipo con productos asociados activos");
        }
    }
}
