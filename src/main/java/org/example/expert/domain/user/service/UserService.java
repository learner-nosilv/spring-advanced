package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    // 비밀번호 변경
    @Transactional  // 메서드가 실행되는 동안 트랜젝션이 활성화되어 오류가 발생하는 경우 데이터를 롤백함
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        // 새로운 비밀번호가 규격에 맞는지 확인하여 규격에 맞지않는다면 InvalidRequestException 발생
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
        // [문제점] 비밀번호 유효성 검사는 DTO 만들 때 하면 되지,
        //        굳이 Controller층을 지나 비즈니스 로직을 수행하는 Service층에서 검사할 필요가 없음
        // [해결방법] DTO에서 바로 비밀번호 유효성 검사까지 진행한다

        // userId에 해당하는 User를 Repository(DB)에서 찾기 - 없다면 InvalidRequestException 발생
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // 사용자가 요청한 새로운 비밀번호(userChangePasswordRequest.getNewPassword())와
        // 기존 비밀번호(user.getPassword) 를 비교하기 - 같다면 InvalidRequestException 발생
        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        // 사용자가 입력한 기존 비밀번호(userChangePasswordRequest.getOldPassword())와
        // 기존 비밀번호(user.getPassword) 를 비교하기 - 다르다면 InvalidRequestException 발생
        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        // 모든 예외를 뚫었다면, 새로운 비밀번호를 암호화하여 사용자 비밀번호로 저장
        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }
}