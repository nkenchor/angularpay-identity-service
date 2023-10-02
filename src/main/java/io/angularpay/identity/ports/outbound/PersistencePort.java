package io.angularpay.identity.ports.outbound;

import io.angularpay.identity.domain.UserIdentity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PersistencePort {
    UserIdentity createUser(UserIdentity request);
    UserIdentity updateUser(UserIdentity request);
    Optional<UserIdentity> findUserByUserReference(String userReference);
    Optional<UserIdentity> findUserByUsername(String username);
    Optional<UserIdentity> findUserByDeviceId(String deviceId);
    Optional<UserIdentity> findUserByUsernameAndPassword(String username, String password);
    Page<UserIdentity> listUsers(Pageable pageable);
}
