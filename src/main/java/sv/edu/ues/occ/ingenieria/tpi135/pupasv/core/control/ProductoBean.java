/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Combo;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Producto;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoPrecio;

/**
 *
 * @author lf22004
 */
@Stateless
@LocalBean
public class ProductoBean extends AbstractDataAccess<Producto> implements Serializable{
    @PersistenceContext(unitName="PupaSV-PU")
    EntityManager em;
    public ProductoBean(){
        super(Producto.class);
    } 
    
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    

    
    // ProductoBean.java

    public List<Producto> listarActivos() {
        try {
            return em.createNamedQuery("Producto.findByActivo", Producto.class)
                    .setParameter("activo", true)
                    .getResultList();
        } catch (Exception ex) {
            Logger.getLogger(ProductoBean.class.getName()).log(Level.SEVERE, "Error al listar productos activos", ex);
            return Collections.emptyList();
        }
    }

    public List<Producto> buscarPorNombre(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Producto> cq = cb.createQuery(Producto.class);
            Root<Producto> root = cq.from(Producto.class);
            cq.select(root).where(cb.like(cb.lower(root.get("nombre")), "%" + keyword.toLowerCase() + "%"));
            return em.createQuery(cq).getResultList();
        } catch (Exception ex) {
            Logger.getLogger(ProductoBean.class.getName()).log(Level.SEVERE, "Error al buscar productos por nombre", ex);
            return Collections.emptyList();
        }
    }

    public List<ProductoPrecio> obtenerPrecios(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return Collections.emptyList();
        }
        try {
            return em.createQuery(
                    "SELECT pp FROM ProductoPrecio pp WHERE pp.idProducto.idProducto = :idProducto",
                    ProductoPrecio.class)
                    .setParameter("idProducto", producto.getIdProducto())
                    .getResultList();
        } catch (Exception ex) {
            Logger.getLogger(ProductoBean.class.getName()).log(Level.SEVERE, "Error al obtener precios del producto", ex);
            return Collections.emptyList();
        }
    }

    public ProductoPrecio obtenerPrecioActual(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return null;
        }
        try {
            List<ProductoPrecio> precios = em.createQuery(
                    "SELECT pp FROM ProductoPrecio pp "
                    + "WHERE pp.idProducto.idProducto = :idProducto "
                    + "AND (pp.fechaHasta IS NULL OR pp.fechaHasta >= CURRENT_DATE) "
                    + "ORDER BY pp.fechaDesde DESC",
                    ProductoPrecio.class)
                    .setParameter("idProducto", producto.getIdProducto())
                    .setMaxResults(1)
                    .getResultList();
            return precios.isEmpty() ? null : precios.get(0);
        } catch (Exception ex) {
            Logger.getLogger(ProductoBean.class.getName()).log(Level.SEVERE, "Error al obtener precio actual", ex);
            return null;
        }
    }

    public void agregarPrecio(Producto producto, BigDecimal precio, Date fechaDesde) {
        if (producto == null || precio == null || fechaDesde == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            ProductoPrecio nuevoPrecio = new ProductoPrecio();
            nuevoPrecio.setIdProducto(producto);
            nuevoPrecio.setPrecioSugerido(precio);
            nuevoPrecio.setFechaDesde(fechaDesde);
            em.persist(nuevoPrecio);
        } catch (Exception ex) {
            Logger.getLogger(ProductoBean.class.getName()).log(Level.SEVERE, "Error al añadir precio", ex);
            throw new IllegalStateException("No se pudo guardar el precio");
        }
    }

    public List<Combo> obtenerCombos(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return Collections.emptyList();
        }
        try {
            return em.createQuery(
                    "SELECT DISTINCT c FROM Combo c "
                    + "JOIN c.comboDetalleList cd "
                    + "WHERE cd.producto.idProducto = :idProducto",
                    Combo.class)
                    .setParameter("idProducto", producto.getIdProducto())
                    .getResultList();
        } catch (Exception ex) {
            Logger.getLogger(ProductoBean.class.getName()).log(Level.SEVERE, "Error al obtener combos", ex);
            return Collections.emptyList();
        }
    }
    
    
}
