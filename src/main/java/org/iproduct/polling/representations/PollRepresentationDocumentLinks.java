package org.iproduct.polling.representations;

import jakarta.ws.rs.core.Link;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.iproduct.polling.entity.Alternative;
import org.iproduct.polling.entity.Poll;
import org.iproduct.polling.entity.PollStatus;

import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="poll")
public class PollRepresentationDocumentLinks {
    private Long id;
    private PollStatus status = PollStatus.CREATED;
    private String title;
    private String question;
    private Date start;
    private Date end;
    @XmlElementWrapper(name = "_embedded")
    @XmlElement(name = "alternative")
    private List<Alternative> alternatives;
    @XmlElementWrapper(name = "_links")
    @XmlElement(name = "link")
    @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
    private Collection<Link> links;

    public PollRepresentationDocumentLinks() {
        links = new ArrayList<Link>();
    }

    public PollRepresentationDocumentLinks(Poll poll, Collection<Link> links) {
        id = poll.getId();
        status = poll.getStatus();
        title = poll.getTitle();
        question = poll.getQuestion();
        start = poll.getStart();
        end = poll.getEnd();
        alternatives = (poll.getAlternatives().size() > 0)?
                poll.getAlternatives() : null;
        this.links = links != null ? links : new ArrayList<Link>();
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

    public Collection<Link> getLinks() {
        return links;
    }

    public void setLinks(Collection<Link> links) {
        this.links = links;
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
        final PollRepresentationDocumentLinks other = (PollRepresentationDocumentLinks) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PolllRepresentation{" + "id=" + id + ", status=" + status + ", title=" + title + ", question=" + question + ", start=" + start + ", end=" + end + ", alternatives=" + alternatives + ", links=" + links + '}';
    }

    
}
