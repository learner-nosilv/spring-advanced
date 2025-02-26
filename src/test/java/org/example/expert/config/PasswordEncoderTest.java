package org.example.expert.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class PasswordEncoderTest {

    @InjectMocks
    private PasswordEncoder passwordEncoder;

    @Test
    void matches_메서드가_정상적으로_동작한다() {
        // given
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when
        boolean matches = passwordEncoder.matches(encodedPassword, rawPassword);
        // passwordEncoder.matches()메서드: 첫번째 인자를 BCrypt 해싱 알고리즘을 사용하여 암호화한 후, 두번째 인자(해시값)과 비교하는 메소드
        // public boolean matches(String rawPassword, String encodedPassword)
        // [문제] 첫 번째 인자에 암호화되지 않은 패스워드, 두 번째 인자에 암호화된 패스워드를 기입헤야 함 (뒤바뀜)

        // then
        assertTrue(matches);
    }
}
