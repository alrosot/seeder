package br.com.trofo.seeder;

import br.com.trofo.seeder.dao.PeerDao;
import br.com.trofo.seeder.dao.PeerRepository;
import br.com.trofo.seeder.entity.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PeerService {

    @Autowired
    private PeerRepository peerRepository;

    @Autowired
    private PeerDao peerDao;

    public Peer registerPeer(String infohash, String ip, int port) {
        Peer peer = new Peer();
        peer.setInfoHash(infohash);
        peer.setIp(ip);
        peer.setPort(port);
        peer.setExpires(new Date()); //TODO calculate

        peerDao.persistEntity(peer);

        return peerRepository.save(peer);
    }
}
