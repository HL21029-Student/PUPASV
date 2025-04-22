/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author HL21029
 */
@Entity
@Table(name = "producto_precio", catalog = "tipicos_tpi135", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProductoPrecio.findAll", query = "SELECT p FROM ProductoPrecio p"),
    @NamedQuery(name = "ProductoPrecio.findByIdProductoPrecio", query = "SELECT p FROM ProductoPrecio p WHERE p.idProductoPrecio = :idProductoPrecio"),
    @NamedQuery(name = "ProductoPrecio.findByFechaDesde", query = "SELECT p FROM ProductoPrecio p WHERE p.fechaDesde = :fechaDesde"),
    @NamedQuery(name = "ProductoPrecio.findByFechaHasta", query = "SELECT p FROM ProductoPrecio p WHERE p.fechaHasta = :fechaHasta"),
    @NamedQuery(name = "ProductoPrecio.findByPrecioSugerido", query = "SELECT p FROM ProductoPrecio p WHERE p.precioSugerido = :precioSugerido")})
public class ProductoPrecio implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_producto_precio", nullable = false)
    private Long idProductoPrecio;
    @Column(name = "fecha_desde")
    @Temporal(TemporalType.DATE)
    private Date fechaDesde;
    @Column(name = "fecha_hasta")
    @Temporal(TemporalType.DATE)
    private Date fechaHasta;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "precio_sugerido", precision = 8, scale = 2)
    private BigDecimal precioSugerido;
    
    @JsonbTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productoPrecio", fetch = FetchType.LAZY)
    private List<OrdenDetalle> ordenDetalleList;
    
    @JsonIgnore
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto idProducto;

    public ProductoPrecio() {
    }

    public ProductoPrecio(Long idProductoPrecio) {
        this.idProductoPrecio = idProductoPrecio;
    }

    public Long getIdProductoPrecio() {
        return idProductoPrecio;
    }

    public void setIdProductoPrecio(Long idProductoPrecio) {
        this.idProductoPrecio = idProductoPrecio;
    }

    public Date getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public Date getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public BigDecimal getPrecioSugerido() {
        return precioSugerido;
    }

    public void setPrecioSugerido(BigDecimal precioSugerido) {
        this.precioSugerido = precioSugerido;
    }

    @XmlTransient
    public List<OrdenDetalle> getOrdenDetalleList() {
        return ordenDetalleList;
    }

    public void setOrdenDetalleList(List<OrdenDetalle> ordenDetalleList) {
        this.ordenDetalleList = ordenDetalleList;
    }

    public Producto getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProductoPrecio != null ? idProductoPrecio.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProductoPrecio)) {
            return false;
        }
        ProductoPrecio other = (ProductoPrecio) object;
        if ((this.idProductoPrecio == null && other.idProductoPrecio != null) || (this.idProductoPrecio != null && !this.idProductoPrecio.equals(other.idProductoPrecio))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.ProductoPrecio[ idProductoPrecio=" + idProductoPrecio + " ]";
    }
    
}
