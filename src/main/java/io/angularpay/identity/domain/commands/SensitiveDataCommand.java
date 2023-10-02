package io.angularpay.identity.domain.commands;

public interface SensitiveDataCommand<T> {
    T mask(T raw);
}
