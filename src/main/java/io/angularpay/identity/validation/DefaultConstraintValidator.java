package io.angularpay.identity.validation;

import io.angularpay.identity.exceptions.ErrorCode;
import io.angularpay.identity.exceptions.ErrorObject;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.angularpay.identity.common.Constants.ERROR_SOURCE;

@Service
public class DefaultConstraintValidator {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    public List<ErrorObject> validate(Object request) {
        if (Objects.isNull(request)) {
            return Collections.singletonList(ErrorObject.builder()
                    .message("must not be NULL")
                    .code(ErrorCode.VALIDATION_ERROR)
                    .source(ERROR_SOURCE)
                    .build());
        } else {
            Set<ConstraintViolation<Object>> violations = validator.validate(request);
            if (CollectionUtils.isEmpty(violations)) {
                return Collections.emptyList();
            } else {
                return violations.stream().map(x -> ErrorObject.builder()
                        .message(x.getPropertyPath().toString() + " - " + x.getMessage())
                        .code(ErrorCode.VALIDATION_ERROR)
                        .source(ERROR_SOURCE)
                        .build())
                        .collect(Collectors.toList());
            }
        }
    }
}
