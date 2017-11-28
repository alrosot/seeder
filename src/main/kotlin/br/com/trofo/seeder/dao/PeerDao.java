package br.com.trofo.seeder.dao;

import br.com.trofo.seeder.entity.Peer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Andoreh
 */
@Repository
@Transactional
public class PeerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void persistEntity(Peer peer) {
        Peer existingPeer = findPeer(peer.getInfoHash(),peer.getIp(),peer.getPort());
        if(existingPeer == null){
            entityManager.persist(peer);            
        } else {
            existingPeer.setExpires(peer.getExpires());           
            existingPeer.setComplete(peer.isComplete());
            entityManager.merge(existingPeer);
        }
    }

    public Collection<Peer> getPeers(Peer requestingPeer, int numWant) {
        Query query = entityManager.createNamedQuery("getPeers");
        query.setParameter("infoHash", requestingPeer.getInfoHash());
        query.setParameter("port", requestingPeer.getPort());
        query.setParameter("ip", requestingPeer.getIp());
        query.setParameter("expires", new Date());
        query.setMaxResults(numWant);
        return query.getResultList();
    }

    private Peer findPeer(String infoHash, String ip, Integer port) {
        Query query = entityManager.createNamedQuery("findPeer");
        query.setParameter("infoHash", infoHash);
        query.setParameter("port", port);
        query.setParameter("ip", ip);
        List resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return null;
        }
        return (Peer) resultList.get(0);
    }
}
