/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author HL21029

 */
@Entity
@Table(name = "orden_detalle", catalog = "tipicos_tpi135", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrdenDetalle.findAll", query = "SELECT o FROM OrdenDetalle o"),
    @NamedQuery(name = "OrdenDetalle.findByIdOrden", query = "SELECT o FROM OrdenDetalle o WHERE o.ordenDetallePK.idOrden = :idOrden"),
    @NamedQuery(name = "OrdenDetalle.findByIdProductoPrecio", query = "SELECT o FROM OrdenDetalle o WHERE o.ordenDetallePK.idProductoPrecio = :idProductoPrecio"),
    @NamedQuery(name = "OrdenDetalle.findByCantidad", query = "SELECT o FROM OrdenDetalle o WHERE o.cantidad = :cantidad"),
    @NamedQuery(name = "OrdenDetalle.findByPrecio", query = "SELECT o FROM OrdenDetalle o WHERE o.precio = :precio"),
    @NamedQuery(name = "OrdenDetalle.findByObservaciones", query = "SELECT o FROM OrdenDetalle o WHERE o.observaciones = :observaciones")})
public class OrdenDetalle implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected OrdenDetallePK ordenDetallePK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad", nullable = false)
    private int cantidad;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "precio", precision = 6, scale = 2)
    private BigDecimal precio;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    
    @JsonIgnore
    @JoinColumn(name = "id_orden", referencedColumnName = "id_orden", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Orden orden;
    
    @JsonIgnore
    @JoinColumn(name = "id_producto_precio", referencedColumnName = "id_producto_precio", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ProductoPrecio productoPrecio;

    public OrdenDetalle() {
    }

    public OrdenDetalle(OrdenDetallePK ordenDetallePK) {
        this.ordenDetallePK = ordenDetallePK;
    }

    public OrdenDetalle(OrdenDetallePK ordenDetallePK, int cantidad) {
        this.ordenDetallePK = ordenDetallePK;
        this.cantidad = cantidad;
    }

    public OrdenDetalle(long idOrden, long idProductoPrecio) {
        this.ordenDetallePK = new OrdenDetallePK(idOrden, idProductoPrecio);
    }

    public OrdenDetallePK getOrdenDetallePK() {
        return ordenDetallePK;
    }

    public void setOrdenDetallePK(OrdenDetallePK ordenDetallePK) {
        this.ordenDetallePK = ordenDetallePK;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public ProductoPrecio getProductoPrecio() {
        return productoPrecio;
    }

    public void setProductoPrecio(ProductoPrecio productoPrecio) {
        this.productoPrecio = productoPrecio;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ordenDetallePK != null ? ordenDetallePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrdenDetalle)) {
            return false;
        }
        OrdenDetalle other = (OrdenDetalle) object;
        if ((this.ordenDetallePK == null && other.ordenDetallePK != null) || (this.ordenDetallePK != null && !this.ordenDetallePK.equals(other.ordenDetallePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.OrdenDetalle[ ordenDetallePK=" + ordenDetallePK + " ]";
    }
    
}
