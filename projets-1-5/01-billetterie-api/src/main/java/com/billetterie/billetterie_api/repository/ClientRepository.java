package com.billetterie.billetterie_api.repository;

import com.billetterie.billetterie_api.entity.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux clients. COUCHE : Repository · NE FAIT PAS : de règle métier. */
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
