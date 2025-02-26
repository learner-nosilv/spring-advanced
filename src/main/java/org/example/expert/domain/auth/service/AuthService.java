package org.example.expert.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입 인증 서비스
    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        // 사용자가 입력한 비밀번호(signupRequest.getPassword())를
        // passwordEncoder.encode(): 암호화해서
        // 암호화된 값을 encodedPassword에 저장하기
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // 사용자가 선택한 역할을 UserRole타입(Enum)으로 변환하여 변수에 저장하기
        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        // 사용자가 입력한 이메일(signupRequest.getEmail())이
        // userRepository.existsByEmail(): 이미 존재하는지 검사하여
        // 이미 존재한다면 InvalidRequestException 예외 날리기
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new InvalidRequestException("이미 존재하는 이메일입니다.");
        }
/*  [문제점]
    - 비밀번호 암호화, UserRole 설정을 하더라도
    - 사용자 이메일이 중복된다면 위의 과정이 쓸모없어짐

    [개선방향] Early return
    - 사용자 이메일 중복여부를 더 먼저 검사하여
    - 불필요한 비밀번호 암호화, UserRole 설정 과정을 없애자
*/

        // 새 User형 변수를 만들고
        User newUser = new User(
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );

        // 실제 레포지토리에 저장
        User savedUser = userRepository.save(newUser);

        // 실제 레포에도 저장되어 회원가입이 완료된 회원의 JWT(JSON Web Token)를 생성하여 문자열 변수에 저장
        // = API 요청 시 사용자 인증에 활용되는 JWT 생성
        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), userRole);

        // 회원 가입이 완료된 회원의 토큰을 인자로 하는 SignupResponse 객체 반환
        return new SignupResponse(bearerToken);
    }

    // 로그인 서비스 : 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401 반환
    @Transactional(readOnly = true)     // 읽기 전용 모드: 영속성 컨텍스트 감지를 하지 않으므로 성능 향상
    public SigninResponse signin(SigninRequest signinRequest) {
        // 입력한 이메일을 레포지토리에서 찾아보고. 찾지 못하면 InvalidRequestException 발생
        User user = userRepository.findByEmail(signinRequest.getEmail()).orElseThrow(
                () -> new InvalidRequestException("가입되지 않은 유저입니다."));

        // 사용자가 입력한 패스워드와 signinRequest.getPassword()
        // 레포지토리에서 해당 id에 해당하는 패스워드 user.getPassword() 가 일치하는지 체크한 후
        // 비밀번호가 다르다면 AuthExcetion 발생
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new AuthException("잘못된 비밀번호입니다.");
        }

        // 사용자의 ID, 이메일, 역할을 포함한 토큰 생성 문자열 변수에 저장
        // = API 요청 시 사용자 인증에 활용되는 JWT 생성
        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        // 로그인이 완료된 회원의 토큰을 인자로 하는 SigninResponse 객체 반환
        return new SigninResponse(bearerToken);
    }
}