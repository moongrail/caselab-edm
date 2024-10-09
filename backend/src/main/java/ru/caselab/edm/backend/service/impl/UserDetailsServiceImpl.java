package ru.caselab.edm.backend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.UserRepository;

import java.util.Optional;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        LOGGER.debug("loadUserByUsername");
        Optional<User> user = userRepository.findUserByLogin(username);

        User existingUser = user.get();

        if (user.isEmpty()) {
            LOGGER.error("Username not found: " + username);
            throw new UsernameNotFoundException("username not found");
        }
        LOGGER.info("Successful");
        return new UserInfoDetails(existingUser);
    }
}
