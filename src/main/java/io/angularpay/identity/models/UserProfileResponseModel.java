
package io.angularpay.identity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.tomcat.jni.Address;

import java.util.List;

@Data
public class UserProfileResponseModel {

    private String handle;
    private String username;
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    private String email;
    private String firstname;
    private String lastname;
    private String phone;
    private List<Address> addresses;
}
