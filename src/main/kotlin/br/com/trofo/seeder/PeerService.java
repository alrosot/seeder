package br.com.trofo.seeder;

import br.com.trofo.seeder.dao.PeerDao;
import br.com.trofo.seeder.dao.PeerRepository;
import br.com.trofo.seeder.entity.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;

@Service
public class PeerService {

    @Autowired
    private PeerRepository peerRepository;

    @Autowired
    private PeerDao peerDao;

    public Collection<Peer> registerPeer(String infohash, String ip, int port) {
        Peer peer = new Peer();
        peer.setInfoHash(infohash);
        peer.setIp(ip);
        peer.setPort(port);
        peer.setExpires(new Date(new Double(System.currentTimeMillis() + (3600.0 * 1000.0 * 1.2)).intValue())); //TODO calculate

        peerDao.persistEntity(peer);

        return peerDao.getPeers(peer, 74);
    }
}
