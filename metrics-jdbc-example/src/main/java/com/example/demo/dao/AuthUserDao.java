package com.example.demo.dao;

import com.example.demo.domain.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by patterncat on 2017-04-11.
 */
@Repository
public interface AuthUserDao extends JpaRepository<AuthUser,Long> {

    AuthUser findByPrincipal(String principal);

    List<AuthUser> findByVersion(Long version);
}
