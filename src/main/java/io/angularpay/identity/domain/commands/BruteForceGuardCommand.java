package io.angularpay.identity.domain.commands;

public interface BruteForceGuardCommand {

    void onLoginSuccess(String clientIp);
    void onLoginFailure(String clientIp);
    boolean isBlocked(String clientIp);
}
