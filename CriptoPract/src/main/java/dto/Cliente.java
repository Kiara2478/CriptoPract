/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author ANDREA
 */
@Entity
@Table(name = "cliente")
@NamedQueries({
    @NamedQuery(name = "Cliente.findAll", query = "SELECT c FROM Cliente c"),
    @NamedQuery(name = "Cliente.findByCodiClie", query = "SELECT c FROM Cliente c WHERE c.codiClie = :codiClie"),
    @NamedQuery(name = "Cliente.findByNdniClie", query = "SELECT c FROM Cliente c WHERE c.ndniClie = :ndniClie"),
    @NamedQuery(name = "Cliente.findByNombClie", query = "SELECT c FROM Cliente c WHERE c.nombClie = :nombClie"),
    @NamedQuery(name = "Cliente.findByCeluClie", query = "SELECT c FROM Cliente c WHERE c.celuClie = :celuClie"),
    @NamedQuery(name = "Cliente.findByDireClie", query = "SELECT c FROM Cliente c WHERE c.direClie = :direClie")})
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codiClie")
    private Integer codiClie;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 8)
    @Column(name = "ndniClie")
    private String ndniClie;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "nombClie")
    private String nombClie;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 9)
    @Column(name = "celuClie")
    private String celuClie;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "direClie")
    private String direClie;

    public Cliente() {
    }

    public Cliente(Integer codiClie) {
        this.codiClie = codiClie;
    }

    public Cliente(Integer codiClie, String ndniClie, String nombClie, String celuClie, String direClie) {
        this.codiClie = codiClie;
        this.ndniClie = ndniClie;
        this.nombClie = nombClie;
        this.celuClie = celuClie;
        this.direClie = direClie;
    }

    public Integer getCodiClie() {
        return codiClie;
    }

    public void setCodiClie(Integer codiClie) {
        this.codiClie = codiClie;
    }

    public String getNdniClie() {
        return ndniClie;
    }

    public void setNdniClie(String ndniClie) {
        this.ndniClie = ndniClie;
    }

    public String getNombClie() {
        return nombClie;
    }

    public void setNombClie(String nombClie) {
        this.nombClie = nombClie;
    }

    public String getCeluClie() {
        return celuClie;
    }

    public void setCeluClie(String celuClie) {
        this.celuClie = celuClie;
    }

    public String getDireClie() {
        return direClie;
    }

    public void setDireClie(String direClie) {
        this.direClie = direClie;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codiClie != null ? codiClie.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Cliente)) {
            return false;
        }
        Cliente other = (Cliente) object;
        if ((this.codiClie == null && other.codiClie != null) || (this.codiClie != null && !this.codiClie.equals(other.codiClie))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "dto.Cliente[ codiClie=" + codiClie + " ]";
    }
    
}
