package org.polling.representations;

import jakarta.ws.rs.core.Link;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.net.URI;

//@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="_link")
public class LinkRepresentation {
    public static final String REL = "rel";
    public static final String TITLE = "title";
    public static final String TYPE = "type";
    
//    @XmlAttribute()
    private URI href;//=URI.create("#");
//    @XmlAttribute
    private String rel;
//    @XmlAttribute
    private String title;
//    @XmlAttribute
    private String type;

    LinkRepresentation(){
    }

    public LinkRepresentation(Link link) {
        this.href = link.getUri();
        this.rel = link.getRel();
        this.title = link.getTitle();
        this.type = link.getType();
    }
    public LinkRepresentation(URI target, String rel) {
        this.href = target;
        this.rel = rel;
    }

    public LinkRepresentation(URI target, String rel, String title) {
        this.href = target;
        this.rel = rel;
        this.title = title;
    }

    public LinkRepresentation(URI target, String rel, String title, String type) {
        this.href = target;
        this.rel = rel;
        this.title = title;
        this.type = type;
    }

    public URI getHref() {
        return href;
    }

    public void setHref(URI target) {
        this.href = target;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.href != null ? this.href.hashCode() : 0);
        hash = 97 * hash + (this.rel != null ? this.rel.hashCode() : 0);
        hash = 97 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
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
        final LinkRepresentation other = (LinkRepresentation) obj;
        if (this.href != other.href && (this.href == null || !this.href.equals(other.href))) {
            return false;
        }
        if ((this.rel == null) ? (other.rel != null) : !this.rel.equals(other.rel)) {
            return false;
        }
        if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        return true;
    }

  

    @Override
    public String toString() {
        return "Link{" + "target=" + href  + ", rel=" + rel + ", title=" + title 
                + ", type=" + type + '}';
    }
    
}
