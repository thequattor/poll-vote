package org.polling.controller;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;
import org.polling.entity.Alternative;
import org.polling.entity.Poll;
import org.polling.entity.Vote;
import org.polling.jpacontroller.exceptions.IllegalOrphanException;
import org.polling.jpacontroller.exceptions.NonexistentEntityException;
import org.polling.jpacontroller.exceptions.RollbackFailureException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class AlternativeController implements Serializable {

    // injected EntityManager property
    @PersistenceContext(unitName = "PollingPU")
    private EntityManager em;

    public AlternativeController() {
    }

    public void create(Long pollId, Alternative alternative)
            throws NonexistentEntityException, RollbackFailureException, Exception {
        alternative.setId(null);
        if (alternative.getVotes() == null) {
            alternative.setVotes(new ArrayList<Vote>());
        }

        Poll poll = em.find(Poll.class, pollId);
        if (poll == null) {
            throw new NonexistentEntityException("Poll with Id = "
                    + pollId + " does not exist");
        }
        alternative.setPoll(poll);
        
        List<Vote> attachedVotes = new ArrayList<Vote>();
        for (Vote votesVoteToAttach : alternative.getVotes()) {
            votesVoteToAttach = em.getReference(votesVoteToAttach.getClass(), votesVoteToAttach.getId());
            attachedVotes.add(votesVoteToAttach);
        }
        alternative.setVotes(attachedVotes);
        em.persist(alternative);
        poll.getAlternatives().add(alternative);
        poll = em.merge(poll);
        for (Vote votesVote : alternative.getVotes()) {
            Alternative oldAlternativeOfVotesVote = votesVote.getAlternative();
            votesVote.setAlternative(alternative);
            votesVote = em.merge(votesVote);
            if (oldAlternativeOfVotesVote != null) {
                oldAlternativeOfVotesVote.getVotes().remove(votesVote);
                oldAlternativeOfVotesVote = em.merge(oldAlternativeOfVotesVote);
            }
        }
    }

    public void edit(Long pollId, Alternative alternative) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        if (alternative.getVotes() == null) {
            alternative.setVotes(new ArrayList<Vote>());
        }
        Poll poll = em.find(Poll.class, pollId);
        if (poll == null) {
            throw new NonexistentEntityException("Poll with Id = "
                    + pollId + " does not exist");
        }
        alternative.setPoll(poll);
        
        Alternative persistentAlternative = em.find(Alternative.class, alternative.getId());
        Poll pollOld = persistentAlternative.getPoll();
        Poll pollNew = alternative.getPoll();
        List<Vote> votesOld = persistentAlternative.getVotes();
        List<Vote> votesNew = alternative.getVotes();
        List<String> illegalOrphanMessages = null;
        for (Vote votesOldVote : votesOld) {
            if (!votesNew.contains(votesOldVote)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Vote " + votesOldVote + " since its alternative field is not nullable.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        if (pollNew != null) {
            pollNew = em.getReference(pollNew.getClass(), pollNew.getId());
            alternative.setPoll(pollNew);
        }
        List<Vote> attachedVotesNew = new ArrayList<Vote>();
        for (Vote votesNewVoteToAttach : votesNew) {
            votesNewVoteToAttach = em.getReference(votesNewVoteToAttach.getClass(), votesNewVoteToAttach.getId());
            attachedVotesNew.add(votesNewVoteToAttach);
        }
        votesNew = attachedVotesNew;
        alternative.setVotes(votesNew);
        alternative = em.merge(alternative);
        if (pollOld != null && !pollOld.equals(pollNew)) {
            pollOld.getAlternatives().remove(alternative);
            pollOld = em.merge(pollOld);
        }
        if (pollNew != null && !pollNew.equals(pollOld)) {
            pollNew.getAlternatives().add(alternative);
            pollNew = em.merge(pollNew);
        }
        for (Vote votesNewVote : votesNew) {
            if (!votesOld.contains(votesNewVote)) {
                Alternative oldAlternativeOfVotesNewVote = votesNewVote.getAlternative();
                votesNewVote.setAlternative(alternative);
                votesNewVote = em.merge(votesNewVote);
                if (oldAlternativeOfVotesNewVote != null && !oldAlternativeOfVotesNewVote.equals(alternative)) {
                    oldAlternativeOfVotesNewVote.getVotes().remove(votesNewVote);
                    oldAlternativeOfVotesNewVote = em.merge(oldAlternativeOfVotesNewVote);
                }
            }
        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        Alternative alternative;
        try {
            alternative = em.getReference(Alternative.class, id);
            alternative.getId();
        } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The alternative with id " + id + " no longer exists.", enfe);
        }
//        List<String> illegalOrphanMessages = null;
//        List<Vote> votesOrphanCheck = alternative.getVotes();
//        for (Vote votesOrphanCheckVote : votesOrphanCheck) {
//            if (illegalOrphanMessages == null) {
//                illegalOrphanMessages = new ArrayList<String>();
//            }
//            illegalOrphanMessages.add("This Alternative (" + alternative + ") cannot be destroyed since the Vote " + votesOrphanCheckVote + " in its votes field has a non-nullable alternative field.");
//        }
//        if (illegalOrphanMessages != null) {
//            throw new IllegalOrphanException(illegalOrphanMessages);
//        }
        Poll poll = alternative.getPoll();
        if (poll != null) {
            poll.getAlternatives().remove(alternative);
            poll = em.merge(poll);
        }
        em.remove(alternative);
    }

    public List<Alternative> findAlternativeEntitiesByPollId(Long pollId) {
        return findAlternativeEntitiesByPollId(pollId, true, -1, -1);
    }

    public List<Alternative> findAlternativeEntitiesByPollId(Long pollId, int maxResults, int firstResult) {
        return findAlternativeEntitiesByPollId(pollId, false, maxResults, firstResult);
    }

    private List<Alternative> findAlternativeEntitiesByPollId(Long pollId, boolean all, int maxResults, int firstResult) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Alternative> q = cb.createQuery(Alternative.class);
        Root<Alternative> alt = q.from(Alternative.class);
        ParameterExpression<Long> pid = cb.parameter(Long.class);
      // FIX THIS  q.select(alt).where(cb.equal(alt.get(Alternative_.poll).get(Poll_.id), pid));

        TypedQuery<Alternative> query = em.createQuery(q);
        query.setParameter(pid, pollId);

        if (!all) {
            query.setMaxResults(maxResults);
            query.setFirstResult(firstResult);
        }
        List<Alternative> results = query.getResultList();
        System.out.println(">>>>>>>>>>>>>>>>>>>> Alternatives: " + results);
        return results;
    }

    public Alternative findAlternative(Long id) {
        return em.find(Alternative.class, id);
    }

    public int getAlternativeCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Alternative> rt = cq.from(Alternative.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}
