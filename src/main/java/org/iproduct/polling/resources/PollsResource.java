/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2014 IPT - Intellectual Products & Technologies.
 * All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 with Classpath Exception only ("GPL"). 
 * You may use this file only in compliance with GPL. You can find a copy 
 * of GPL in the root directory of this project in the file named LICENSE.txt.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the GPL file named LICENSE.txt in the root directory of 
 * the project.
 *
 * GPL Classpath Exception:
 * IPT - Intellectual Products & Technologies (IPT) designates this particular 
 * file as subject to the "Classpath" exception as provided by IPT in the GPL 
 * Version 2 License file that accompanies this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 */
package org.iproduct.polling.resources;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static jakarta.ws.rs.core.MediaType.*;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.iproduct.polling.controller.AlternativeController;

import org.iproduct.polling.controller.PollController;
import org.iproduct.polling.controller.VoteController;
import org.iproduct.polling.entity.Poll;
import org.iproduct.polling.filters.Logged;
import org.iproduct.polling.filters.Profiled;
import org.iproduct.polling.jpacontroller.exceptions.IllegalOrphanException;
import org.iproduct.polling.jpacontroller.exceptions.NonexistentEntityException;
import org.iproduct.polling.jpacontroller.exceptions.RollbackFailureException;
import org.iproduct.polling.representations.PollRepresentationCustomLinks;
import org.iproduct.polling.representations.PollsRepresentation;
import static org.iproduct.polling.resources.utils.LinkRelations.COLLECTION;
import static org.iproduct.polling.resources.utils.LinkRelations.NEXT;
import static org.iproduct.polling.resources.utils.LinkRelations.PREV;
import static org.iproduct.polling.resources.utils.LinkRelations.SELF;
import static org.iproduct.polling.resources.utils.LinkRelations.UP;

@Path("/polls")
@Stateless
public class PollsResource {
    
//    @javax.enterprise.inject.Produces
//    Poll poll;
    private static boolean serverBusy = false;

    @Inject
    private PollController pollController;
    @Inject 
    private AlternativeController alternativeController;
    @Inject 
    private VoteController voteController;
   
    // injected UserTransaction
//    @Resource
//    private UserTransaction utx;
//
    @Context
    private UriInfo uriInfo;
    
    Link selfLink = Link.fromPath("http://localhost:8080/polling/resources/polls")
            .rel(SELF).build();
    
    @GET
    @Produces(value = {APPLICATION_XML + "; qs=0.75", APPLICATION_JSON + "; qs=0.9"})
    @Asynchronous
    public void getAllPolls(@Suspended final AsyncResponse asyncResponse) {
        asyncResponse.setTimeoutHandler( 
            (AsyncResponse ar) ->  {
                if(isBusy()) //ar.resume( new WebApplicationException(
//                    "Server is too busy. Try again later.",
//                    SERVICE_UNAVAILABLE)); 
                    ar.cancel(300);
                else ar.setTimeout(10, SECONDS);
            });
        asyncResponse.setTimeout(10, TimeUnit.SECONDS);
        asyncResponse.resume(doGetAllPolls(selfLink));
    }

    private static boolean isBusy() {
        return serverBusy;
    }
    private PollsRepresentation doGetAllPolls(Link selfLink) {
        return new PollsRepresentation(pollController.findPollEntities(), 
                Arrays.asList(selfLink));
    }

    /**
     * Get range of available polls with identifiers statring from
     * {@code fromId}, and with maximal size of {@code numberItems}
     *
     * @param firstResult the sequential number of the first poll to be returned
     * @param maxResults the maximal number of polls to be returned
     * @return Collection of Poll JAXB XML/JSON representations
     */
    @GET
    @Path("/range")
    @Produces({APPLICATION_XML + "; qs=0.75", APPLICATION_JSON + "; qs=0.9"})
    public Collection<Poll> getPollsRange(
            @QueryParam("first") int firstResult, @QueryParam("max") Integer maxResults) {
        return pollController.findPollEntities(maxResults, firstResult);
    }

    /**
     * Get count of all available polls
     *
     * @return polls total count as plain text string
     */
    @GET
    @Path("/count")
    @Produces(TEXT_PLAIN)
    @Pattern(regexp = "\\d+")
    public String getPollsCount() {
        return Integer.toString(pollController.getPollCount());
    }

