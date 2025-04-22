package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.TipoProducto;
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Producto;

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

    /**
     * Elimina un TipoProducto por su ID si existe.
     * @param id ID del tipo de producto a eliminar
     */
    public void delete(Long id) {
        TipoProducto entidad = getEntityManager().find(TipoProducto.class, id);
        if (entidad != null) {
            getEntityManager().remove(entidad);
        }
    }

    /**
     * Retorna todos los tipos de producto que estén activos.
     * @return Lista de TipoProducto con campo activo = true
     */
    public List<TipoProducto> findAllActivos() {
        return em.createQuery(
            "SELECT t FROM TipoProducto t WHERE t.activo = true", TipoProducto.class
        ).getResultList();
    }

    /**
     * Busca TipoProducto por coincidencia parcial en nombre (ignorando mayúsculas/minúsculas).
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de coincidencias
     */
    public List<TipoProducto> buscarPorNombre(String nombre) {
        return em.createQuery(
            "SELECT t FROM TipoProducto t WHERE LOWER(t.nombre) LIKE :nombre", TipoProducto.class
        ).setParameter("nombre", "%" + nombre.toLowerCase() + "%")
         .getResultList();
    }

    /**
     * Verifica si existe un TipoProducto con el mismo nombre (ignora mayúsculas).
     * @param nombre Nombre a verificar
     * @return true si ya existe, false si es único
     */
    public boolean isNombreRepetido(String nombre) {
        Long count = em.createQuery(
            "SELECT COUNT(t) FROM TipoProducto t WHERE LOWER(t.nombre) = :nombre", Long.class
        ).setParameter("nombre", nombre.toLowerCase())
         .getSingleResult();
        return count > 0;
    }

    /**
     * Obtiene todos los productos activos asociados a un tipo de producto, con precios no nulos.
     * @param idTipoProducto ID del tipo de producto
     * @return Lista de productos activos con precio no nulo
     */
    public List<Producto> findProductosActivosConPrecioPorTipo(Long idTipoProducto) {
        return em.createQuery(
            "SELECT p FROM Producto p WHERE p.tipoProducto.idTipoProducto = :idTipo AND p.activo = true AND p.precio IS NOT NULL",
            Producto.class
        )
        .setParameter("idTipo", idTipoProducto)
        .getResultList();
    }
}





