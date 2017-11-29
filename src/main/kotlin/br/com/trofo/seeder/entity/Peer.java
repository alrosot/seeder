/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.trofo.seeder.entity;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Andoreh
 */
@Entity
@Table(appliesTo = "peer", indexes = {
        @Index(name = "searchIndex", columnNames = {"infoHash", "expires"})})
@javax.persistence.Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"infoHash", "ip", "port"}))
@NamedQueries({
        @NamedQuery(name = "getPeers",
                query = "from Peer where infoHash = :infoHash and expires > :expires and not (port = :port and ip = :ip)"),
        @NamedQuery(name = "findPeer",
                query = "from Peer where infoHash = :infoHash and port = :port and ip = :ip")})
public class Peer implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(length = 40, nullable = false)
    private String infoHash;
    @Column(nullable = false)
    private String ip;
    @Column(nullable = false)
    private Integer port;
    @Column(nullable = false)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date expires;
    private boolean complete;

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(String infoHash) {
        this.infoHash = infoHash;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Peer)) {
            return false;
        }
        Peer other = (Peer) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "br.com.trofo.seeder.entity.Peer[ id=" + id + ", infoHash=" + infoHash + " ]";
    }
}
