package vn.edu.ute.capstone.service;

import vn.edu.ute.capstone.dto.UserCreateDTO;
import vn.edu.ute.capstone.dto.UserProfileDTO;
import vn.edu.ute.capstone.dto.UserUpdateDTO;
import vn.edu.ute.capstone.entity.User;

import java.util.List;

public interface UserService {

    UserProfileDTO getProfileByUsername(String username);

    User findByUsername(String username);

    User findById(Long id);

    List<User> getAllUsers();

    org.springframework.data.domain.Page<User> getUsersPaged(String currentUsername, int page, int size);

    List<User> getLecturersByDepartment(Long departmentId);

    User createUser(UserCreateDTO createDTO);

    User updateUser(Long id, UserUpdateDTO updateDTO);

    void toggleUserStatus(Long id);

    void changePassword(String username, String oldPassword, String newPassword);

    void resetPassword(Long id, String newPassword);

}
