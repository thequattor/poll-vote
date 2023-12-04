package org.iproduct.polling.resources;

import jakarta.ejb.EJBException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.iproduct.polling.controller.AlternativeController;
import org.iproduct.polling.controller.PollController;
import org.iproduct.polling.controller.VoteController;
import org.iproduct.polling.entity.Alternative;
import org.iproduct.polling.jpacontroller.exceptions.IllegalOrphanException;
import org.iproduct.polling.jpacontroller.exceptions.NonexistentEntityException;
import org.iproduct.polling.jpacontroller.exceptions.RollbackFailureException;
import org.iproduct.polling.representations.AlternativeRepresentation;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.*;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.iproduct.polling.resources.utils.LinkRelations.*;

@Path("/alternatives")
public class AlternativesResource {
    private Long pollId;
    private UriInfo uriInfo;
    private PollController pollController;
    private AlternativeController alternativeController;
    private VoteController voteController;

    /**
     * Public no argument constructor 
     */
    public AlternativesResource() {}

    /**
     * Public constructor with parent Poll resource identifier as argument
     * @param poll the parent Poll resource
     * @param pollController the parent {@link org.iproduct.polling.controller.PollController}
     * @param uriInfo the UriInfo instance from parent resource
     */
    AlternativesResource(Long pollId, PollController pollController, 
            AlternativeController alternativeController, 
            VoteController voteController,
            UriInfo uriInfo) {
        this.pollId = pollId;
        this.pollController = pollController;
        this.alternativeController = alternativeController;
        this.voteController = voteController;
        this.uriInfo = uriInfo;
        
//        altrnativeController = new AlternativeController(pollController);
    }
     
    /**
     * Get all available polls as a collection
     *
     * @return Collection of Alternative JAXB XML/JSON representations
     */
    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Collection<Alternative> getAllAlternatives() {
        System.out.println(">>>>>>>>>>>>>>>>>>> poll = " + pollId);
        return alternativeController.findAlternativeEntitiesByPollId(pollId);
    }

    /**
     * Get range of available polls with identifiers statring from
     * {@code fromId}, and with maximal size of {@code numberItems}
     *
     * @param firstResult the sequential number of the first poll to be returned
     * @param maxResults the maximal number of polls to be returned
     * @return Collection of Alternative JAXB XML/JSON representations
     */
    @GET
    @Path("/range")
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Collection<Alternative> getAlternativesRange(
            @QueryParam("first") int firstResult, @QueryParam("max") int maxResults) {
        return alternativeController
            .findAlternativeEntitiesByPollId(pollId, maxResults, firstResult);
    }

    /**
     * Get count of all available polls
     *
     * @return polls total count as plain text string
     */
    @GET
    @Path("/count")
    @Produces(TEXT_PLAIN)
    public String getAlternativesCount() {
        return Integer.toString(alternativeController.getAlternativeCount());
    }

