package org.iproduct.polling.representations;

import jakarta.ws.rs.core.Link;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.iproduct.polling.entity.Alternative;
import org.iproduct.polling.entity.Poll;
import org.iproduct.polling.entity.PollStatus;

import java.util.*;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static org.iproduct.polling.resources.utils.LinkRelations.SELF;

@XmlRootElement(name="poll")
@XmlAccessorType(XmlAccessType.FIELD)
public class PollRepresentationCustomLinks {
    private Long id;
    private PollStatus status = PollStatus.CREATED;
    private String title;
    private String question;
    private Date start;
    private Date end;
    @XmlElementWrapper(name = "_embedded")
    @XmlElement(name = "alternative")
    private List<AlternativeRepresentation> alternatives;
    @XmlElement(name="_links")
    @XmlJavaTypeAdapter(LinkMapAdapter.class)
    private Map<String, LinkRepresentation> links = new LinkedHashMap<>();

    public PollRepresentationCustomLinks() {
    }

    public PollRepresentationCustomLinks(Poll poll, Collection<Link> links) {
        id = poll.getId();
        status = poll.getStatus();
        title = poll.getTitle();
        question = poll.getQuestion();
        start = poll.getStart();
        end = poll.getEnd(); 
        if(poll.getAlternatives() != null && poll.getAlternatives().size() > 0){
            alternatives  = new ArrayList<>();
            Link pollSelf;
            if(links != null && links.size() > 0) {
                List<Link> selfLinks = links.stream().filter((Link l) ->  l.getRel().equals(SELF))
                    .collect(Collectors.toList());
                 pollSelf = selfLinks.get(0);
            } else { 
                pollSelf = null;
            }
               
            poll.getAlternatives().stream().forEachOrdered((Alternative alt) -> {
                List<Link> altSelfs =  null;
                if(pollSelf != null){
                    altSelfs = new ArrayList<>();
                    altSelfs.add(Link.fromUriBuilder(pollSelf.getUriBuilder()
                            .path("alternatives").path(Long.toString(alt.getId()))) 
                        .rel(SELF).type(APPLICATION_XML).type(APPLICATION_JSON)
                        .title("Self link").build() );
                }
                alternatives.add(new AlternativeRepresentation(alt, altSelfs));
            });
        }
        if(links != null){
            links.stream().forEachOrdered((Link link) -> {
                this.links.put(link.getRel(), new LinkRepresentation(link));
            });
        }
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

    public List<AlternativeRepresentation> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<AlternativeRepresentation> alternatives) {
        this.alternatives = alternatives;
    }

    public Map<String, LinkRepresentation> getLinks() {
        return links;
    }

    public void setLinks(Map<String, LinkRepresentation> links) {
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
        final PollRepresentationCustomLinks other = (PollRepresentationCustomLinks) obj;
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
