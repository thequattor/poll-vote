package org.polling.representations;

import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.polling.entity.Alternative;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.polling.resources.utils.LinkRelations.SELF;
import static org.polling.resources.utils.LinkRelations.VOTE;

@XmlRootElement(name="alternative")
@XmlAccessorType(XmlAccessType.FIELD)
public class AlternativeRepresentation {
    private Long id;
    private String text;
    private int position;
    private int votes = 0;
    @XmlElement(name="_links")
    @XmlJavaTypeAdapter(LinkMapAdapter.class)
    private Map<String, LinkRepresentation> links = new LinkedHashMap<>();

    public AlternativeRepresentation() {
    }

    public AlternativeRepresentation(Alternative alt, Collection<Link> linksArg) {
        id = alt.getId();
        text = alt.getText();
        position = alt.getPosition();
        votes = alt.getVotes().size();
        if(linksArg != null){
            linksArg.stream().forEachOrdered((Link link) -> {
                links.put(link.getRel(), new LinkRepresentation(link));
            });
        }
        if(links.get(SELF) != null)
            links.put(VOTE, new LinkRepresentation(
                Link.fromUriBuilder(
                    UriBuilder.fromUri(links.get(SELF).getHref()).path("votes"))
                        .rel(VOTE).title("Vote for alternative").type(APPLICATION_JSON)
                        .build()) );
    } 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public Map<String, LinkRepresentation> getLinks() {
        return links;
    }

    public void setLinks(Map<String, LinkRepresentation> links) {
        this.links = links;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final AlternativeRepresentation other = (AlternativeRepresentation) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AlternativeRepresentation{" + "id=" + id + ", text=" + text 
                + ", position=" + position + ", votes=" + votes
                + ", links=" + links + '}';
    }

    
}
