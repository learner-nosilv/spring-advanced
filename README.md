# SPRING ADVANCED

## Git PR & Commit
- [Lv1. PR](https://github.com/learner-nosilv/spring-advanced/pull/1) 
- [Lv 2. N+1 문제 PR](https://github.com/learner-nosilv/spring-advanced/pull/2)
- [Lv 3. 테스트코드 연습 PR](https://github.com/learner-nosilv/spring-advanced/pull/3)


단계별로 해당 과제의 대한 내용과 함께 남긴 커밋 목록
```bash
레벨 1-1: AuthService 코드분석
레벨 1-1: Early Return 완료
레벨 1-2: WeatherClient 코드분석
레벨 1-2: 리팩토링 퀴즈 - 불필요한 피하기
레벨 1-3: UserService 코드분석
레벨 1-3: 코드 개선 퀴즈 - Validation
레벨 2. Todo entity와 Repository분석
레벨 2. N+1 문제
레벨 3. PasswordEncoder, Test 코드 분석
레벨 3-1: 테스트 코드 연습-1
레벨 3-2-1: 테스트 코드 연습-2의 1번 케이스
레벨 3-2-2: 테스트 코드 연습-2의 2번 케이스
레벨 3-2-3: 테스트 코드 연습-2의 3번 케이스
```

## Lv 1. 코드 개선 `필수`
## **1-1. 코드 개선 퀴즈 -** Early Return
- 파일 위치 `org.example.expert.domain.auth.service` 의 `AuthService` class
```
// 회원가입 인증 서비스
@Transactional
public SignupResponse signup(SignupRequest signupRequest) {

    // 1. 사용자가 입력한 비밀번호를 암호화하여 변수에 저장
    String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

    // 2. 사용자가 선택한 역할을 UserRole타입(Enum)으로 변환하여 변수에 저장
    UserRole userRole = UserRole.of(signupRequest.getUserRole());

    // 3. 사용자가 입력한 이메일이 DB 상에 이미 존재하는지 검사하여, 존재한다면 예외발생시킴 [문제의 지점] 
    if (userRepository.existsByEmail(signupRequest.getEmail())) {
        throw new InvalidRequestException("이미 존재하는 이메일입니다.");
    }
...
```
### 문제점
3. 사용자 이메일이 중복되어 예외가 발생한다면, 이미 진행한 과정인 1.비밀번호암호화, 2. UserRole 설정과정이 쓸모없어짐

### 개선방향 Early return
3. 사용자 이메일 중복여부검사를 가장 먼저 진행하기
```
// 회원가입 인증 서비스
@Transactional
public SignupResponse signup(SignupRequest signupRequest) {
    // 3->1. 사용자가 입력한 이메일이 DB 상에 이미 존재하는지 검사하여, 존재한다면 예외발생시킴 [개선지점] 
      if (userRepository.existsByEmail(signupRequest.getEmail())) {
          throw new InvalidRequestException("이미 존재하는 이메일입니다.");
      }

    // 1->2. 사용자가 입력한 비밀번호를 암호화하여 변수에 저장
      String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

    // 2->3. 사용자가 선택한 역할을 UserRole타입(Enum)으로 변환하여 변수에 저장
      UserRole userRole = UserRole.of(signupRequest.getUserRole());
...
```

## **1-2. 리팩토링 퀴즈 - 불필요한 `if-else` 피하기**
- 파일 위치 `org.example.expert.client` 의 `WeatherClient` class 의 `getTodayWeather()` method
```
// 외부 API를 사용하여 오늘의 날씨 데이터를 가져와서 weatherDto에 맞추어 반환하는 메소드
public String getTodayWeather() {

    // 1. 날씨 API 에 데이터를 요청한 후, 값을 변수에 할당
    ResponseEntity<WeatherDto[]> responseEntity =
            restTemplate.getForEntity(buildWeatherApiUri(), WeatherDto[].class);

    // 2. 위에서 할당받은 값의 Body 만을 배열에 할당
    WeatherDto[] weatherArray = responseEntity.getBody();        // [문제의 지점 1]

    // 3-1. 할당받은 값의 상태코드가 정상OK(200)인지 검사 & 예외 발생
    if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
        throw new ServerException("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + responseEntity.getStatusCode());
    // 3-2. 할당받은 배열이 null이거나 비어있는 것 역시 ServerException 발생 [문제의 지점2]
    } else { 
        if (weatherArray == null || weatherArray.length == 0) {
            throw new ServerException("날씨 데이터가 없습니다.");
        }
    }
...
```

### 문제점
- [문제의 지점 1] 의 변수 weatherArray 가  계속 사용되지 않다가 [문제의 지점 2]  에서 사용됨
- [문제의 지점 2] 가 굳이 if-else의 else가 될 필요 없음

### 개선방향: 불필요한 if-else 제거
- [문제의 지점 1] 2. weatherArray 초기화 문을 [문제의 지점 2] 3-2. 근처로 옮기면서 if-else관계 제거하기

```
// 외부 API를 사용하여 오늘의 날씨 데이터를 가져와서 weatherDto에 맞추어 반환하는 메소드
public String getTodayWeather() {

    // 1. 날씨 API 에 데이터를 요청한 후, 값을 변수에 할당
    ResponseEntity<WeatherDto[]> responseEntity =
            restTemplate.getForEntity(buildWeatherApiUri(), WeatherDto[].class);

    // 3-1. -> 2. 할당받은 값의 상태코드가 정상OK(200)인지 검사 & 예외 발생
    if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
        throw new ServerException("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + responseEntity.getStatusCode());
    }

    // 2. -> 3. 위에서 할당받은 값의 Body 만을 배열에 할당 [개선지점 1]
    WeatherDto[] weatherArray = responseEntity.getBody();

    // 3-2. -> 4. 할당받은 배열이 null이거나 비어있는 것 역시 ServerException 발생 [개선지점 2]
    if (weatherArray == null || weatherArray.length == 0) {
          throw new ServerException("날씨 데이터가 없습니다.");
    }
}
...
```

## **1-3. 코드 개선 퀴즈 - Validation**
- 파일 위치 `org.example.expert.domain.user.service` 의 `UserService` class 의 `changePassword()` method
```
// 비밀번호 변경
public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
  // 1. 새로운 비밀번호가 규격에 맞는지 확인하여 규격에 맞지않는다면 InvalidRequestException 발생 [문제의 지점] 
  if (userChangePasswordRequest.getNewPassword().length() < 8 ||
          !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
          !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
      throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
  }
  ...
```

### 문제점
- 비밀번호 유효성 검사를 비즈니스 로직을 수행하는 Service층까지 들어와서 해야할 필요가 없음

### 개선방향: DTO Validation
- Service층이 아닌, DTO를 생성하는 Controller층에서 비밀번호 유효성 검사를 진행하도록 한다

- ① 파일 위치 `org.example.expert.domain.user.service` 의 `UserService` class 의 `changePassword()` method
```
// 비밀번호 변경
public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
  // 1. -> 제거 새로운 비밀번호가 규격에 맞는지 확인하여 규격에 맞지않는다면 InvalidRequestException 발생 [개선 지점] 
  ...
```

- ② 파일 위치 `org.example.expert.domain.user.controller` 의 `UserController` class 의 `changePassword()` method
  - DTO(UserChangePasswordRequest)를 생성할 때 유효성 검사를 하도록 `@Valid` 를 붙힌다.
  - **수정 전** `public void changePassword( @Auth AuthUser authUser, @RequestBody UserChangePasswordRequest userChangePasswordRequest)`
  - **수정 후** `public void changePassword( @Auth AuthUser authUser, @Valid @RequestBody UserChangePasswordRequest userChangePasswordRequest)`

- ③ 파일 위치 `org.example.expert.domain.user.dto.request` 의 `UserChangePasswordRequest` class
  - **수정 전**
  ```
  @NotBlank
  private String newPassword;
  ```

  - **수정 후**
  ```
  @NotBlank
  @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
  @Pattern(regexp = ".*\\d.*", message = "새 비밀번호는 숫자를 포함해야 합니다.")
  @Pattern(regexp = ".*[A-Z].*", message = "새 비밀번호는 대문자를 포함해야 합니다.")
  private String newPassword;
  ```

---

## Lv 2. N+1 문제  `필수`

> 엔티티 정의에서 ManyToOne이 있고 One인 부모 데이터가 LAZY loading 인 상황에서 해당 엔티티에 다건 조회하는 경우
> N+1 번의 쿼리가 발생하여 성능이 저하되는 문제가 발생할 수 있다.
> - 1회 : Count쿼리 = 총 데이터 개수(N개) 파악  
>   `SELECT COUNT(*) FROM todo;`  
> - N회 : 각 데이터에 대한 부모 데이터 조회 X N 번  
>   `SELECT * FROM user WHERE id = ?;`  X N 번  
> - 해결 방안 : FETCH JOIN, @EntityGraph, @BatchSize  
>   FETCH JOIN 문제 : 페이징과 함께 사용하면 경고 및 성능저하 발생  

- 파일 위치 `org.example.expert.domain.todo.repository` 의 `TodoRepository` interface

## 1.

```
@Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);
```

- **문제점** N+1 문제가 발생하는 상황 + Paging 적용 + FETCH JOIN → 경고 및 성능저하 문제발생
 
- **개선방향** FETCH JOIN을 사용하지 않고 @EntityGraph(attributePaths = "user") 를 사용한다
```
@EntityGraph(attributePaths = "user")
@Query("SELECT t FROM Todo t ORDER BY t.modifiedAt DESC")
Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);
```
    
## 2.

```
@Query("SELECT t FROM Todo t " + "LEFT JOIN FETCH t.user " + "WHERE t.id = :todoId")
Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
```

- **문제 없음** 하나의 Todo Entity만을 findById(단일조회)하므로 N+1 문제가 발생하지 않음

---
  
## Lv 3. 테스트코드 연습 `선택`
## **3-1. 테스트 코드 연습 - 1 (예상대로 성공하는지에 대한 케이스입니다.)**
- **문제** : passwordEncoder.matches(암호화 안 된 패스워드, 암호화 된 패스워드) 형태인데, 인자값을 잘못 넣었다.
  - `boolean matches = passwordEncoder.matches(encodedPassword, rawPassword);`
- **해결** : 메소드 정의에 맞게 인자값을 제대로 넣는다(두 인자를 바꾸면 됨)
  - `boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);`

## **3-2. 테스트 코드 연습 - 2 (예상대로 예외처리 하는지에 대한 케이스입니다.)**
### 3-2-케이스1. 예상대로 예외처리 하는지에 대한 케이스
- 파일 위치 `‎src/main/java/org/example/expert/domain/manager/service` 의 `ManagerServiceTest` class 의 `getManagers` method
  ```
  public List<ManagerResponse> getManagers(long todoId) {
    Todo todo = todoRepository.findById(todoId)
      .orElseThrow(() -> new InvalidRequestException("Todo not found"));
  ```
  - getManager 는 `InvalidRequestException` 예외를 발생시키고 `"Todo not found"` 메세지를 만든다.

- 파일 위치 `‎src/test/java/org/example/expert/domain/manager/service` 의 `ManagerService` class
  - **문제**: 테스트 코드의 이름이 의미적으로 맞지 않고, 비교하려는 에러 메세지를 틀리게 작성하였다. 
    - `public void manager_목록_조회_시_Todo가_없다면_NPE_에러를_던진다()` [이름오류] 해당 메서드는 NPE가 아니라 IRE 에러를 던짐
    - `assertEquals("Manager not found", exception.getMessage());` [코드오류] 발생하는 에러 메세지는 Manager not found가 아니라 Todo not found
  - **해결**: 테스트 코드의 이름과 비교하려는 에러 메세지를 getManager의 예외타입과 에러메세지로 작성한다. 
    - `public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다()`
    - `assertEquals("Todo not found", exception.getMessage());`


### 3-2-케이스2. 예상대로 예외처리 하는지에 대한 케이스
- 파일 위치 `‎‎src/main/java/org/example/expert/domain/comment/service` 의 `CommentService` class 의 `saveComment` method
  ```
  public CommentSaveResponse saveComment(AuthUser authUser, long todoId, CommentSaveRequest commentSaveRequest) {
    User user = User.fromAuthUser(authUser);
    Todo todo = todoRepository.findById(todoId).orElseThrow(() ->
      new InvalidRequestException("Todo not found"));
  ... }
  ```
  - saveComment 는 `InvalidRequestException` 예외를 발생시키고 `"Todo not found"` 메세지를 만든다.  
  
- 파일 위치 `‎src/test/java/org/example/expert/domain/comment/service` 의 `CommentServiceTest` class
  - **문제**: 테스트 코드는 `saveComment()` 에서 `ServerException`이 발생할 때를 다루는데, 실상 `saveComment()`에서는 해당 예외가 발생하지 않음
    - `import org.example.expert.domain.common.exception.ServerException;`
    -  `ServerException exception = assertThrows(ServerException.class, () -> {`
  - **해결**: `saveComment()`에서 발생하는 예외를 테스트 코드가 다루도록 수정한다.
    - `import org.example.expert.domain.common.exception.InvalidRequestException;`
    -  `InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {`

### 3-2-케이스3. 예상대로 예외처리 하는지에 대한 케이스

- 파일위치 `src/main/java/org/example/expert/domain/manager/service` 의 `ManagerService` class의 `saveManager` method 
  ```
  @Transactional
  public ManagerSaveResponse saveManager(AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest) {
    User user = User.fromAuthUser(authUser);
    Todo todo = todoRepository.findById(todoId)
      .orElseThrow(() -> new InvalidRequestException("Todo not found"));
  ...
  ```

- **방향성** todo의 user가 null인 경우 InvalidRequestException을 발생시키는 코드를 추가한다.
  ```
  @Transactional
  public ManagerSaveResponse saveManager(AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest) {
    User user = User.fromAuthUser(authUser);
    Todo todo = todoRepository.findById(todoId)
      .orElseThrow(() -> new InvalidRequestException("Todo not found"));
    if(todo.getUser() == null){
      throw new InvalidRequestException("해당 일정을 만든 유저가 유효하지 않습니다.");
    }
  ...
  ```

- **추가 변경사항** 비교할 에러메세지를 수정한다
  - 파일 위치 `‎‎src/test/java/org/example/expert/domain/manager/service` 의 `ManagerServiceTest` class
  - 수정 전 `assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());`
  - 수정 후 `assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());`
