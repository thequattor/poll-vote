package org.polling.representations;

import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.polling.entity.Poll;
import org.polling.resources.utils.LinkRelations;

import java.util.*;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="polls")
public class PollsRepresentation {
    private int size = 0;
    @XmlElementWrapper(name = "_embedded")
    @XmlElement(name = "poll")
    private Collection<PollRepresentationCustomLinks> polls;
    @XmlElement(name="_links")
    @XmlJavaTypeAdapter(LinkMapAdapter.class)
    private Map<String, LinkRepresentation> links = new LinkedHashMap<>();

    public PollsRepresentation() {
    }

    public PollsRepresentation(Collection<Poll> pollsArg, Collection<Link> linksArg) {
        this.size = pollsArg.size();
        if(linksArg != null){
            linksArg.stream().forEachOrdered((Link link) -> {
                links.put(link.getRel(), new LinkRepresentation(link));
            });
        }
        Link selfLink = links.get(LinkRelations.SELF) != null ?
            Link.fromUri(links.get(LinkRelations.SELF).getHref())
                .rel(LinkRelations.SELF).title("Polls collection").type(APPLICATION_JSON)
                .build() : null;

        this.polls = (pollsArg.size() > 0)? 
            pollsArg.stream().map( (Poll poll) -> {
                List<Link> l = null;
                if(selfLink != null){
                    l =  new ArrayList<>();
                    l.add(Link.fromUriBuilder(
                        UriBuilder.fromLink(selfLink).path(Long.toString(poll.getId())))
                        .rel(LinkRelations.SELF).type(APPLICATION_JSON).build());
                }
                return new PollRepresentationCustomLinks(poll, l);
            }).collect(Collectors.toList())
            : null;
    } 

    public int getSize() {
        return size;
    }

    public Collection<PollRepresentationCustomLinks> getPolls() {
        return polls;
    }

    public void setPolls(Collection<PollRepresentationCustomLinks> polls) {
        this.polls = polls;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.polls);
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
        final PollsRepresentation other = (PollsRepresentation) obj;
        if (!Objects.equals(this.polls, other.polls)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PollsRepresentation{" + "size=" + size + ", polls=" + polls + '}';
    }
}
