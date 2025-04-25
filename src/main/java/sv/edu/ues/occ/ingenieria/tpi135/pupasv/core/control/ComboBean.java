package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Combo;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ComboDetalle;

/**
 *
 * @author HL21029
 */
@Stateless
@LocalBean
public class ComboBean extends AbstractDataAccess<Combo> implements Serializable {

    public static final int MIN_PRODUCTOS_COMBO = 2;
    public static final int MAX_PRODUCTOS_COMBO = 10;

    @PersistenceContext(unitName = "PupaSV-PU")
    EntityManager em;

    @Inject
    ComboDetalleBean comboDetalleBean;

    @Inject
    ProductoBean productoBean;

    public ComboBean() {
        super(Combo.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public Combo crearCombo(String nombre, String descripcion, List<ComboDetalle> detalles) {
        validarNombreUnico(nombre);
        validarCantidadDetalles(detalles);

        Combo nuevoCombo = new Combo();
        nuevoCombo.setNombre(nombre);
        nuevoCombo.setDescripcionPublica(descripcion);
        nuevoCombo.setActivo(true);

        // Primero creamos el combo sin detalles
        this.create(nuevoCombo);

        // Agregamos los detalles después de crear el combo
        for (ComboDetalle detalle : detalles) {
            comboDetalleBean.crearDetalle(
                nuevoCombo.getIdCombo(),
                detalle.getProducto().getIdProducto(),
                detalle.getCantidad()
            );
        }

        return nuevoCombo; // Return the combo, but it won't have a price persisted
    }

    public Combo actualizarCombo(Long idCombo, String nuevoNombre, String nuevaDescripcion, Boolean activo) {
        Combo combo = this.findById(idCombo);

        if (combo != null) {
            if (nuevoNombre != null && !nuevoNombre.isBlank()) {
                validarNombreUnico(nuevoNombre);
                combo.setNombre(nuevoNombre);
            }

            if (nuevaDescripcion != null) {
                combo.setDescripcionPublica(nuevaDescripcion);
            }

            if (activo != null) {
                validarCambioEstado(combo, activo);
                combo.setActivo(activo);
            }

            return this.update(combo);
        }
        return null;
    }

    public List<Combo> buscarCombosActivosPorNombre(String nombre) {
        return em.createQuery(
            "SELECT c FROM Combo c WHERE c.activo = TRUE AND LOWER(c.nombre) LIKE LOWER(:nombre)",
            Combo.class)
            .setParameter("nombre", "%" + nombre + "%")
            .getResultList();
    }

    // This method will only calculate the price, but not update the database
    public BigDecimal calcularPrecioCombo(Combo combo) {
        List<ComboDetalle> detalles = comboDetalleBean.buscarPorCombo(combo.getIdCombo());

        BigDecimal precioTotal = detalles.stream()
            .filter(ComboDetalle::getActivo)
            .map(d -> productoBean.obtenerPrecioActual(d.getProducto().getIdProducto())
                .multiply(BigDecimal.valueOf(d.getCantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return precioTotal;
    }

    private void validarNombreUnico(String nombre) {
        Long count = em.createQuery(
            "SELECT COUNT(c) FROM Combo c WHERE LOWER(c.nombre) = LOWER(:nombre)", Long.class)
            .setParameter("nombre", nombre)
            .getSingleResult();

        if (count > 0) {
            throw new IllegalArgumentException("Ya existe un combo con este nombre");
        }
    }

    private void validarCantidadDetalles(List<ComboDetalle> detalles) {
        if (detalles == null || detalles.size() < MIN_PRODUCTOS_COMBO) {
            throw new IllegalArgumentException("El combo debe tener al menos " + MIN_PRODUCTOS_COMBO + " productos");
        }

        if (detalles.size() > MAX_PRODUCTOS_COMBO) {
            throw new IllegalArgumentException("Máximo " + MAX_PRODUCTOS_COMBO + " productos por combo");
        }
    }

    private void validarCambioEstado(Combo combo, Boolean nuevoEstado) {
        if (nuevoEstado && !combo.getActivo()) {
            // Verificamos que todos los productos de los detalles estén activos utilizando ComboDetalleBean
            List<ComboDetalle> detalles = comboDetalleBean.buscarPorCombo(combo.getIdCombo());

            boolean todosProductosActivos = detalles.stream()
                .allMatch(d -> productoBean.findById(d.getProducto().getIdProducto()).getActivo()); // Asumiendo que ProductoBean tiene findById

            if (!todosProductosActivos) {
                throw new IllegalStateException("No se puede activar combo con productos inactivos");
            }

            // Verificamos que tenga al menos el mínimo de productos activos utilizando ComboDetalleBean
            long detallesActivos = detalles.stream()
                .filter(ComboDetalle::getActivo)
                .count();

            if (detallesActivos < MIN_PRODUCTOS_COMBO) {
                throw new IllegalStateException(
                    "El combo debe tener al menos " +
                    MIN_PRODUCTOS_COMBO +
                    " productos activos"
                );
            }
        }
    }

    public List<Combo> listarCombosActivos() {
        return em.createQuery(
            "SELECT c FROM Combo c WHERE c.activo = TRUE ORDER BY c.nombre", Combo.class)
            .getResultList();
    }

    public void remove(Combo combo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
