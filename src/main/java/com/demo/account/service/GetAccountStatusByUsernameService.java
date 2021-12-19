package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.demo.ErrorMessage.NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE;

@Slf4j
@Service
public class GetAccountStatusByUsernameService {
    @Autowired
    private KeycloakUserClient keycloakUserClient;

    public AccountStatus getAccountStatusByUsername(String username) {
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(username);
        keycloakUserList.removeIf(account -> !username.equals(account.getUsername()));

        if (CollectionUtils.isEmpty(keycloakUserList)) {
            return AccountStatus.UNREGISTERED;
        } else if (keycloakUserList.size() > 1) {
            log.error("{} - {}", NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE, username);
            throw new KeycloakException(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);
        } else if (keycloakUserList.get(0).isEmailVerified()) {
            return AccountStatus.ACTIVE;
        } else {
            return AccountStatus.UNVERIFIED;
        }
    }
}
