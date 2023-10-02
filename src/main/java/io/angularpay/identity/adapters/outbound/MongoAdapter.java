package io.angularpay.identity.adapters.outbound;

import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.ports.outbound.PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoAdapter implements PersistencePort {

    private final UserIdentityRepository userIdentityRepository;

    @Override
    public UserIdentity createUser(UserIdentity request) {
        request.setCreatedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return userIdentityRepository.save(request);
    }

    @Override
    public UserIdentity updateUser(UserIdentity request) {
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return userIdentityRepository.save(request);
    }

    @Override
    public Optional<UserIdentity> findUserByUserReference(String userReference) {
        return userIdentityRepository.findByUserReference(userReference);
    }

    @Override
    public Optional<UserIdentity> findUserByUsername(String username) {
        return userIdentityRepository.findByUsername(username);
    }

    @Override
    public Optional<UserIdentity> findUserByDeviceId(String deviceId) {
        return userIdentityRepository.findByDevicesContains(deviceId);
    }

    @Override
    public Optional<UserIdentity> findUserByUsernameAndPassword(String username, String password) {
        return userIdentityRepository.findByUsernameAndPassword(username, password);
    }

    @Override
    public Page<UserIdentity> listUsers(Pageable pageable) {
        return userIdentityRepository.findAll(pageable);
    }

}
