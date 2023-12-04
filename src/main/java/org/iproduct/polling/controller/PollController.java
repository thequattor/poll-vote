package org.iproduct.polling.controller;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.iproduct.polling.entity.Alternative;
import org.iproduct.polling.entity.Poll;
import org.iproduct.polling.jpacontroller.exceptions.IllegalOrphanException;
import org.iproduct.polling.jpacontroller.exceptions.NonexistentEntityException;
import org.iproduct.polling.jpacontroller.exceptions.RollbackFailureException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class PollController implements Serializable {
    
    // injected EntityManager property
    @PersistenceContext(unitName = "PollingPU")
    private EntityManager em;
    
//    @Resource
//    private UserTransaction utx;

    public PollController() {
    }
    
//    public PollController(UserTransaction utx, EntityManagerFactory emf) {
//        this.utx = utx;
//        this.emf = emf;
//    }
//    private UserTransaction utx = null;
//    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return em;
//        return emf.createEntityManager();
    }

    public void create(Poll poll) throws RollbackFailureException, Exception {
        poll.setId(null);
        if (poll.getAlternatives() == null) {
            poll.setAlternatives(new ArrayList<Alternative>());
        }
//        EntityManager em = null;
        try {
//            utx.begin();
//            em = getEntityManager();
            List<Alternative> attachedAlternatives = new ArrayList<Alternative>();
            for (Alternative alternativesAlternativeToAttach : poll.getAlternatives()) {
                alternativesAlternativeToAttach = em.getReference(alternativesAlternativeToAttach.getClass(), alternativesAlternativeToAttach.getId());
                attachedAlternatives.add(alternativesAlternativeToAttach);
            }
            poll.setAlternatives(attachedAlternatives);
            em.persist(poll);
            for (Alternative alternativesAlternative : poll.getAlternatives()) {
                Poll oldPollOfAlternativesAlternative = alternativesAlternative.getPoll();
                alternativesAlternative.setPoll(poll);
                alternativesAlternative = em.merge(alternativesAlternative);
                if (oldPollOfAlternativesAlternative != null) {
                    oldPollOfAlternativesAlternative.getAlternatives().remove(alternativesAlternative);
                    oldPollOfAlternativesAlternative = em.merge(oldPollOfAlternativesAlternative);
                }
            }
        } catch (ConstraintViolationException ex) {
            ex.getConstraintViolations().stream().forEach(
                (ConstraintViolation cv) -> System.out.println(cv));
            throw ex;
        }
//            utx.commit();
//        } catch (Exception ex) {
//            try {
//                utx.rollback();
//            } catch (Exception re) {
//                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
//            }
//            throw ex;
//        } finally {
//            if (em != null) {
//                em.close();
//            }
//        }
    }

    public void edit(Poll poll) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
//        EntityManager em = null;
//        try {
//            utx.begin();
            em = getEntityManager();
            Poll persistentPoll = em.find(Poll.class, poll.getId());
            if(persistentPoll == null)
                throw new NonexistentEntityException("The poll with id " 
                        + poll.getId()+ " does not exist.");
            List<Alternative> alternativesOld = persistentPoll.getAlternatives();
            List<Alternative> alternativesNew = poll.getAlternatives();
            if (alternativesNew == null)
                alternativesNew = new ArrayList<>();
            List<String> illegalOrphanMessages = null;
            for (Alternative alternativesOldAlternative : alternativesOld) {
                if (!alternativesNew.contains(alternativesOldAlternative)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Alternative " + alternativesOldAlternative + " since its poll field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Alternative> attachedAlternativesNew = new ArrayList<Alternative>();
            for (Alternative alternativesNewAlternativeToAttach : alternativesNew) {
                alternativesNewAlternativeToAttach = em.getReference(alternativesNewAlternativeToAttach.getClass(), alternativesNewAlternativeToAttach.getId());
                attachedAlternativesNew.add(alternativesNewAlternativeToAttach);
            }
            alternativesNew = attachedAlternativesNew;
            poll.setAlternatives(alternativesNew);
            poll = em.merge(poll);
            for (Alternative alternativesNewAlternative : alternativesNew) {
                if (!alternativesOld.contains(alternativesNewAlternative)) {
                    Poll oldPollOfAlternativesNewAlternative = alternativesNewAlternative.getPoll();
                    alternativesNewAlternative.setPoll(poll);
                    alternativesNewAlternative = em.merge(alternativesNewAlternative);
                    if (oldPollOfAlternativesNewAlternative != null && !oldPollOfAlternativesNewAlternative.equals(poll)) {
                        oldPollOfAlternativesNewAlternative.getAlternatives().remove(alternativesNewAlternative);
                        oldPollOfAlternativesNewAlternative = em.merge(oldPollOfAlternativesNewAlternative);
                    }
                }
            }
//            utx.commit();
//        } catch (Exception ex) {
//            try {
//                utx.rollback();
//            } catch (Exception re) {
//                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
//            }
//            String msg = ex.getLocalizedMessage();
//            if (msg == null || msg.length() == 0) {
//                Long id = poll.getId();
//                if (findPoll(id) == null) {
//                    throw new NonexistentEntityException("The poll with id " + id + " no longer exists.");
//                }
//            }
//            throw ex;
//        } finally {
//            if (em != null) {
//                em.close();
//            }
//        }
    }

    public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
//        EntityManager em = null;
//        try {
//            utx.begin();
//            em = getEntityManager();
            Poll poll;
            poll = em.find(Poll.class, id);
            if(poll == null) 
                throw new NonexistentEntityException("The poll with id " 
                        + id + " does not exist.");
//            List<String> illegalOrphanMessages = null;
//            List<Alternative> alternativesOrphanCheck = poll.getAlternatives();
//            for (Alternative alternativesOrphanCheckAlternative : alternativesOrphanCheck) {
//                if (illegalOrphanMessages == null) {
//                    illegalOrphanMessages = new ArrayList<String>();
//                }
//                illegalOrphanMessages.add("This Poll (" + poll + ") cannot be destroyed since the Alternative " + alternativesOrphanCheckAlternative + " in its alternatives field has a non-nullable poll field.");
//            }
//            if (illegalOrphanMessages != null) {
//                throw new IllegalOrphanException(illegalOrphanMessages);
//            }
            em.remove(poll);
//            utx.commit();
//        } catch (Exception ex) {
//            try {
//                utx.rollback();
//            } catch (Exception re) {
//                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
//            }
//            throw ex;
//        } finally {
//            if (em != null) {
//                em.close();
//            }
//        }
    }

    public List<Poll> findPollEntities() {
        return findPollEntities(true, -1, -1);
    }

    public List<Poll> findPollEntities(int maxResults, int firstResult) {
        return findPollEntities(false, maxResults, firstResult);
    }

    private List<Poll> findPollEntities(boolean all, int maxResults, int firstResult) {
//        EntityManager em = getEntityManager();
//        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Poll.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
//        } finally {
//            em.close();
//        }
    }

    public Poll findPoll(Long id) {
//        EntityManager em = getEntityManager();
//        try {
            return em.find(Poll.class, id);
//        } finally {
//            em.close();
//        }
    }

    public int getPollCount() {
//        EntityManager em = getEntityManager();
//        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Poll> rt = cq.from(Poll.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
//        } finally {
//            em.close();
//        }
    }

}