    /**
     * Receive particular resource with given identifier or status code 404
     * NOT_FOUND if the resource does not exist.
     *
     * @param id the poll identifier
     * @return Poll JAXB XML/JSON representation
     */
    @GET 
    @Logged
    @Profiled
    @Path("/{id}")
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response getPollById(
        @PathParam("id") @NotNull @Min(1)
            Long id) {
        Poll poll = pollController.findPoll(id);
        if (poll == null) {
            throw new WebApplicationException("Poll with Id = "
                    + id + " does not exist", NOT_FOUND);
        }
        List<Poll> polls = pollController.findPollEntities();
        int i = polls.indexOf(poll);
 
        Link prevLink = (i > 0) ?  Link.fromUriBuilder(
                uriInfo.getBaseUriBuilder().path(PollsResource.class)
                    .path(polls.get(i - 1).getId().toString()) )
                .rel(PREV)
                .type(APPLICATION_XML)
                .type(APPLICATION_JSON)
                .title("Previous poll").build() : null;

        Link nextLink = (i < polls.size() - 1) ? Link.fromUriBuilder(
                uriInfo.getBaseUriBuilder().path(PollsResource.class)
                    .path(polls.get(i + 1).getId().toString()) )
                .rel(NEXT)
                .type(APPLICATION_XML)
                .type(APPLICATION_JSON)
                .title("Next poll").build() : null;
                
        Link collectionLink = Link.fromUriBuilder(
                    uriInfo.getBaseUriBuilder().path(PollsResource.class) ) 
                .rel(COLLECTION)
                .type(APPLICATION_XML)
                .type(APPLICATION_JSON)
                .title("Polls collection").build();
                
        Link upLink = Link.fromLink(collectionLink)
                .rel(UP)
                .type(APPLICATION_XML)
                .type(APPLICATION_JSON)
                .title("Root resource").build();

        Link selfLink = Link.fromUriBuilder(uriInfo.getAbsolutePathBuilder()) 
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
        resp = Response.ok(new PollRepresentationCustomLinks(poll, links));
        
        return resp.build();
    }

    /**
     * Receive return list of alternatives for particular resource with given 
     * identifier or status code 404 NOT_FOUND if the resource does not exist.
     *
     * @param id the poll identifier
     * @return Poll JAXB XML/JSON representation
     */
    @Path("/{id}/alternatives")
    public AlternativesResource getPollAlternativesByPollId(@PathParam("id") Long id) {
        Poll poll = pollController.findPoll(id);
        if (poll == null) {
            throw new WebApplicationException("Poll with Id = " 
                    + id + " does not exist", NOT_FOUND);
        }
        return new AlternativesResource(poll.getId(), 
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
     * The method is not idempotent (see {@link #updatePoll(Long, Poll)} for a
     * discussion)</p>
     *
     * @param poll the new poll to be added
     * @return HHTP response with entity body containing Poll JAXB XML/JSON
     * representation
     */
    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response addPoll(Poll poll) {
        try {
            pollController.create(poll);
        } catch (Exception ex) {
            Logger.getLogger(PollsResource.class.getName()).log(Level.SEVERE, null, ex);
            throw new WebApplicationException(ex, BAD_REQUEST);
        }
        Response response = Response.created(
                uriInfo.getAbsolutePathBuilder()
                .path(Long.toString(poll.getId())).build() )
                .entity(poll)
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
     * @param poll poll entity to be updated
     * @return HTTP Response
     */
    @PUT
    @Path("/{id}")
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response updatePoll(@PathParam("id") Long id, Poll poll) {
        Response response;
        if (id.equals(poll.getId())) {
            try {
                pollController.edit(poll); //TODO Etag comaprison
                response = Response.noContent().build(); //More appropriate than 200OK
            } catch (ConcurrentModificationException e) {
                response = Response.status(Response.Status.CONFLICT)
                    .type(TEXT_PLAIN).entity(e.getMessage()).build();
            } catch (NonexistentEntityException e) {
                response = Response.status(NOT_FOUND)
                    .type(TEXT_PLAIN).entity(e.getMessage()).build();
            } catch(IllegalOrphanException | RollbackFailureException e){
                response = Response.status(BAD_REQUEST)
                    .type(TEXT_PLAIN).entity(e.getMessage()).build();
            } catch (Exception e){
                Logger.getLogger(PollsResource.class.getName())
                    .log(Level.SEVERE, null, e);
                throw new WebApplicationException(e, BAD_REQUEST);
            }
        } else {
            response = Response.status(BAD_REQUEST)
                    .type(TEXT_PLAIN).entity("Resource identifier " + id
                    + " can not be changed to " + poll.getId() 
                    + ". Resource identifiers are immutable.").build();
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
     * The method is not idempotent (see {@link #updatePoll(Long, Poll)} for a
     * discussion)</p>
     *
     * @param id the poll identifier
     * @return HTTP Response
     */
    @DELETE
    @Path("/{id}")
    public Response deletePoll(@PathParam("id") Long id) {
        Poll deletedItem;
        Response response;
        try {
            deletedItem = pollController.findPoll(id);
            pollController.destroy(id);
            response = Response.ok().entity(deletedItem).build();
            } catch (NonexistentEntityException e) {
                response = Response.status(NOT_FOUND)
                    .type(TEXT_PLAIN).entity(e.getMessage()).build();
            } catch(IllegalOrphanException | RollbackFailureException e){
                response = Response.status(BAD_REQUEST)
                    .type(TEXT_PLAIN).entity(e.getMessage()).build();
            } catch (Exception e){
                Logger.getLogger(PollsResource.class.getName())
                    .log(Level.SEVERE, null, e);
                throw new WebApplicationException(e, BAD_REQUEST);
            }
        return response;
    }

}
