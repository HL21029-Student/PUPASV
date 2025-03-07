/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author lf22004
 */
@Entity
@Table(name = "producto_detalle", catalog = "tipicos_tpi135", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProductoDetalle.findAll", query = "SELECT p FROM ProductoDetalle p"),
    @NamedQuery(name = "ProductoDetalle.findByIdTipoProducto", query = "SELECT p FROM ProductoDetalle p WHERE p.productoDetallePK.idTipoProducto = :idTipoProducto"),
    @NamedQuery(name = "ProductoDetalle.findByIdProducto", query = "SELECT p FROM ProductoDetalle p WHERE p.productoDetallePK.idProducto = :idProducto"),
    @NamedQuery(name = "ProductoDetalle.findByActivo", query = "SELECT p FROM ProductoDetalle p WHERE p.activo = :activo"),
    @NamedQuery(name = "ProductoDetalle.findByObservaciones", query = "SELECT p FROM ProductoDetalle p WHERE p.observaciones = :observaciones")})
public class ProductoDetalle implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ProductoDetallePK productoDetallePK;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Producto producto;
    @JoinColumn(name = "id_tipo_producto", referencedColumnName = "id_tipo_producto", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TipoProducto tipoProducto;

    public ProductoDetalle() {
    }

    public ProductoDetalle(ProductoDetallePK productoDetallePK) {
        this.productoDetallePK = productoDetallePK;
    }

    public ProductoDetalle(int idTipoProducto, long idProducto) {
        this.productoDetallePK = new ProductoDetallePK(idTipoProducto, idProducto);
    }

    public ProductoDetallePK getProductoDetallePK() {
        return productoDetallePK;
    }

    public void setProductoDetallePK(ProductoDetallePK productoDetallePK) {
        this.productoDetallePK = productoDetallePK;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public TipoProducto getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(TipoProducto tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (productoDetallePK != null ? productoDetallePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProductoDetalle)) {
            return false;
        }
        ProductoDetalle other = (ProductoDetalle) object;
        if ((this.productoDetallePK == null && other.productoDetallePK != null) || (this.productoDetallePK != null && !this.productoDetallePK.equals(other.productoDetallePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoDetalle[ productoDetallePK=" + productoDetallePK + " ]";
    }
    
}