    /**
     * Receive particular resource with given identifier or status code 404
     * NOT_FOUND if the resource does not exist.
     *
     * @param id the poll identifier
     * @return Alternative JAXB XML/JSON representation
     */
    @GET
    @Path("/{id}")
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response getAlternativeById(@PathParam("id") Long id) {
        Alternative alt = alternativeController.findAlternative(id);
        if (alt == null) {
            throw new WebApplicationException("Alternative with Id = "
                    + id + " does not exist", NOT_FOUND);
        }
        List<Alternative> alternatives = alternativeController.findAlternativeEntitiesByPollId(pollId);
        int i = alternatives.indexOf(alt);
 
        Link prevLink = (i > 0) ?  Link.fromUriBuilder(
                uriInfo.getBaseUriBuilder().path(PollsResource.class)
                    .path(Long.toString(pollId)).path("alternatives")
                    .path(alternatives.get(i - 1).getId().toString()) )
                .rel(PREV)
                .type(APPLICATION_XML)
                .type(APPLICATION_JSON)
                .title("Previous poll").build() : null;

        Link nextLink = (i < alternatives.size() - 1) ? Link.fromUriBuilder(
                uriInfo.getBaseUriBuilder().path(PollsResource.class)
                    .path(Long.toString(pollId)).path("alternatives")
                    .path(alternatives.get(i + 1).getId().toString()) )
                .rel(NEXT)
                .type(APPLICATION_XML)
                .type(APPLICATION_JSON)
                .title("Next poll").build() : null;
                
        Link collectionLink = Link.fromUriBuilder(
                    uriInfo.getBaseUriBuilder().path(PollsResource.class)
                        .path(Long.toString(pollId)).path("alternatives") )
                .rel(COLLECTION)
                .type(APPLICATION_XML)
                .type(APPLICATION_JSON)
                .title("Polls collection").build();
                
        Link upLink =  Link.fromUriBuilder(
                    uriInfo.getBaseUriBuilder().path(PollsResource.class))
                .rel(UP)
                .type(APPLICATION_XML)
                .type(APPLICATION_JSON)
                .title("Root resource").build();

        Link selfLink =   Link.fromUriBuilder(
            uriInfo.getBaseUriBuilder().path(PollsResource.class)
                .path(Long.toString(pollId)).path("alternatives")
                .path(Long.toString(alt.getId())) )
                .rel(SELF).type(APPLICATION_XML).type(APPLICATION_JSON)
                .title("Self link").build();
                
        List<Link> links = Arrays.asList(prevLink, nextLink, collectionLink,
                upLink, selfLink).stream()
                .filter(a -> a != null)
                .collect(Collectors.toList());
        System.out.println(">>>>>>>>>>LINKS:" + links);
     
        Response.ResponseBuilder resp;
     // 1. Structural Links in entity body - at the moment works in XML only
//        resp = Response.ok(new PollRepresentationDocumentLinks(poll, links));

     // 2. State transition links as HTTP Link headers - work independently
     // of the MIME type of the resource
//        resp = Response.ok(new PollRepresentationHeaderLinks(poll))
//                .links(links.toArray(new Link[]{}));

     // 3. Custom Link serialization (JAXB mapping) - allows maximum flexibility
        resp = Response.ok(new AlternativeRepresentation(alt, links));
        
        return resp.build();
    }
    /**
     * Receive return list of alternatives for particular resource with given 
     * identifier or status code 404 NOT_FOUND if the resource does not exist.
     *
     * @param id the poll identifier
     * @return Poll JAXB XML/JSON representation
     */
    @Path("{id}/votes")
    public VotesResource getPollAlternativesByPollId(@PathParam("id") Long altId) {
        Alternative alternative = alternativeController.findAlternative(altId);
        if (alternative == null) {
            throw new WebApplicationException("Alternative with Id = " 
                    + altId + " does not exist", NOT_FOUND);
        }
        return new VotesResource(pollId, alternative.getId(),
                pollController, alternativeController, voteController, uriInfo);
    }

    /**
     * Create new resource with identifier automatically assigned by polls
     * container resource using 'Container-Item' RESTful resource pattern The
     * status code returned by this method is 201 CREATED with
     * <a href="https://tools.ietf.org/html/rfc5988">RFC 5988 Link header</a>,
     * and entity body containing representation of newly created resource with
     * auto assigned identifier
     * <p>
     * The method is not idempotent (see {@link #updateAlternative(Long, Alternative)} for a
     * discussion)</p>
     *
     * @param alt the new alternative to be added
     * @return HHTP response with entity body containing Alternative JAXB XML/JSON
     * representation
     */
    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response addAlternative(Alternative alt) {
        try {
            alternativeController.create(pollId, alt);
        } catch (EJBException e) {
            handleEJBException(e);
        } catch (NonexistentEntityException ex) {
            throw new WebApplicationException(ex.getMessage(), NOT_FOUND);
        } catch (Exception e){
            Logger.getLogger(AlternativesResource.class.getName())
                .log(Level.SEVERE, "Alternative Resource throws exception:", e);
            throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
        }
        Response response = Response.created(
            uriInfo.getAbsolutePathBuilder()
                .path(Long.toString(alt.getId())).build() )
            .entity(alt)
            .build();
        return response;
    }

