package org.polling.resources;

import jakarta.ejb.EJBException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.polling.controller.AlternativeController;
import org.polling.controller.PollController;
import org.polling.controller.VoteController;
import org.polling.entity.Vote;
import org.polling.jpacontroller.exceptions.IllegalOrphanException;
import org.polling.jpacontroller.exceptions.NonexistentEntityException;
import org.polling.jpacontroller.exceptions.RollbackFailureException;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jakarta.ws.rs.core.MediaType.*;
import static jakarta.ws.rs.core.Response.Status.*;

@Path("/votes")
public class VotesResource {
    private Long pollId;
    private Long alternativeId;
    private UriInfo uriInfo;
    private PollController pollController;
    private AlternativeController altrnativeController;
    private VoteController voteController;

    /**
     * Public no argument constructor 
     */
    public VotesResource() {}

    VotesResource(Long pollId, Long alternativeId, PollController pollController, 
            AlternativeController alternativeController, 
            VoteController voteController,
            UriInfo uriInfo) {
        this.pollId = pollId;
        this.alternativeId = alternativeId;
        this.pollController = pollController;
        this.altrnativeController = alternativeController;
        this.voteController = voteController;
        this.uriInfo = uriInfo;       
    }
     
    /**
     * Get all available polls as a collection
     *
     * @return Collection of Vote JAXB XML/JSON representations
     */
    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Collection<Vote> getAllVotes() {
        System.out.println(">>>>>>>>>>>>>>>>>>> poll = " + pollId);
        return voteController.findVoteEntitiesByPollAltIds(pollId, alternativeId);
    }

    /**
     * Get range of available polls with identifiers statring from
     * {@code fromId}, and with maximal size of {@code numberItems}
     *
     * @param firstResult the sequential number of the first poll to be returned
     * @param maxResults the maximal number of polls to be returned
     * @return Collection of Vote JAXB XML/JSON representations
     */
    @GET
    @Path("/range")
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Collection<Vote> getVotesRange(
            @QueryParam("first") int firstResult, @QueryParam("max") int maxResults) {
        return voteController
            .findVoteEntitiesByPollAltIds(pollId, alternativeId, maxResults, firstResult);
    }

    /**
     * Get count of all available polls
     *
     * @return polls total count as plain text string
     */
    @GET
    @Path("/count")
    @Produces(TEXT_PLAIN)
    public String getVotesCount() {
        return Integer.toString(voteController.getVoteCount());
    }

    /**
     * Receive particular resource with given identifier or status code 404
     * NOT_FOUND if the resource does not exist.
     *
     * @param id the poll identifier
     * @return Vote JAXB XML/JSON representation
     */
    @GET
    @Path("/{id}")
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Vote getVoteById(@PathParam("id") Long id) {
        Vote poll = voteController.findVote(id);
        if (poll == null) {
            throw new WebApplicationException("Vote with Id = " 
                    + id + " does not exist", NOT_FOUND);
        }
//			throw new NotFoundException("Entity with resourceId = " + id + " does not exist");
        return poll;
    }

    /**
     * Create new resource with identifier automatically assigned by polls
     * container resource using 'Container-Item' RESTful resource pattern The
     * status code returned by this method is 201 CREATED with
     * <a href="https://tools.ietf.org/html/rfc5988">RFC 5988 Link header</a>,
     * and entity body containing representation of newly created resource with
     * auto assigned identifier
     * <p>
     * The method is not idempotent (see {@link #updateVote(Long, Vote)} for a
     * discussion)</p>
     *
     * @param vote the new alternative to be added
     * @return HHTP response with entity body containing Vote JAXB XML/JSON
     * representation
     */
    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response addVote(Vote vote) {
        try {
            voteController.create(alternativeId, vote);
        } catch (EJBException e) {
            handleEJBException(e);
        } catch (NonexistentEntityException ex) {
            throw new WebApplicationException(ex.getMessage(), NOT_FOUND);
        } catch (Exception e){
            Logger.getLogger(VotesResource.class.getName())
                .log(Level.SEVERE, "Vote Resource throws exception:", e);
            throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
        }
        Response response = Response.created(uriInfo.getAbsolutePathBuilder()
                .path(Long.toString(vote.getId())).build() )
            .entity(vote)
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
    public Response updateVote(@PathParam("id") Long id, Vote alt) {
        Response response = Response.noContent().build();//More appropriate than 200OK
        if (id.equals(alt.getId())) {
            try {
                voteController.edit(alternativeId, alt); //TODO Etag comaprison
            } catch (EJBException e) {
                    handleEJBException(e);
            } catch (ConcurrentModificationException e) {
                throw new WebApplicationException(e.getMessage(), CONFLICT);
            } catch (NonexistentEntityException e) {
                throw new WebApplicationException(e.getMessage(), NOT_FOUND);
            } catch(IllegalOrphanException | RollbackFailureException e){
                throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
            } catch (Exception e){
                Logger.getLogger(VotesResource.class.getName())
                    .log(Level.SEVERE, "Vote Resource throws exception:", e);
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
     * The method is not idempotent (see {@link #updateVote(Long, Vote)} for a
     * discussion)</p>
     *
     * @param id the poll identifier
     * @return HTTP Response
     */
    @DELETE
    @Path("/{id}")
    public Vote deleteVote(@PathParam("id") Long id) {
        Vote deletedItem = null;
        try {
            System.out.println("Try to delete vote with id:" + id);
            deletedItem = voteController.findVote(id);
            System.out.println("Vote found:" + deletedItem);
            voteController.destroy(id);
        } catch (EJBException e) {
            handleEJBException(e);
        } catch (NonexistentEntityException e) {
            throw new WebApplicationException(e.getMessage(), NOT_FOUND);
        } catch(IllegalOrphanException | RollbackFailureException e){
            throw new WebApplicationException(e.getMessage(), BAD_REQUEST);
        } catch (Exception e){
            Logger.getLogger(VotesResource.class.getName())
                .log(Level.SEVERE, "Vote Resource throws exception:", e);
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
            Logger.getLogger(VotesResource.class.getName())
                    .log(Level.SEVERE, "Vote Resource throws exception:", e);
            throw new WebApplicationException(ex.getMessage(), BAD_REQUEST);
        }
        ConstraintViolationException cve = (ConstraintViolationException) ex;
        StringBuilder sb =
                new StringBuilder("Data constrains have been violeted:\n");
        cve.getConstraintViolations().stream().forEach(
                (ConstraintViolation cv) -> {
                    sb.append(cv.getRootBean()).append(": ")
                            .append(cv.getPropertyPath()).append(" --> ")
                            .append(cv.getInvalidValue()).append(" : ")
                            .append(cv.getMessage()).append("\n");
                });
        System.out.println(">>>>>>>>>>>>>>>>>>>>>> " + sb.toString());
        throw new WebApplicationException(sb.toString(), BAD_REQUEST);
    }


}
