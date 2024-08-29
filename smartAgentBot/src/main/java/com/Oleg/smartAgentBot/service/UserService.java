package com.Oleg.smartAgentBot.service;



import com.Oleg.smartAgentBot.model.User;
import com.Oleg.smartAgentBot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

}
