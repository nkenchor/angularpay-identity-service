package io.angularpay.identity.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.exceptions.ErrorObject;
import io.angularpay.identity.models.AccessControl;
import io.angularpay.identity.models.AuthenticatedUser;
import io.angularpay.identity.models.AuthenticationMode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.angularpay.identity.exceptions.ErrorCode.NO_PRINCIPAL;
import static io.angularpay.identity.exceptions.ErrorCode.REQUEST_NOT_FOUND;

@Slf4j
public class Helper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> String writeAsStringOrDefault(ObjectMapper mapper, T source) {
        try {
            return mapper.writeValueAsString(source);
        } catch (JsonProcessingException exception) {
            log.error("An error occurred while writing source parameter as string", exception);
            return "";
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization error");
        }
    }

    public static AuthenticatedUser fromHeaders(Map<String, String> headers) {
        String clientIp = "";
        String xForwardedFor = headers.get("x-forwarded-for");
        if (StringUtils.hasText(xForwardedFor)) {
            clientIp = xForwardedFor.split(",")[0].trim();
        }
        List<Role> roles = new ArrayList<>();
        String rolesHeader = headers.get("x-angularpay-user-roles");
        if (StringUtils.hasText(rolesHeader)) {
            String[] rolesArr = rolesHeader.split(",");
            roles = Arrays.stream(rolesArr).map(x -> {
                try {
                    return Role.valueOf(x.trim());
                } catch (Exception exception) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return AuthenticatedUser.builder()
                .username(headers.get("x-angularpay-username"))
                .userReference(headers.get("x-angularpay-user-reference"))
                .deviceId(headers.get("x-angularpay-device-id"))
                .correlationId(headers.get("x-angularpay-correlation-id"))
                .clientIp(clientIp)
                .roles(roles)
                .authenticationMode(headers.get("x-angularpay-authentication"))
                .build();
    }

    public static boolean hasPermittedRole(List<Role> requiredRoles, List<Role> authenticatedUserRoles) {
        if(CollectionUtils.isEmpty(requiredRoles)) return false;
        if(CollectionUtils.isEmpty(authenticatedUserRoles)) return false;
        return requiredRoles.stream().anyMatch(authenticatedUserRoles::contains);
    }

    public static AuthenticationMode parseAuthenticationMode(String authenticationType) {
        try {
            return AuthenticationMode.valueOf(authenticationType);
        } catch (Exception exception) {
            return AuthenticationMode.MOBILE_USER_LOGIN;
        }
    }

    public static HttpStatus resolveStatus(List<ErrorObject> errorObjects) {
        if (CollectionUtils.isEmpty(errorObjects)) {
            // ideally, this method shouldn't be called unless there are actual validation errors
            // this check should be performed by the caller
            // In this case the AbstractCommand already performs this check
            // Therefore, this block will never be executed
            return HttpStatus.OK;
        }
        return errorObjects.stream().anyMatch(x -> x.getCode() == NO_PRINCIPAL) ? HttpStatus.INTERNAL_SERVER_ERROR :
                errorObjects.stream().anyMatch(x -> x.getCode() == REQUEST_NOT_FOUND) ? HttpStatus.NOT_FOUND :
                        HttpStatus.BAD_REQUEST;
    }

    public static Map<String, String> getTokenIfExists(UserIdentity userIdentity) {
        Map<String, String> jtis = new HashMap<>();
        if (StringUtils.hasText(userIdentity.getLoginActivity().getLastAccessTokenJti())) {
            jtis.put(
                    userIdentity.getLoginActivity().getLastAccessTokenJti(),
                    userIdentity.getLoginActivity().getLastAccessTokenExpiresAt()
            );
        }
        if (StringUtils.hasText(userIdentity.getLoginActivity().getLastRefreshTokenJti())) {
            jtis.put(
                    userIdentity.getLoginActivity().getLastRefreshTokenJti(),
                    userIdentity.getLoginActivity().getLastRefreshTokenExpiresAt()
            );
        }
        return jtis;
    }

    public static void addToMappedDiagnosticContext(String name, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(name, value);
        }
    }

    public static void addToMappedDiagnosticContextOrRandomUUID(String name, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(name, value);
        } else {
            MDC.put(name, UUID.randomUUID().toString());
        }
    }

    public static String tryMaskEmail(String email) {
        return maskUsername(email);
    }

    public static String maskEmail(String email) {
        String[] split = email.split("@");
        return split[0].charAt(0) + "*****" + split[0].charAt(split[0].length() - 1) + "@" + split[1];
    }

    public static String maskUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return username;
        }
        String[] split = username.split("@");
        if (split.length > 1) {
            return split[0].charAt(0) + "*****" + split[0].charAt(split[0].length() - 1) + "@" + split[1];
        } else {
            return username.substring(0, 3) + "*****" + username.substring(username.length() - 3);
        }
    }

    public static String maskUserReference(String userReference) {
        if (!StringUtils.hasText(userReference)) {
            return userReference;
        }
        String[] split = userReference.split("@");
        if (split.length > 1) {
            return split[0].charAt(0) + "*****" + split[0].charAt(split[0].length() - 1) + "@" + split[1];
        } else {
            return userReference;
        }
    }

    public static <T extends AccessControl> String maskAuthenticatedUser(ObjectMapper mapper, T raw) {
        JsonNode node = mapper.convertValue(raw, JsonNode.class);
        JsonNode authenticatedUser = node.get("authenticatedUser");
        ((ObjectNode) authenticatedUser).put("username", maskUsername(raw.getAuthenticatedUser().getUsername()));
        ((ObjectNode) authenticatedUser).put("userReference", maskUserReference(raw.getAuthenticatedUser().getUserReference()));
        return node.toString();
    }


    public static String toTitleCase(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        StringBuilder titleCase = new StringBuilder(input.length());
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }
}
