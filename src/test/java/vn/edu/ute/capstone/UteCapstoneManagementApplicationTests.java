package vn.edu.ute.capstone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UteCapstoneManagementApplicationTests {

    @Test
    void contextLoads() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String hash = encoder.encode("123456");
        System.out.println("=== BCRYPT HASH OF 123456: " + hash + " ===");
        System.out.println("=== MATCHES: " + encoder.matches("123456", hash) + " ===");
    }

}
