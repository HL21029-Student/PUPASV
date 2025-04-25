/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoDetalle;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoDetallePK;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.TipoProducto;

@Stateless
@LocalBean
public class ProductoDetalleBean extends AbstractDataAccess<ProductoDetalle> {

    @PersistenceContext(unitName = "PupaSV-PU")
    private EntityManager em;

    public ProductoDetalleBean() {
        super(ProductoDetalle.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public ProductoDetalle crearProductoDetalle(Long idProducto, Long idTipoProducto, String observaciones) {
        validarRelacionExistente(idProducto, idTipoProducto);
        Producto producto = validarProductoActivo(idProducto);
        TipoProducto tipo = validarTipoProductoActivo(idTipoProducto);
        
        ProductoDetallePK pk = new ProductoDetallePK(
            idTipoProducto.intValue(), 
            idProducto
        );
        
        ProductoDetalle detalle = new ProductoDetalle();
        detalle.setProductoDetallePK(pk);
        detalle.setActivo(true);
        detalle.setObservaciones(observaciones);
        detalle.setProducto(producto);
        detalle.setTipoProducto(tipo);
        
        this.create(detalle);
        return detalle;
    }

    public ProductoDetalle actualizarProductoDetalle(Long idProducto, Long idTipoProducto, Boolean activo, String observaciones) {
        ProductoDetallePK pk = new ProductoDetallePK(
            idTipoProducto.intValue(), 
            idProducto
        );
        
        ProductoDetalle detalle = this.findById(pk);
        if (detalle != null) {
            if (activo != null) {
                validarCambioEstado(detalle, activo);
                detalle.setActivo(activo);
            }
            if (observaciones != null) {
                detalle.setObservaciones(observaciones);
            }
            return this.update(detalle);
        }
        return null;
    }

    public List<ProductoDetalle> buscarPorProducto(Long idProducto) {
        return em.createQuery(
            "SELECT pd FROM ProductoDetalle pd "
            + "WHERE pd.producto.idProducto = :idProducto", ProductoDetalle.class)
            .setParameter("idProducto", idProducto)
            .getResultList();
    }

    public List<ProductoDetalle> buscarPorTipoProducto(Long idTipoProducto) {
        return em.createQuery(
            "SELECT pd FROM ProductoDetalle pd "
            + "WHERE pd.tipoProducto.idTipoProducto = :idTipo", ProductoDetalle.class)
            .setParameter("idTipo", idTipoProducto)
            .getResultList();
    }

    public boolean existeRelacionActiva(Long idProducto, Long idTipoProducto) {
        ProductoDetallePK pk = new ProductoDetallePK(idTipoProducto.intValue(), idProducto);
        ProductoDetalle detalle = em.find(ProductoDetalle.class, pk);
        return detalle != null && detalle.getActivo();
    }

    private void validarRelacionExistente(Long idProducto, Long idTipoProducto) {
        if (existeRelacionActiva(idProducto, idTipoProducto)) {
            throw new IllegalStateException("La relación producto-tipo ya existe");
        }
    }

    private Producto validarProductoActivo(Long idProducto) {
        Producto producto = em.find(Producto.class, idProducto);
        if (producto == null || !producto.getActivo()) {
            throw new IllegalArgumentException("Producto no existe o está inactivo");
        }
        return producto;
    }

    private TipoProducto validarTipoProductoActivo(Long idTipoProducto) {
        TipoProducto tipo = em.find(TipoProducto.class, idTipoProducto);
        if (tipo == null || !tipo.getActivo()) {
            throw new IllegalArgumentException("Tipo de producto no existe o está inactivo");
        }
        return tipo;
    }

    private void validarCambioEstado(ProductoDetalle detalle, Boolean nuevoEstado) {
        if (!nuevoEstado && detalle.getActivo()) {
            validarUsoEnPrecios(detalle);
        }
    }

    private void validarUsoEnPrecios(ProductoDetalle detalle) {
        Long count = em.createQuery(
            "SELECT COUNT(pp) FROM ProductoPrecio pp "
            + "WHERE pp.idProducto.idProducto = :idProducto "
            + "AND pp.fechaHasta IS NULL", Long.class)
            .setParameter("idProducto", detalle.getProducto().getIdProducto())
            .getSingleResult();
        
        if (count > 0) {
            throw new IllegalStateException("No se puede desactivar, el producto tiene precios activos");
        }
    }
}

