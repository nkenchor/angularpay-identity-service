package io.angularpay.identity.adapters.inbound;

import io.angularpay.identity.configurations.AngularPayConfiguration;
import io.angularpay.identity.domain.Role;
import io.angularpay.identity.domain.UserIdentity;
import io.angularpay.identity.domain.commands.*;
import io.angularpay.identity.models.*;
import io.angularpay.identity.ports.inbound.IdentityManagementRestApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.angularpay.identity.helpers.Helper.fromHeaders;

@RestController
@RequestMapping("/identity/users")
@RequiredArgsConstructor
public class IdentityManagementRestApiAdapter implements IdentityManagementRestApiPort {

    private final AngularPayConfiguration configuration;

    private final CreateUserCommand createUserCommand;
    private final OnboardUserCommand onboardUserCommand;
    private final UpdateUserCommand updateUserCommand;
    private final ResetPasswordCommand resetPasswordCommand;
    private final ChangePasswordCommand changePasswordCommand;
    private final ForgotPasswordStartCommand forgotPasswordStartCommand;
    private final ForgotPasswordConfirmCommand forgotPasswordConfirmCommand;
    private final ForgotPasswordCompleteCommand forgotPasswordCompleteCommand;
    private final AddDeviceCommand addDeviceCommand;
    private final RemoveDeviceCommand removeDeviceCommand;
    private final UpdateRolesCommand updateRolesCommand;
    private final RemoveRoleCommand removeRoleCommand;
    private final EnableUserCommand enableUserCommand;
    private final DeleteUserCommand deleteUserCommand;
    private final GetUserByReferenceCommand getUserByReferenceCommand;
    private final GetUserByUsernameCommand getUserByUsernameCommand;
    private final GetUserListCommand getUserListCommand;
    private final GetAssignableRolesCommand getAssignableRolesCommand;

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse createUser(
            @RequestBody CreateUserApiModel request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        CreateUserCommandRequest createUserCommandRequest = CreateUserCommandRequest.builder()
                .createUserApiModel(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return createUserCommand.execute(createUserCommandRequest);
    }

    @PostMapping("/onboard")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse onboardUser(
            @RequestBody OnboardUserApiModel request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        OnboardUserCommandRequest createUserCommandRequest = OnboardUserCommandRequest.builder()
                .onboardUserApiModel(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return onboardUserCommand.execute(createUserCommandRequest);
    }

    @PutMapping("/{userReference}")
    @Override
    public void updateUser(
            @PathVariable String userReference,
            @RequestBody UpdateUserApiModel updateUserApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateUserCommandRequest updateUserCommandRequest = UpdateUserCommandRequest.builder()
                .userReference(userReference)
                .updateUserApiModel(updateUserApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updateUserCommand.execute(updateUserCommandRequest);
    }

    @PutMapping("/{userReference}/reset-password")
    @Override
    public void resetPassword(
            @PathVariable String userReference,
            @RequestBody ResetPasswordApiModel resetPasswordApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ResetPasswordCommandRequest resetPasswordCommandRequest = ResetPasswordCommandRequest.builder()
                .userReference(userReference)
                .resetPasswordApiModel(resetPasswordApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.resetPasswordCommand.execute(resetPasswordCommandRequest);
    }

    @PutMapping("/{userReference}/change-password")
    @Override
    public void changePassword(
            @PathVariable String userReference,
            @RequestBody ChangePasswordApiModel changePasswordApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ChangePasswordCommandRequest changePasswordCommandRequest = ChangePasswordCommandRequest.builder()
                .userReference(userReference)
                .changePasswordApiModel(changePasswordApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.changePasswordCommand.execute(changePasswordCommandRequest);
    }

    @PostMapping("/forgot-password/start")
    @ResponseBody
    @Override
    public GenericReferenceResponse forgotPasswordStart(
            @RequestBody ForgotPasswordStartApiModel forgotPasswordStartApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ForgotPasswordStartCommandRequest forgotPasswordStartCommandRequest = ForgotPasswordStartCommandRequest.builder()
                .forgotPasswordStartApiModel(forgotPasswordStartApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.forgotPasswordStartCommand.execute(forgotPasswordStartCommandRequest);
    }

    @PutMapping("/{userReference}/forgot-password/confirm")
    @Override
    public void forgotPasswordConfirm(
            @PathVariable String userReference,
            @RequestBody ForgotPasswordCofrimApiModel forgotPasswordCofrimApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ForgotPasswordConfirmCommandRequest forgotPasswordConfirmCommandRequest = ForgotPasswordConfirmCommandRequest.builder()
                .userReference(userReference)
                .forgotPasswordCofrimApiModel(forgotPasswordCofrimApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.forgotPasswordConfirmCommand.execute(forgotPasswordConfirmCommandRequest);
    }

    @PutMapping("/{userReference}/forgot-password/complete")
    @Override
    public void forgotPasswordComplete(
            @PathVariable String userReference,
            @RequestBody ForgotPasswordCompleteApiModel forgotPasswordCompleteApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ForgotPasswordCompleteCommandRequest forgotPasswordCompleteCommandRequest = ForgotPasswordCompleteCommandRequest.builder()
                .userReference(userReference)
                .forgotPasswordCompleteApiModel(forgotPasswordCompleteApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.forgotPasswordCompleteCommand.execute(forgotPasswordCompleteCommandRequest);
    }

    @PostMapping("/{userReference}/devices")
    @Override
    public void addDevice(
            @PathVariable String userReference,
            @RequestBody AddDeviceApiModel addDeviceApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        AddDeviceCommandRequest addDeviceCommandRequest = AddDeviceCommandRequest.builder()
                .userReference(userReference)
                .addDeviceApiModel(addDeviceApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.addDeviceCommand.execute(addDeviceCommandRequest);
    }

    @DeleteMapping("/{userReference}/devices/{deviceId}")
    @Override
    public void removeDevice(
            @PathVariable String userReference,
            @PathVariable String deviceId,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RemoveDeviceCommandRequest removeDeviceCommandRequest = RemoveDeviceCommandRequest.builder()
                .userReference(userReference)
                .deviceId(deviceId)
                .authenticatedUser(authenticatedUser)
                .build();
        this.removeDeviceCommand.execute(removeDeviceCommandRequest);
    }

    @PutMapping("/{userReference}/roles")
    @Override
    public void updateRoles(
            @PathVariable String userReference,
            @RequestBody List<Role> roles,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateRolesCommandRequest updateRolesCommandRequest = UpdateRolesCommandRequest.builder()
                .userReference(userReference)
                .roles(roles)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updateRolesCommand.execute(updateRolesCommandRequest);
    }

    @DeleteMapping("/{userReference}/roles/{role}")
    @Override
    public void removeRole(
            @PathVariable String userReference,
            @PathVariable Role role,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RemoveRoleCommandRequest removeRoleCommandRequest = RemoveRoleCommandRequest.builder()
                .userReference(userReference)
                .role(role)
                .authenticatedUser(authenticatedUser)
                .build();
        this.removeRoleCommand.execute(removeRoleCommandRequest);
    }

    @PutMapping("/{userReference}/enable/{enable}")
    @Override
    public void enableUser(
            @PathVariable String userReference,
            @PathVariable boolean enable,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        EnableUserCommandRequest enableUserCommandRequest = EnableUserCommandRequest.builder()
                .userReference(userReference)
                .enable(enable)
                .authenticatedUser(authenticatedUser)
                .build();
        this.enableUserCommand.execute(enableUserCommandRequest);
    }

    @DeleteMapping("/{userReference}")
    @Override
    public void deleteUser(
            @PathVariable String userReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        DeleteUserCommandRequest deleteUserCommandRequest = DeleteUserCommandRequest.builder()
                .userReference(userReference)
                .authenticatedUser(authenticatedUser)
                .build();
        this.deleteUserCommand.execute(deleteUserCommandRequest);
    }

    @GetMapping("/{userReference}")
    @ResponseBody
    @Override
    public UserIdentity getUserByReference(
            @PathVariable String userReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetRequestByReferenceCommandRequest getRequestByReferenceCommandRequest = GetRequestByReferenceCommandRequest.builder()
                .userReference(userReference)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.getUserByReferenceCommand.execute(getRequestByReferenceCommandRequest);
    }

    @GetMapping("/username/{username}")
    @ResponseBody
    @Override
    public UserIdentity getUserByUsername(
            @PathVariable String username,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetByUsernameCommandRequest getByUsernameCommandRequest = GetByUsernameCommandRequest.builder()
                .username(username)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.getUserByUsernameCommand.execute(getByUsernameCommandRequest);
    }

    @GetMapping("/list/page/{page}")
    @ResponseBody
    @Override
    public List<UserIdentity> getUserList(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetSignupRequestListCommandRequest getSignupRequestListCommandRequest = GetSignupRequestListCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return this.getUserListCommand.execute(getSignupRequestListCommandRequest);
    }

    @GetMapping("/roles")
    @ResponseBody
    @Override
    public List<GetDefinedRolesApiResponse> getDefinedRoles(@RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetDefinedRolesCommandRequest getDefinedRolesCommandRequest = GetDefinedRolesCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .build();
        return this.getAssignableRolesCommand.execute(getDefinedRolesCommandRequest);
    }
}
