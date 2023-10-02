package io.angularpay.identity.adapters.outbound;

import io.angularpay.identity.domain.UserIdentity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserIdentityRepository extends MongoRepository<UserIdentity, String> {

    Optional<UserIdentity> findByUserReference(String reference);
    Optional<UserIdentity> findByUsername(String username);
    Optional<UserIdentity> findByDevicesContains(String deviceId);
    Optional<UserIdentity> findByUsernameAndPassword(String username, String password);
    Page<UserIdentity> findAll(Pageable pageable);
}
