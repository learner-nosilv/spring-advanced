package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // [다건조회]
    // [상황] Todo Entity: Todo-User ManyToOne: 여러 Todo가 하나의 User에 연결됨 + LAZY이므로 User 데이터는 필요할 때 로딩된다
    // [문제] JPA가 FETCH JOIN이 포함된 쿼리에서 + findAll...()하여 페이징을 적용 (Page<T>) 하려고 함
    //      Todo Entity 를 전부 조회할 때에 User는 Lazy 로딩되므로 모든 데이터를 메모리에 로드한 후 페이징을 처리하려고 한다 = 성능저하
    //  1. 1회 : 총 Todo 개수 조회(Count 쿼리) : SELECT * FROM todo ORDER BY modifiedAt DESC; = N 개
    //  2. N회 : 조회된 Todo의 실제 User 조회(FETCH JOIN) : SELECT * FROM user WHERE id = [...]; X N 회
    // [해결방안] FETCH JOIN을 사용하지 않고 @EntityGraph(attributePaths = "user") 를 사용한다
    @EntityGraph(attributePaths = "user")
    @Query("SELECT t FROM Todo t ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    // [단건조회]
    // [상황] Todo Entity: Todo-User ManyToOne: 여러 Todo가 하나의 User에 연결됨 + LAZY이므로 User 데이터는 필요할 때 로딩된다
    // 하나의 Todo Entity만을 findById(단일조회)하므로 위와는 달리 추가적인 N회의 User조회 쿼리가 발생하지 않음
    //  = 페이징 문제가 발생하지 않음
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
    // Optional : 조회되는 Todo가 없는 경우 null 반환
    int countById(Long todoId);
}
