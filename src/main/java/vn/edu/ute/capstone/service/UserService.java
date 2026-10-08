package vn.edu.ute.capstone.service;

import vn.edu.ute.capstone.dto.UserProfileDTO;
import vn.edu.ute.capstone.entity.User;

import java.util.List;

public interface UserService {

    UserProfileDTO getProfileByUsername(String username);

    User findByUsername(String username);

    List<User> getLecturersByDepartment(Long departmentId);

}
