package com.ecom.service.impl;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl  implements UserService {

    @Autowired
    private UserRepository  userRepository;

    @Override
    public UserDtls saveUser(UserDtls user) {
        UserDtls saveUser = userRepository.save(user);

        return saveUser;
    }

}
