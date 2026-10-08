package vn.edu.ute.capstone.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.UserProfileDTO;
import vn.edu.ute.capstone.entity.Role;
import vn.edu.ute.capstone.entity.User;
import vn.edu.ute.capstone.repository.UserRepository;
import vn.edu.ute.capstone.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfileByUsername(String username) {
        User user = findByUsername(username);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String departmentName = (user.getDepartment() != null) ? user.getDepartment().getName() : "Không thuộc bộ môn";

        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .departmentName(departmentName)
                .roles(roleNames)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với tài khoản: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getLecturersByDepartment(Long departmentId) {
        return userRepository.findAll().stream()
                .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(departmentId))
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_LECTURER")))
                .toList();
    }

}
