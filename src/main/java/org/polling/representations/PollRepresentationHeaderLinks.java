package org.polling.representations;

import jakarta.xml.bind.annotation.*;
import org.polling.entity.Alternative;
import org.polling.entity.Poll;
import org.polling.entity.PollStatus;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="poll")
public class PollRepresentationHeaderLinks {
    private Long id;
    private PollStatus status = PollStatus.CREATED;
    private String title;
    private String question;
    private Date start;
    private Date end;
    @XmlElementWrapper(name = "_embedded")
    @XmlElement(name = "alternative")
    private List<Alternative> alternatives;

    public PollRepresentationHeaderLinks() {
    }

    public PollRepresentationHeaderLinks(Poll poll) {
        id = poll.getId();
        status = poll.getStatus();
        title = poll.getTitle();
        question = poll.getQuestion();
        start = poll.getStart();
        end = poll.getEnd();
        alternatives = (poll.getAlternatives().size() > 0)?
                poll.getAlternatives() : null;
    } 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PollStatus getStatus() {
        return status;
    }

    public void setStatus(PollStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public List<Alternative> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<Alternative> alternatives) {
        this.alternatives = alternatives;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final PollRepresentationHeaderLinks other = (PollRepresentationHeaderLinks) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PolllRepresentation{" + "id=" + id + ", status=" + status 
                + ", title=" + title + ", question=" + question + ", start=" 
                + start + ", end=" + end + ", alternatives=" + alternatives + '}';
    }

    
}
