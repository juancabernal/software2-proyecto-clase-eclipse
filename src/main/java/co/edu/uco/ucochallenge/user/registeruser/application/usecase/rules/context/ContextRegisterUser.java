package co.edu.uco.ucochallenge.user.registeruser.application.usecase.rules.context;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContextRegisterUser {
    private UUID idType = UUIDHelper.getDefault();
    private String idNumber = TextHelper.getDefault();
    private String firstName = TextHelper.getDefault();
    private String secondName = TextHelper.getDefault();
    private String firstSurname = TextHelper.getDefault();
    private String secondSurname = TextHelper.getDefault();
    private UUID homeCity = UUIDHelper.getDefault();
    private String email = TextHelper.getDefault();
    private String mobileNumber = TextHelper.getDefault();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstSurname() {
        return firstSurname;
    }

    public void setFirstSurname(String firstSurname) {
        this.firstSurname = firstSurname;
    }

    public UUID getHomeCity() {
        return homeCity;
    }

    public void setHomeCity(UUID homeCity) {
        this.homeCity = homeCity;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public UUID getIdType() {
        return idType;
    }

    public void setIdType(UUID idType) {
        this.idType = idType;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getSecondSurname() {
        return secondSurname;
    }

    public void setSecondSurname(String secondSurname) {
        this.secondSurname = secondSurname;
    }
}
