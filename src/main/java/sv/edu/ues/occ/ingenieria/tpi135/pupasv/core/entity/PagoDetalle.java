/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author HL21029

 */
@Entity
@Table(name = "pago_detalle", catalog = "tipicos_tpi135", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PagoDetalle.findAll", query = "SELECT p FROM PagoDetalle p"),
    @NamedQuery(name = "PagoDetalle.findByIdPagoDetalle", query = "SELECT p FROM PagoDetalle p WHERE p.idPagoDetalle = :idPagoDetalle"),
    @NamedQuery(name = "PagoDetalle.findByMonto", query = "SELECT p FROM PagoDetalle p WHERE p.monto = :monto"),
    @NamedQuery(name = "PagoDetalle.findByObservaciones", query = "SELECT p FROM PagoDetalle p WHERE p.observaciones = :observaciones")})
public class PagoDetalle implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_pago_detalle", nullable = false)
    private Long idPagoDetalle;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "monto", precision = 6, scale = 2)
    private BigDecimal monto;
    @Size(max = 2147483647)
    @Column(name = "observaciones", length = 2147483647)
    private String observaciones;
    
    @JsonIgnore
    @JoinColumn(name = "id_pago", referencedColumnName = "id_pago")
    @ManyToOne(fetch = FetchType.LAZY)
    private Pago idPago;

    public PagoDetalle() {
    }

    public PagoDetalle(Long idPagoDetalle) {
        this.idPagoDetalle = idPagoDetalle;
    }

    public Long getIdPagoDetalle() {
        return idPagoDetalle;
    }

    public void setIdPagoDetalle(Long idPagoDetalle) {
        this.idPagoDetalle = idPagoDetalle;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Pago getIdPago() {
        return idPago;
    }

    public void setIdPago(Pago idPago) {
        this.idPago = idPago;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPagoDetalle != null ? idPagoDetalle.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PagoDetalle)) {
            return false;
        }
        PagoDetalle other = (PagoDetalle) object;
        if ((this.idPagoDetalle == null && other.idPagoDetalle != null) || (this.idPagoDetalle != null && !this.idPagoDetalle.equals(other.idPagoDetalle))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.entity.PagoDetalle[ idPagoDetalle=" + idPagoDetalle + " ]";
    }
    
}
