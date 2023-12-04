package org.iproduct.polling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.FetchType.EAGER;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Vote implements Serializable {

    @TableGenerator(name = "vote_gen",
            table = "id_gen",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "vote_id",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "vote_gen")
    @NotNull
    @Basic(optional = false)
    @Column(updatable = false, insertable = true, nullable = false)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
//    @Email
    @Basic(optional = false)
    @Column(unique = false, updatable = true, insertable = true, nullable = false, length = 255)
    private String email;
    
    @Column(name = "vote_time", unique = false, updatable = true, insertable = true, 
            nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    private Date voteTime = new Date();

    @XmlTransient
    @NotNull
    @ManyToOne(optional = false, targetEntity = Alternative.class)
    private Alternative alternative;

    public Vote() {

    }

    public Date getVoteTime() {
        return this.voteTime;
    }

    public void setVoteTime(Date voteTime) {
        this.voteTime = voteTime;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alternative getAlternative() {
        return this.alternative;
    }

    public void setAlternative(Alternative alternative) {
        this.alternative = alternative;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vote other = (Vote) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Vote{" + "voteTime=" + voteTime + ", id=" + id + ", email=" 
                + email 
                + (alternative != null ? ", alternativeId=" + alternative.getId() : "")
                + '}';
    }

}
