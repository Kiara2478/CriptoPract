/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

/**
 *
 * @author ANDREA
 */
@Entity
@Table(name = "persona")
@NamedQueries({
    @NamedQuery(name = "Persona.findAll", query = "SELECT p FROM Persona p"),
    @NamedQuery(name = "Persona.findByCodiPers", query = "SELECT p FROM Persona p WHERE p.codiPers = :codiPers"),
    @NamedQuery(name = "Persona.findByNdniPers", query = "SELECT p FROM Persona p WHERE p.ndniPers = :ndniPers"),
    @NamedQuery(name = "Persona.findByNombPers", query = "SELECT p FROM Persona p WHERE p.nombPers = :nombPers"),
    @NamedQuery(name = "Persona.findByFechaNaciPers", query = "SELECT p FROM Persona p WHERE p.fechaNaciPers = :fechaNaciPers"),
    @NamedQuery(name = "Persona.findByPesoPers", query = "SELECT p FROM Persona p WHERE p.pesoPers = :pesoPers"),
    @NamedQuery(name = "Persona.findByLogiPers", query = "SELECT p FROM Persona p WHERE p.logiPers = :logiPers"),
    @NamedQuery(name = "Persona.findByPassPers", query = "SELECT p FROM Persona p WHERE p.passPers = :passPers")})
public class Persona implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codiPers")
    private Integer codiPers;
    @Size(max = 8)
    @Column(name = "ndniPers")
    private String ndniPers;
    @Size(max = 50)
    @Column(name = "nombPers")
    private String nombPers;
    @Column(name = "fechaNaciPers")
    @Temporal(TemporalType.DATE)
    private Date fechaNaciPers;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "pesoPers")
    private Double pesoPers;
    @Size(max = 100)
    @Column(name = "logiPers")
    private String logiPers;
    @Size(max = 100)
    @Column(name = "passPers")
    private String passPers;

    public Persona() {
    }

    public Persona(Integer codiPers) {
        this.codiPers = codiPers;
    }

    public Integer getCodiPers() {
        return codiPers;
    }

    public void setCodiPers(Integer codiPers) {
        this.codiPers = codiPers;
    }

    public String getNdniPers() {
        return ndniPers;
    }

    public void setNdniPers(String ndniPers) {
        this.ndniPers = ndniPers;
    }

    public String getNombPers() {
        return nombPers;
    }

    public void setNombPers(String nombPers) {
        this.nombPers = nombPers;
    }

    public Date getFechaNaciPers() {
        return fechaNaciPers;
    }

    public void setFechaNaciPers(Date fechaNaciPers) {
        this.fechaNaciPers = fechaNaciPers;
    }

    public Double getPesoPers() {
        return pesoPers;
    }

    public void setPesoPers(Double pesoPers) {
        this.pesoPers = pesoPers;
    }

    public String getLogiPers() {
        return logiPers;
    }

    public void setLogiPers(String logiPers) {
        this.logiPers = logiPers;
    }

    public String getPassPers() {
        return passPers;
    }

    public void setPassPers(String passPers) {
        this.passPers = passPers;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codiPers != null ? codiPers.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Persona)) {
            return false;
        }
        Persona other = (Persona) object;
        if ((this.codiPers == null && other.codiPers != null) || (this.codiPers != null && !this.codiPers.equals(other.codiPers))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dto.Persona[ codiPers=" + codiPers + " ]";
    }
    
}
