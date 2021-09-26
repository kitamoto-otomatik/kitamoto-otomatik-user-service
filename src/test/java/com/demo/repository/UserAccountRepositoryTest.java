package com.demo.repository;

import com.demo.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("mock")
@ExtendWith(SpringExtension.class)
public class UserAccountRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    public void components() {
        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
        assertThat(entityManager).isNotNull();
        assertThat(userAccountRepository).isNotNull();
    }

    @Test
    public void getUserAccountByTypeAndEmailAddress() {
        Optional<UserAccount> actual = userAccountRepository.getUserAccountByTypeAndEmailAddress("root", "nikkinicholas@gmail.com");
        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getId()).isEqualTo("1");
        assertThat(actual.get().getType()).isEqualTo("root");
        assertThat(actual.get().getEmailAddress()).isEqualTo("nikkinicholas@gmail.com");
        assertThat(actual.get().getPassword()).isEqualTo("85c5aea0f0de0a610f64ae855562f985f9516ad5c34d8ea7a27f2306807feb43");
        assertThat(actual.get().getSalt()).isEqualTo("996b520b-7915-45ac-a3c2-1aed05c4b4a0");
        assertThat(actual.get().getFirstName()).isEqualTo("Nikki Nicholas");
        assertThat(actual.get().getLastName()).isEqualTo("Romero");
    }
}
