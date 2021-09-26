package com.demo.repository;

import com.demo.model.UserAccount;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserAccountRepository extends CrudRepository<UserAccount, Integer> {
    Optional<UserAccount> getUserAccountByTypeAndEmailAddress(String type, String emailAddress);
}
