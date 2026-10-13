package vn.edu.ute.capstone.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ute.capstone.entity.User;
import vn.edu.ute.capstone.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String rawEmail = oAuth2User.getAttribute("email");
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_email"), "Không thể lấy địa chỉ email từ tài khoản Google.");
        }

        final String finalEmail = rawEmail.toLowerCase().trim();
        log.info("[OAuth2 Login Attempt] Email Google: {}", finalEmail);

        // Kiểm tra domain email của trường UTE
        boolean isStudent = finalEmail.endsWith("@student.hcmute.edu.vn");
        boolean isStaff = finalEmail.endsWith("@ute.edu.vn");

        if (!isStudent && !isStaff) {
            log.warn("[OAuth2 Login Denied] Domain không hợp lệ: {}", finalEmail);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("access_denied"),
                    "Hệ thống chỉ chấp nhận email chính thức do ĐH Sư phạm Kỹ thuật TP.HCM (HCMUTE) cấp (@student.hcmute.edu.vn hoặc @ute.edu.vn)."
            );
        }

        // Tìm người dùng trong CSDL bằng email
        User user = userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> {
                    log.warn("[OAuth2 Login Failed] Email {} hợp lệ nhưng chưa tồn tại trong CSDL", finalEmail);
                    return new OAuth2AuthenticationException(
                            new OAuth2Error("user_not_found"),
                            "Email trường '" + finalEmail + "' chưa được cấp quyền trong hệ thống Khóa luận tốt nghiệp. Vui lòng liên hệ Văn phòng Khoa/Bộ môn."
                    );
                });

        if (Boolean.FALSE.equals(user.getIsActive())) {
            log.warn("[OAuth2 Login Failed] Tài khoản {} đang bị khóa", finalEmail);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_disabled"),
                    "Tài khoản của bạn hiện đang bị tạm khóa. Vui lòng liên hệ Quản trị viên."
            );
        }

        log.info("[OAuth2 Login Success] Đăng nhập thành công cho người dùng: {} ({})", user.getFullName(), user.getUsername());
        return new CustomOAuth2UserDetails(user, oAuth2User.getAttributes());
    }
}
