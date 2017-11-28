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
    public static final int EXPIRATION_INTERVAL_MILLIS = INTERVAL * 1200;


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
        if (defaultedEvent != PeerEvent.stopped) {
            expires = new Date(System.currentTimeMillis() + EXPIRATION_INTERVAL_MILLIS);
        } else {
            expires = new Date(System.currentTimeMillis());
        }
        peer.setExpires(expires);
        peer.setComplete(defaultedEvent.equals(PeerEvent.completed));

        peerDao.persistEntity(peer);

        return peerDao.getPeers(peer, 74);
    }
}
