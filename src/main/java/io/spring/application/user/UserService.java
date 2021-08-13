package io.spring.application.user;

import io.spring.core.user.EncryptService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Service
@Validated
public class UserService {
    private final UserRepository userRepository;
    private final String defaultImage;
    private final EncryptService encryptService;

    @Autowired
    public UserService(UserRepository userRepository, @Value("${image.default}") String defaultImage, EncryptService encryptService) {
        this.userRepository = userRepository;
        this.defaultImage = defaultImage;
        this.encryptService = encryptService;
    }

    public User createUser(@Valid RegisterParam registerParam) {
        User user = new User(registerParam.getEmail(), registerParam.getUsername(), encryptService.encrypt(registerParam.getPassword()), "", defaultImage);
        userRepository.save(user);
        return user;
    }

    public void updateUser(@Valid UpdateUserCommand command) {
        User user = command.getTargetUser();
        UpdateUserParam updateUserParam = command.getParam();
        user.update(updateUserParam.getEmail(), updateUserParam.getUsername(), updateUserParam.getPassword(), updateUserParam.getBio(), updateUserParam.getImage());
        userRepository.save(user);
    }
}
