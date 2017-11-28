package br.com.trofo.seeder;

import br.com.trofo.seeder.dao.PeerDao;
import br.com.trofo.seeder.dao.PeerRepository;
import br.com.trofo.seeder.entity.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@Service
public class PeerService {

    public static final int INTERVAL = 3600;

    @Autowired
    private PeerRepository peerRepository;

    @Autowired
    private PeerDao peerDao;

    public Collection<Peer> registerPeer(String infohash, String ip, int port, Optional<PeerEvent> event) {
        PeerEvent defaultedEvent = event.orElse(PeerEvent.started);
        Peer peer = new Peer();
        peer.setInfoHash(infohash);
        peer.setIp(ip);
        peer.setPort(port);
        Date expires;
        if (!defaultedEvent.equals(PeerEvent.stopped)) {
            expires = new Date(new Double(System.currentTimeMillis() + (INTERVAL * 1000.0 * 1.2)).intValue());
        } else {
            expires = new Date();
        }
        peer.setExpires(expires);
        peer.setComplete(defaultedEvent.equals(PeerEvent.completed));

        peerDao.persistEntity(peer);

        return peerDao.getPeers(peer, 74);
    }
}
