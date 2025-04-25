/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.control;


import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HL21029
 */
public abstract  class AbstractDataAccess<T> {
        // Se parametriza para mayor seguridad de tipos
    final Class<T> tipoDeDato;
    private String Orden;
    private static final Logger LOGGER = Logger.getLogger(AbstractDataAccess.class.getName());

    public AbstractDataAccess(Class<T> t) {
        this.tipoDeDato = t;
    }

    public abstract EntityManager getEntityManager();

    // Usamos Long (clase) en lugar de long (primitivo)
    public T findById(Long id) throws IllegalArgumentException, IllegalStateException {
        if (id == null) {
            throw new IllegalArgumentException("El parámetro id no puede ser nulo");
        }
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("Error al acceder al repositorio");
        }
        return em.find(tipoDeDato, id);
    }
    
    public T findById(Serializable id) throws IllegalArgumentException, IllegalStateException {
        if (id == null) {
            throw new IllegalArgumentException("El parámetro id (clave primaria compuesta) no puede ser nulo");
        }
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("Error al acceder al repositorio");
        }
        return em.find(tipoDeDato, id);
    }

    public List<T> findAll() throws IllegalStateException {
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("Error al acceder al repositorio");
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(tipoDeDato);
        Root<T> r = cq.from(tipoDeDato);
        cq.select(r);
        TypedQuery<T> q = em.createQuery(cq);
        return q.getResultList();
    }

    public List<T> findRange(int first, int max) throws IllegalStateException, IllegalArgumentException {
        return findRange(first, max, "", ""); // Llama al método con orden vacío
    }

    public List<T> findRange(int first, int max, String orden, String direccion) throws IllegalStateException, IllegalArgumentException {
        EntityManager em = getEntityManager();
        if (first >= 0 && max > 0 && em != null) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(tipoDeDato);
            Root<T> raiz = cq.from(tipoDeDato);
            cq.select(raiz);

            if (!orden.isEmpty()) {
                if (!direccion.equals("ASCENDING")) {
                    cq.orderBy(cb.desc(raiz.get(orden)));
                } else {
                    cq.orderBy(cb.asc(raiz.get(orden)));
                }
            }

            TypedQuery<T> query = em.createQuery(cq);
            query.setFirstResult(first);
            query.setMaxResults(max);
            return query.getResultList();
        }
        return Collections.emptyList();
    }

    /**
     * Almacena un registro en el repositorio.
     * @param registro Registro a guardar.
     * @throws IllegalStateException Si no puede acceder al repositorio.
     * @throws IllegalArgumentException Si el registro es nulo.
     */
    public void create(T registro) throws IllegalStateException, IllegalArgumentException {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        EntityManager em = getEntityManager();
        if (em == null) {
            throw new IllegalStateException("Error al acceder al repositorio");
        }
        em.persist(registro);
    }

    public T update(T registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        EntityManager em = getEntityManager();
        if (em != null) {
            return em.merge(registro);
        } else {
            throw new IllegalStateException("Error al acceder al repositorio");
        }
    }

    // Se utiliza CriteriaQuery<Long> para contar las entidades, lo cual es adecuado
    public Long count() {
        EntityManager em = getEntityManager();
        if (em != null) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> raiz = cq.from(tipoDeDato);
            cq.select(cb.count(raiz));
            TypedQuery<Long> q = em.createQuery(cq);
            return q.getSingleResult();
        }
        return 0L;
    }

    public void delete(T registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }
        EntityManager em = getEntityManager();
        if (em != null) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaDelete<T> dq = cb.createCriteriaDelete(tipoDeDato);
            Root<T> raiz = dq.from(tipoDeDato);
            dq.where(cb.equal(raiz, registro));
            em.createQuery(dq).executeUpdate();
        } else {
            throw new IllegalStateException("Error al acceder al repositorio");
        }
    }

}

