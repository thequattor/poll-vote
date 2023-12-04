package org.iproduct.polling.representations;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkMapAdapter extends XmlAdapter<LinksRepresentation, Map<String, LinkRepresentation>> {

    @Override
    public Map<String, LinkRepresentation> unmarshal(LinksRepresentation v) throws Exception {
        Map map = new HashMap();
        v.getLinks().stream().forEachOrdered(link -> map.put(link.getRel(), link));
        return map;
    }

    @Override
    public LinksRepresentation marshal(Map<String, LinkRepresentation> map) throws Exception {
        List<LinkRepresentation> list = new ArrayList<>();
        map.values().stream().forEachOrdered(v -> list.add(v) );
        LinksRepresentation links = new LinksRepresentation(list); 
        return links;
    }
}
