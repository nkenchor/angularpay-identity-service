
package io.angularpay.identity.models;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class AssociatedReferenceResponse extends GenericReferenceResponse {

    private final String identityReference;
    private final String userReference;
}
