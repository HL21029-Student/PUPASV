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
import sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.Orden;

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
    
}
