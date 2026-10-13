package vn.edu.ute.capstone.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.dto.UserCreateDTO;
import vn.edu.ute.capstone.dto.UserProfileDTO;
import vn.edu.ute.capstone.dto.UserUpdateDTO;
import vn.edu.ute.capstone.entity.Department;
import vn.edu.ute.capstone.entity.Role;
import vn.edu.ute.capstone.entity.User;
import vn.edu.ute.capstone.repository.DepartmentRepository;
import vn.edu.ute.capstone.repository.RoleRepository;
import vn.edu.ute.capstone.repository.UserRepository;
import vn.edu.ute.capstone.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với tài khoản: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<User> getUsersPaged(String currentUsername, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("id").descending()
        );

        if (currentUsername == null || currentUsername.isBlank()) {
            return userRepository.findAll(pageable);
        }

        User currentAdmin = userRepository.findByUsername(currentUsername).orElse(null);
        // Nếu Admin thuộc 1 Khoa cụ thể (qua Department -> Faculty) -> Chỉ lấy User thuộc Khoa đó (Faculty Isolation)
        if (currentAdmin != null && currentAdmin.getDepartment() != null && currentAdmin.getDepartment().getFaculty() != null) {
            Long facultyId = currentAdmin.getDepartment().getFaculty().getId();
            log.info("[Faculty Isolation] Admin {} đang quản lý người dùng thuộc Khoa ID: {}", currentUsername, facultyId);
            return userRepository.findByFacultyId(facultyId, pageable);
        }

        // Trường hợp Super Admin toàn trường (hoặc không gắn khoa cụ thể)
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getLecturersByDepartment(Long departmentId) {
        return userRepository.findAll().stream()
                .filter(u -> u.getDepartment() != null && u.getDepartment().getId().equals(departmentId))
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_LECTURER".equals(r.getName())))
                .toList();
    }

    @Override
    @Transactional
    public User createUser(UserCreateDTO createDTO) {
        if (userRepository.existsByUsername(createDTO.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + createDTO.getUsername() + "' đã tồn tại trên hệ thống!");
        }
        if (userRepository.existsByEmail(createDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + createDTO.getEmail() + "' đã được sử dụng!");
        }

        Department department = null;
        if (createDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(createDTO.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Bộ môn không tồn tại với ID: " + createDTO.getDepartmentId()));
        }

        Set<Role> roles = new HashSet<>();
        if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
            roles.addAll(roleRepository.findAllById(createDTO.getRoleIds()));
        }

        User user = User.builder()
                .username(createDTO.getUsername().trim())
                .password(passwordEncoder.encode(createDTO.getPassword()))
                .fullName(createDTO.getFullName().trim())
                .email(createDTO.getEmail().trim())
                .phone(createDTO.getPhone())
                .department(department)
                .roles(roles)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateDTO updateDTO) {
        User user = findById(id);

        if (!user.getEmail().equalsIgnoreCase(updateDTO.getEmail()) && userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + updateDTO.getEmail() + "' đã được sử dụng bởi tài khoản khác!");
        }

        user.setFullName(updateDTO.getFullName().trim());
        user.setEmail(updateDTO.getEmail().trim());
        user.setPhone(updateDTO.getPhone());

        if (updateDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateDTO.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Bộ môn không tồn tại với ID: " + updateDTO.getDepartmentId()));
            user.setDepartment(department);
        } else {
            user.setDepartment(null);
        }

        if (updateDTO.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(updateDTO.getRoleIds()));
            user.setRoles(roles);
        }

        if (updateDTO.getIsActive() != null) {
            user.setIsActive(updateDTO.getIsActive());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = findById(id);
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = findById(id);
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
