package br.com.trofo.seeder.dao;

import br.com.trofo.seeder.entity.Peer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface PeerRepository extends JpaRepository<Peer, Long> {

    @Query("select distinct infoHash from Peer")
    Set<String> findDistinctInfoHashes();
}
