/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
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
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoDetalle;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoDetallePK;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoPrecio;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.TipoProducto;

/**
 *
 * @author HL21029
 */
@Stateless
@LocalBean
public class ProductoBean extends AbstractDataAccess<Producto> implements Serializable{
    @PersistenceContext(unitName="PupaSV-PU")
    EntityManager em;
    
    @Inject
    private ProductoDetalleBean productoDetalleBean; 
    
    public ProductoBean(){
        super(Producto.class);
    } 
    
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    

    
    public Producto crearProducto(String nombre, String observaciones, List<TipoProducto> tipos) {
            // Validar si el nombre ya existe (ejemplo)
            if (existeNombre(nombre)) {
                throw new IllegalArgumentException("El nombre del producto ya existe.");
            }

            Producto nuevoProducto = new Producto();
            nuevoProducto.setNombre(nombre);
            nuevoProducto.setObservaciones(observaciones);
            nuevoProducto.setActivo(true);

            this.create(nuevoProducto); // Persistir el producto para obtener el ID
            em.flush();
            // Ahora que el producto está persistido, podemos crear los ProductoDetalle
            if (tipos != null && !tipos.isEmpty()) {
                for (TipoProducto tipo : tipos) {
                    productoDetalleBean.crearProductoDetalle(nuevoProducto.getIdProducto(), tipo.getIdTipoProducto(), null); // No hay observaciones iniciales
                }
            }

            return nuevoProducto;
        }

    public Producto actualizarProducto(Long idProducto, String nuevoNombre, String nuevasObservaciones, Boolean activo) {
        Producto producto = this.findById(idProducto);

        if (producto != null) {
            if (nuevoNombre != null) {
                if (nuevoNombre.isBlank()) {
                    throw new IllegalArgumentException("El nombre no puede estar vacío.");
                }
                validarNombreUnico(nuevoNombre);
                producto.setNombre(nuevoNombre);
            }

            if (nuevasObservaciones != null) {
                producto.setObservaciones(nuevasObservaciones);
            }

            if (activo != null) {
                producto.setActivo(activo);
            }

            return this.update(producto);
        }
        return null;
    }

    public void agregarTipoProducto(Producto producto, TipoProducto tipo) {
        ProductoDetalle detalle = new ProductoDetalle();
        detalle.setProductoDetallePK(
            new ProductoDetallePK(tipo.getIdTipoProducto().intValue(), producto.getIdProducto())
        );
        detalle.setActivo(true);
        producto.getProductoDetalleList().add(detalle);
    }

    public ProductoPrecio establecerPrecio(Long idProducto, BigDecimal precio) {
        Producto producto = this.findById(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }
        
        // Desactivar precio anterior
        producto.getProductoPrecioList().forEach(p -> p.setFechaHasta(new Date()));
        
        // Crear nuevo precio
        ProductoPrecio nuevoPrecio = new ProductoPrecio();
        nuevoPrecio.setIdProducto(producto);
        nuevoPrecio.setFechaDesde(new Date());
        nuevoPrecio.setPrecioSugerido(precio);
        
        em.persist(nuevoPrecio);
        return nuevoPrecio;
    }

    public List<Producto> buscarPorNombre(String patron) {
        TypedQuery<Producto> query = em.createQuery(
            "SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(:patron)", Producto.class);
        query.setParameter("patron", "%" + patron + "%");
        return query.getResultList();
    }

    public List<Producto> listarActivos() {
        return em.createQuery(
            "SELECT p FROM Producto p WHERE p.activo = TRUE ORDER BY p.nombre", Producto.class)
            .getResultList();
    }
    
    public boolean existeNombre(String nombre) {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM Producto p WHERE p.nombre = :nombre", Long.class);
        query.setParameter("nombre", nombre);
        return query.getSingleResult() > 0;
    }

    public void desactivarProducto(Long idProducto) {
        Producto producto = this.findById(idProducto);
        if (producto != null) {
            producto.setActivo(false);
            this.update(producto);
        }
    }

    public BigDecimal obtenerPrecioActual(Long idProducto) {
        TypedQuery<BigDecimal> query = em.createQuery(
            "SELECT pp.precioSugerido FROM ProductoPrecio pp "
            + "WHERE pp.idProducto.idProducto = :id AND pp.fechaHasta IS NULL "
            + "ORDER BY pp.fechaDesde DESC", BigDecimal.class);
        query.setParameter("id", idProducto);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    private void validarNombreUnico(String nombre) {
        Long count = em.createQuery(
            "SELECT COUNT(p) FROM Producto p WHERE LOWER(p.nombre) = LOWER(:nombre)", Long.class)
            .setParameter("nombre", nombre)
            .getSingleResult();
        
        if (count > 0) {
            throw new IllegalArgumentException("Ya existe un producto con este nombre");
        }
    }
    
}