    /**
     * Modifies existing resource identified by last path segment in the
     * resource uri, which should be the same as the poll identifier in the
     * resource representation.
     * <ul>
     * <li>If the poll identifier and uri segment do not match an error response
     * is returned with status code 400 BAD_REQUEST.</li>
     * <li>If there is no resource with given identifier on the server, then
     * response 404 NOT_FOUND is returned. </li>
     * <li>If the resource on the server has been concurrently modified by
     * another client (which is detected by comparing the previous ETag =
     * hashcode of the resource being updated with the currently computed for
     * the resource one on the server) then error response 409 CONFLICT is
     * returned</li>
     * </ul>
     * All above mentioned error responses are accompanied by entity body
     * containing plain text message describing the problem.
     *
     * <p>
     * If the update has been successful 204 NO_CONTENT status code is returned
     * with empty body (an opportunity described in
     * <a href"http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">RFC 2616
     * Section 9</a>)</p>
     * <p>
     * The method is idempotent - meaning that it can be safely repeated
     * multiple times by the client with no negative side effects (in contrast
     * with POST method, which is not idempotent)
     *
     * @param id poll identifier
     * @param alt poll entity to be updated
     * @return HTTP Response
     */
    @PUT
    @Path("/{id}")
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updateAlternative(@PathParam("id") Long id, Alternative alt) {
        Response response = Response.noContent().build();//More appropriate than 200OK
        if (id.equals(alt.getId())) {
            try {
                alternativeController.edit(pollId, alt); //TODO Etag comaprison
            } catch (EJBException e) {
                    handleEJBException(e);
            } catch (ConcurrentModificationException e) {
                throw new WebApplicationException(e.getMessage(), CONFLICT);
            } catch (NonexistentEntityException e) {
                throw new WebApplicationException(e.getMessage(), NOT_FOUND);
            } catch(IllegalOrphanException | RollbackFailureException e){
                throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
            } catch (Exception e){
                Logger.getLogger(AlternativesResource.class.getName())
                    .log(Level.SEVERE, "Alternative Resource throws exception:", e);
                throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
            }
        } else {
            throw new WebApplicationException("Resource identifier " + id
                    + " can not be changed to " + alt.getId() 
                    + ". Resource identifiers are immutable.", BAD_REQUEST);
        }
        return response;
    }

    /**
     * Deletes existing resource identified by last path segment in the resource
     * uri
     * <ul>
     * <li>If there is no resource with given identifier on the server, then
     * response 404 NOT_FOUND is returned. </li>
     * </ul>
     * The above mentioned error response is accompanied by entity body
     * containing plain text message describing the problem.
     *
     * <p>
     * If the DELETE has been successful 200 OK status code is returned with
     * entity body containing the deleted entity representation (see
     * <a href"http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">RFC 2616
     * Section 9</a> for more information)</p>
     * <p>
     * The method is not idempotent (see {@link #updateAlternative(Long, Alternative)} for a
     * discussion)</p>
     *
     * @param id the poll identifier
     * @return HTTP Response
     */
    @DELETE
    @Path("/{id}")
    public Alternative deleteAlternative(@PathParam("id") Long id) {
        Alternative deletedItem = null;
        try {
            deletedItem = alternativeController.findAlternative(id);
            alternativeController.destroy(id);
        } catch (EJBException e) {
            handleEJBException(e);
        } catch (NonexistentEntityException e) {
            throw new WebApplicationException(e.getMessage(), NOT_FOUND);
        } catch(IllegalOrphanException | RollbackFailureException e){
            throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
        } catch (Exception e){
            Logger.getLogger(AlternativesResource.class.getName())
                .log(Level.SEVERE, "Alternative Resource throws exception:", e);
            throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
        }
        return(deletedItem);
    }
    
    private void handleEJBException(EJBException e) throws WebApplicationException {
        Throwable ex = e.getCausedByException();
        while(!(ex instanceof ConstraintViolationException)&&
                ex.getCause() != null ){
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>" + ex);
            ex = ex.getCause();
        }
        
        if(!(ex instanceof ConstraintViolationException)){
            Logger.getLogger(AlternativesResource.class.getName())
                    .log(Level.SEVERE, "Alternative Resource throws exception:", e);
            throw new WebApplicationException(ex.getMessage(), BAD_REQUEST);
        }
        ConstraintViolationException cve = (ConstraintViolationException) ex;
        StringBuilder sb =
                new StringBuilder("Data constrains have been violeted:\n");
        cve.getConstraintViolations().stream().forEach(
                (ConstraintViolation<?> cv) -> {
                    sb.append(cv.getRootBean()).append(": ")
                            .append(cv.getPropertyPath()).append(" --> ")
                            .append(cv.getInvalidValue()).append(" : ")
                            .append(cv.getMessage()).append("\n");
                });
        System.out.println(">>>>>>>>>>>>>>>>>>>>>> " + sb.toString());
        throw new WebApplicationException(sb.toString(), BAD_REQUEST);
    }


}
