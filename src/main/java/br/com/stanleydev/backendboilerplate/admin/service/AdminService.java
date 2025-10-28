package br.com.stanleydev.backendboilerplate.admin.service;

import br.com.stanleydev.backendboilerplate.admin.dto.AdminUpdateUserRequest;
import br.com.stanleydev.backendboilerplate.exception.EmailAlreadyExistsException;
import br.com.stanleydev.backendboilerplate.exception.ResourceNotFoundException;
import br.com.stanleydev.backendboilerplate.user.dto.UserResponse;
import br.com.stanleydev.backendboilerplate.user.model.User;
import br.com.stanleydev.backendboilerplate.user.repository.UserRepository;
import br.com.stanleydev.backendboilerplate.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserService userService; // We reuse the DTO mapper


    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userService::mapToUserResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userService.mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException("Email already taken");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);
        return userService.mapToUserResponse(updatedUser);
    }


    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}