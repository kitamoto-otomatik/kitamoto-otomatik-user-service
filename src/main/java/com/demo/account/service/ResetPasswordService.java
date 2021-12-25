package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.Credential;
import com.demo.account.model.KeycloakResetPasswordRequest;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.ResetPasswordRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.demo.ErrorMessage.*;

@Slf4j
@Service
public class ResetPasswordService {
    @Value("${password.reset.code}")
    private String passwordResetCode;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    public void resetPassword(ResetPasswordRequest request) {
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(request.getUsername());
        keycloakUserList.removeIf(account -> !request.getUsername().equals(account.getUsername()));

        if (CollectionUtils.isEmpty(keycloakUserList)) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        } else if (keycloakUserList.size() != 1) {
            log.error("{} - {}", NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE, request.getUsername());
            throw new KeycloakException(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);
        } else if (!keycloakUserList.get(0).isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);
        } else if (MapUtils.isEmpty(keycloakUserList.get(0).getAttributes()) ||
                CollectionUtils.isEmpty(keycloakUserList.get(0).getAttributes().get(passwordResetCode)) ||
                StringUtils.isBlank(keycloakUserList.get(0).getAttributes().get(passwordResetCode).get(0)) ||
                !keycloakUserList.get(0).getAttributes().get(passwordResetCode).get(0).equals(request.getPasswordResetCode())) {
            throw new RequestException(PASSWORD_RESET_CODE_INVALID_ERROR_MESSAGE);
        } else {
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put(passwordResetCode, new ArrayList<>());

            Credential credential = new Credential();
            credential.setType("password");
            credential.setValue(request.getPassword());
            credential.setTemporary(false);

            KeycloakResetPasswordRequest keycloakResetPasswordRequest = new KeycloakResetPasswordRequest();
            keycloakResetPasswordRequest.setAttributes(attributes);
            keycloakResetPasswordRequest.setCredentials(Collections.singletonList(credential));

            keycloakUserClient.updateKeycloakAccountCredentials(keycloakUserList.get(0).getId(), keycloakResetPasswordRequest);
        }
    }
}
