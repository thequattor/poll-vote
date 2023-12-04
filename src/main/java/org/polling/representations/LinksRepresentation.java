package org.polling.representations;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "_links")
@XmlAccessorType(XmlAccessType.FIELD)
public class LinksRepresentation {
    @XmlElement(name="link")
    List<LinkRepresentation> links = new ArrayList<>();

    public LinksRepresentation() {
    }

    public LinksRepresentation(List<LinkRepresentation> links) {
        this.links = links;
    }

    public List<LinkRepresentation> getLinks() {
        return links;
    }

    public void setLinks(List<LinkRepresentation> links) {
        this.links = links;
    }

    public LinksRepresentation addLink(LinkRepresentation aLink) {
        links.add(aLink);
        return this;
    }
    
    @Override
    public String toString() {
        return "Links" + links;
    }
    
}
