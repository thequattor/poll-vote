package org.polling.controller;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;
import org.polling.entity.Alternative;
import org.polling.entity.Vote;
import org.polling.jpacontroller.exceptions.NonexistentEntityException;
import org.polling.jpacontroller.exceptions.RollbackFailureException;

import java.io.Serializable;
import java.util.List;

@Stateless
public class VoteController implements Serializable {

    // injected EntityManager property
    @PersistenceContext(unitName = "PollingPU")
    private EntityManager em;

    public VoteController() {
    }

    public void create(Long altId, Vote vote) throws RollbackFailureException, Exception {
        vote.setId(null);

        Alternative alternative = em.find(Alternative.class, altId);
        if (alternative == null) {
            throw new NonexistentEntityException("Alternative with Id = "
                    + altId + " does not exist");
        }
        vote.setAlternative(alternative);

        em.persist(vote);
        if (alternative != null) {
            alternative.getVotes().add(vote);
            alternative = em.merge(alternative);
        }
    }

    public void edit(Long altId, Vote vote) throws NonexistentEntityException, RollbackFailureException, Exception {
        Alternative alternative = em.find(Alternative.class, altId);
        if (alternative == null) {
            throw new NonexistentEntityException("Alternative with Id = "
                    + altId + " does not exist");
        }
        vote.setAlternative(alternative);
     
        Vote persistentVote = em.find(Vote.class, vote.getId());
        Alternative alternativeOld = persistentVote.getAlternative();
        Alternative alternativeNew = vote.getAlternative();
        if (alternativeNew != null) {
            alternativeNew = em.getReference(alternativeNew.getClass(), alternativeNew.getId());
            vote.setAlternative(alternativeNew);
        }
        vote = em.merge(vote);
        if (alternativeOld != null && !alternativeOld.equals(alternativeNew)) {
            alternativeOld.getVotes().remove(vote);
            alternativeOld = em.merge(alternativeOld);
        }
        if (alternativeNew != null && !alternativeNew.equals(alternativeOld)) {
            alternativeNew.getVotes().add(vote);
            alternativeNew = em.merge(alternativeNew);
        }
    }

    public void destroy(Long id) throws NonexistentEntityException, RollbackFailureException, Exception {
        Vote vote;
        try {
            vote = em.getReference(Vote.class, id);
            vote.getId();
        } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The vote with id " + id + " no longer exists.", enfe);
        }
        Alternative alternative = vote.getAlternative();
        if (alternative != null) {
            alternative.getVotes().remove(vote);
            alternative = em.merge(alternative);
        }
        em.remove(vote);
    }

    public List<Vote> findVoteEntitiesByPollAltIds(Long pollId, Long altId) {
        return findVoteEntitiesByPollAltIds(pollId, altId, true, -1, -1);
    }

    public List<Vote> findVoteEntitiesByPollAltIds(Long pollId, Long altId, int maxResults, int firstResult) {
        return findVoteEntitiesByPollAltIds(pollId, altId, false, maxResults, firstResult);
    }

    private List<Vote> findVoteEntitiesByPollAltIds(Long pollId, Long altId, boolean all, int maxResults, int firstResult) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Vote> q = cb.createQuery(Vote.class);
        Root<Vote> vote = q.from(Vote.class);
        ParameterExpression<Long> pid = cb.parameter(Long.class);
        ParameterExpression<Long> aid = cb.parameter(Long.class);
        /*q.select(vote).where(cb.and(
            cb.equal(vote.get(Vote_.alternative).get(Alternative_.poll).get(Poll_.id), pid),
            cb.equal(vote.get(Vote_.alternative).get(Alternative_.id), aid)
        ));*/

        TypedQuery<Vote> query = em.createQuery(q);
        query.setParameter(pid, pollId).setParameter(aid, altId);

        if (!all) {
            query.setMaxResults(maxResults);
            query.setFirstResult(firstResult);
        }
        List<Vote> results = query.getResultList();
        System.out.println(">>>>>>>>>>>>>>>>>>>> Votes: " + results);
        return results;
   }

    public Vote findVote(Long id) {
        return em.find(Vote.class, id);
    }

    public int getVoteCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Vote> rt = cq.from(Vote.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}
