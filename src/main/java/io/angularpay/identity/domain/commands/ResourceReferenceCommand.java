package io.angularpay.identity.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}
