package org.iproduct.polling.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.FetchType.EAGER;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Poll implements Serializable {
    @TableGenerator(name = "poll_gen",
            table = "id_gen",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "poll_id",
            allocationSize = 1
    )
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "poll_gen")
    @Basic(optional = false)
    @Column(updatable = false, insertable = true, nullable = false)
    private Long id;

    @Column(unique = false, updatable = true, insertable = true, nullable = false)
    @Basic
    @Enumerated(EnumType.ORDINAL)
    private PollStatus status = PollStatus.CREATED;

    @Column(unique = false, updatable = true, insertable = true, nullable = true, length = 255)
    @Basic(optional = false)
    private String title;

    @Column(unique = false, updatable = true, insertable = true, nullable = true, length = 255)
    @Basic(optional = false)
    private String question;

    @Column(name = "poll_start", unique = false, updatable = true, insertable = true, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Basic(optional = false)
    @NotNull
    private Date start;

    @Column(name = "poll_end", unique = false, updatable = true, insertable = true, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Basic(optional = false)
    @NotNull
    private Date end;

    @OneToMany(cascade = {CascadeType.ALL}, targetEntity = Alternative.class, mappedBy = "poll",
        fetch = EAGER )
    private List<Alternative> alternatives;

    /**
     * No argument constructor
     */
    public Poll() {
    }

    /**
     * Full constructor
     *
     * @param id poll identifier
     * @param title poll title
     * @param question poll question
     * @param start poll start date and time
     * @param end poll end date and time
     * @param alternatives {@link java.util.List} of Alternative instances for the poll
     */
    public Poll(Long id, String title, String question, Date start, Date end, List<Alternative> alternatives) {
        this.id = id;
        this.title = title;
        this.question = question;
        this.start = start;
        this.end = end;
        this.alternatives = alternatives;
    }

    /**
     * Required attributes only constructor
     *
     * @param title poll title
     * @param question poll question
     * @param start poll start date and time
     * @param end poll end date and time
      */
    public Poll(String title, String question, Date start, Date end) {
        this.title = title;
        this.question = question;
        this.start = start;
        this.end = end;
        this.alternatives = new ArrayList<>();
    }


    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Date getStart() {
        return this.start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public List<Alternative> getAlternatives() {
        return this.alternatives;
    }

    public void setAlternatives(List<Alternative> alternatives) {
        this.alternatives = alternatives;
    }

    public Date getEnd() {
        return this.end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PollStatus getStatus() {
        return this.status;
    }

    public void setStatus(PollStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.id);
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
        final Poll other = (Poll) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Poll{" + "id=" + id + ", status=" + status + ", title=" + title 
                + ", question=" + question + ", start=" + start + ", end=" + end 
                + ", alternatives=" + alternatives + '}';
    }

}
