package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Combo;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ComboDetalle;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ComboDetallePK;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Producto;

@Stateless
@LocalBean
public class ComboDetalleBean extends AbstractDataAccess<ComboDetalle> {

    private static final int CANTIDAD_MINIMA = 1;
    private static final int CANTIDAD_MAXIMA = 10;

    @PersistenceContext(unitName = "PupaSV-PU")
    private EntityManager em;
    
    @Inject
    private ComboBean comboBean;

    @Inject
    private ProductoBean productoBean;
    
    public ComboDetalleBean() {
        super(ComboDetalle.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ComboDetalle> findDetallesByCombo(Long idCombo) {
        try {
            return em.createNamedQuery("ComboDetalle.findByIdCombo", ComboDetalle.class)
                .setParameter("idCombo", idCombo)
                .getResultList();
        } catch(Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return List.of();
    }

    public ComboDetalle crearDetalle(Long idCombo, Long idProducto, Integer cantidad) {
        validarExistenciaPrevia(idCombo, idProducto);
        Combo combo = validarComboActivo(idCombo);
        Producto producto = validarProductoActivo(idProducto);
        validarProductoParaCombo(producto);
        
        ComboDetalle detalle = new ComboDetalle();
        detalle.setComboDetallePK(new ComboDetallePK(idCombo, idProducto));
        detalle.setCantidad(validarCantidad(cantidad));
        detalle.setActivo(true);
        detalle.setCombo(combo);
        detalle.setProducto(producto);
        
        this.create(detalle);
        
        // Actualizamos el precio del combo
        comboBean.calcularPrecioCombo(combo);
        
        return detalle;
    }

    public ComboDetalle actualizarCantidad(Long idCombo, Long idProducto, Integer nuevaCantidad) {
        ComboDetallePK pk = new ComboDetallePK(idCombo, idProducto);
        ComboDetalle detalle = this.findById(pk);
        
        if (detalle != null) {
            detalle.setCantidad(validarCantidad(nuevaCantidad));
            ComboDetalle updated = this.update(detalle);
            
            // Actualizamos el precio del combo
            comboBean.calcularPrecioCombo(detalle.getCombo());
            
            return updated;
        }
        return null;
    }

    public void toggleActivo(Long idCombo, Long idProducto) {
        ComboDetallePK pk = new ComboDetallePK(idCombo, idProducto);
        ComboDetalle detalle = this.findById(pk);
        
        if (detalle != null) {
            detalle.setActivo(!detalle.getActivo());
            validarMinimosProductosActivos(detalle.getCombo());
            this.update(detalle);
            
            // Actualizamos el precio del combo
            comboBean.calcularPrecioCombo(detalle.getCombo());
        }
    }
    
    public void agregarProductoACombo(Long idCombo, Long idProducto, Integer cantidad) {
        Combo combo = em.find(Combo.class, idCombo);
        Producto producto = em.find(Producto.class, idProducto);
        
        if (combo == null || producto == null) {
            throw new IllegalArgumentException("Combo o producto no encontrado");
        }
        
        validarProductoParaCombo(producto);
        validarExistenciaPrevia(idCombo, idProducto);
        
        ComboDetalle detalle = new ComboDetalle();
        detalle.setComboDetallePK(new ComboDetallePK(idCombo, idProducto));
        detalle.setCantidad(validarCantidad(cantidad));
        detalle.setActivo(true);
        detalle.setCombo(combo);
        detalle.setProducto(producto);
        
        this.create(detalle);
        
        // Validamos que no exceda el máximo de productos permitidos
        validarMaximosProductos(combo);
        
        // Actualizamos el precio del combo
        comboBean.calcularPrecioCombo(combo);
    }

    public List<ComboDetalle> buscarPorCombo(Long idCombo) {
        return em.createQuery(
            "SELECT cd FROM ComboDetalle cd WHERE cd.combo.idCombo = :idCombo", 
            ComboDetalle.class)
            .setParameter("idCombo", idCombo)
            .getResultList();
    }

    public List<ComboDetalle> buscarPorProducto(Long idProducto) {
        return em.createQuery(
            "SELECT cd FROM ComboDetalle cd WHERE cd.producto.idProducto = :idProducto", 
            ComboDetalle.class)
            .setParameter("idProducto", idProducto)
            .getResultList();
    }

    private Integer validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < CANTIDAD_MINIMA || cantidad > CANTIDAD_MAXIMA) {
            throw new IllegalArgumentException(
                String.format("Cantidad debe estar entre %d y %d", 
                CANTIDAD_MINIMA, CANTIDAD_MAXIMA)
            );
        }
        return cantidad;
    }

    private void validarExistenciaPrevia(Long idCombo, Long idProducto) {
        Long count = em.createQuery(
            "SELECT COUNT(cd) FROM ComboDetalle cd "
            + "WHERE cd.combo.idCombo = :idCombo "
            + "AND cd.producto.idProducto = :idProducto", Long.class)
            .setParameter("idCombo", idCombo)
            .setParameter("idProducto", idProducto)
            .getSingleResult();
        
        if (count > 0) {
            throw new IllegalStateException("El producto ya está en el combo");
        }
    }

    private Combo validarComboActivo(Long idCombo) {
        Combo combo = em.find(Combo.class, idCombo);
        if (combo == null || !combo.getActivo()) {
            throw new IllegalArgumentException("Combo no disponible");
        }
        return combo;
    }

    private Producto validarProductoActivo(Long idProducto) {
        Producto producto = em.find(Producto.class, idProducto);
        if (producto == null || !producto.getActivo()) {
            throw new IllegalArgumentException("Producto no disponible");
        }
        return producto;
    }
    
    private void validarProductoParaCombo(Producto producto) {
        if (producto == null || !producto.getActivo()) {
            throw new IllegalArgumentException("Producto no disponible para combos");
        }

        // Usamos el método del ProductoBean para obtener el precio actual
        if (productoBean.obtenerPrecioActual(producto.getIdProducto()) == null ||
            productoBean.obtenerPrecioActual(producto.getIdProducto()).compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Producto sin precio válido");
        }
    }

    private void validarMinimosProductosActivos(Combo combo) {
        long activos = buscarPorCombo(combo.getIdCombo()).stream()
            .filter(ComboDetalle::getActivo)
            .count();
        
        if (activos < ComboBean.MIN_PRODUCTOS_COMBO) {
            throw new IllegalStateException(
                "El combo debe tener al menos " + 
                ComboBean.MIN_PRODUCTOS_COMBO + 
                " productos activos"
            );
        }
    }
    
    private void validarMaximosProductos(Combo combo) {
        long totalProductos = buscarPorCombo(combo.getIdCombo()).size();
        
        if (totalProductos > ComboBean.MAX_PRODUCTOS_COMBO) {
            throw new IllegalArgumentException(
                "El combo no puede tener más de " + 
                ComboBean.MAX_PRODUCTOS_COMBO + 
                " productos"
            );
        }
    }
    
    public boolean existeEnComboActivo(Long idProducto) {
        Long count = em.createQuery(
            "SELECT COUNT(cd) FROM ComboDetalle cd "
            + "WHERE cd.producto.idProducto = :idProducto "
            + "AND cd.combo.activo = TRUE", Long.class)
            .setParameter("idProducto", idProducto)
            .getSingleResult();
        
        return count > 0;
    }
}