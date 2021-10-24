package com.demo.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserAccountRepository extends CrudRepository<UserAccount, Integer> {
    Optional<UserAccount> getUserAccountByTypeAndEmailAddress(String type, String emailAddress);
}
