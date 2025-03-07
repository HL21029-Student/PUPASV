/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 * @author lf22004
 */
@Embeddable
public class OrdenDetallePK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "id_orden", nullable = false)
    private long idOrden;
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_producto_precio", nullable = false)
    private long idProductoPrecio;

    public OrdenDetallePK() {
    }

    public OrdenDetallePK(long idOrden, long idProductoPrecio) {
        this.idOrden = idOrden;
        this.idProductoPrecio = idProductoPrecio;
    }

    public long getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(long idOrden) {
        this.idOrden = idOrden;
    }

    public long getIdProductoPrecio() {
        return idProductoPrecio;
    }

    public void setIdProductoPrecio(long idProductoPrecio) {
        this.idProductoPrecio = idProductoPrecio;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idOrden;
        hash += (int) idProductoPrecio;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrdenDetallePK)) {
            return false;
        }
        OrdenDetallePK other = (OrdenDetallePK) object;
        if (this.idOrden != other.idOrden) {
            return false;
        }
        if (this.idProductoPrecio != other.idProductoPrecio) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.OrdenDetallePK[ idOrden=" + idOrden + ", idProductoPrecio=" + idProductoPrecio + " ]";
    }
    
}
