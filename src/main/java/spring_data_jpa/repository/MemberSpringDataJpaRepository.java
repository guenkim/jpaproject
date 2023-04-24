package spring_data_jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import spring_data_jpa.dto.MemberDto;
import spring_data_jpa.entitiy.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/******************************************
 * spring data jpa 공통 인터페이스 repository
 *****************************************/
public interface MemberSpringDataJpaRepository extends JpaRepository<Member,Long> , MemberSpringDataJpaCustomRepository{

    /**
     *  메소드 이름으로 쿼리 생성
     *  쿼리 메소드 필터 조건
     * 스프링 데이터 JPA 공식 문서 참고:
     * (https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation)
     * 조회: find…By ,read…By ,query…By get…By,
     *   https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
     * 예:) findHelloBy 처럼 ...에 식별하기 위한 내용(설명)이 들어가도 된다.
     *  COUNT: count…By 반환타입 long
     *  EXISTS: exists…By 반환타입 boolean
     *  삭제: delete…By, remove…By 반환타입 long
     *  DISTINCT: findDistinct, findMemberDistinctBy
     *  LIMIT: findFirst3, findFirst, findTop, findTop3
     *  https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result
     *
     **/
    List<Member> findByUsernameAndAgeGreaterThan(String username , int age);

    /**
     JPA NamedQuery : 실무에서 거의 사용하지 않음
     Member entity에 선언
     @NamedQuery(name="Member.searchUser",
     query="select m from Member m where username= :username"
     )
     **/
    @Query(name="Member.searchUser")
    List<Member> searchUser(@Param("username") String username);


    /**
     @Query

     스프링 데이터 JPA를 사용하면 실무에서 Named Query를 직접 등록해서 사용하는 일은 드물다.
     대신 @Query 를 사용해서 리파지토리 메소드에 쿼리를 직접 정의한다.

     > 참고: 실무에서는 메소드 이름으로 쿼리 생성 기능은 파라미터가 증가하면 메서드 이름이 매우
     지저분해진다. 따라서 @Query 기능을 자주 사용하게 된다
     **/
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username , @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    /**
    DTO로 직접 조회
     **/
    @Query("select new spring_data_jpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * 컬렉션 파라미터 바인딩
     * Collection 타입으로 in절 지원
     */
    @Query("select m from Member m where m.username in :names ")
    List<Member> findMembers(@Param("names") Collection<String> members);

    /**
     - 반환 타입 (메소드 이름으로 쿼리 생성)
     스프링 데이터 JPA는 유연한 반환 타입 지원
     List<Member> findByUsername(String name); //컬렉션
     Member findByUsername(String name); //단건
     Optional<Member> findByUsername(String name); //단건 Optional
     */
    List<Member> findListByUsername(String name); //컬렉션
    Member findByUsername(String name); //단건
    Optional<Member> findOptionalByUsername(String name); //단건 Optional

    /**
     *스프링 데이터 JPA 페이징과 정렬
     * 페이징과 정렬 파라미터
     *      org.springframework.data.domain.Sort : 정렬 기능
     *      org.springframework.data.domain.Pageable : 페이징 기능 (내부에 Sort 포함)
     * 특별한 반환 타입
     *      org.springframework.data.domain.Page : 추가 count 쿼리 결과를 포함하는 페이징
     *      org.springframework.data.domain.Slice : 추가 count 쿼리 없이 다음 페이지만 확인 가능 (내부적으로 limit + 1조회)
     *
     * List (자바 컬렉션): 추가 count 쿼리 없이 결과만 반환
     *
     * 주의: Page는 1부터 시작이 아니라 0부터 시작이다.
     */
    Page<Member> findPageByAge(int age, Pageable pageable); //count 쿼리 사용

    /** count 쿼리 분리 **/
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m"
    )
    Page<Member> upgradeFindPageByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable); //count 쿼리 사용 안함
    List<Member> findListByAge(int age, Pageable pageable); //count 쿼리 사용 안함
    List<Member> findSortByAge(int age, Sort sort);

    /**
     * 스프링 데이터 JPA를 사용한 벌크성 수정 쿼리
     */
    @Modifying(clearAutomatically = true) //벌크 수정, 영속성 컨텍스트 초기화
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);


    /**
     fetch join (member,team 함께 조인 << lazy join과 반대 개념)
     **/
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();


    /********************************
     * EntityGraph 정리
     * 사실상 페치 조인(FETCH JOIN)의 간편 버전
     * LEFT OUTER JOIN 사용
     ******************************/

    //공통 메서드 오버라이드
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();


    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();


    //메서드 이름으로 쿼리에서 특히 편리하다.
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(String username);

    /*****************************************
     * JPA Hint & Lock
     * JPA Hint
     * JPA 쿼리 힌트(SQL 힌트가 아니라 JPA 구현체에게 제공하는 힌트)
     * return 받은 member 엔티티는 dirty checking 안됨 (즉, 수정 안됨)
     * @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
     **************************************************/
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);


    /********************
    select for update를 위해 lock을 지원
     ******************/
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);


    /*************************************
     * Projections
     * 엔티티 대신에 DTO를 편리하게 조회할 때 사용
     * 전체 엔티티가 아니라 만약 회원 이름만 딱 조회하고 싶으면?
     * 인터페이스 기반
     * * 주의
     *          * 프로젝션 대상이 root 엔티티면, JPQL SELECT 절 최적화 가능
     *          * 프로젝션 대상이 ROOT가 아니면
     *          * LEFT OUTER JOIN 처리
     *          * 모든 필드를 SELECT해서 엔티티로 조회한 다음에 계산
     *      * 정리
     *          * 프로젝션 대상이 root 엔티티면 유용하다.
     *          * 프로젝션 대상이 root 엔티티를 넘어가면 JPQL SELECT 최적화가 안된다!
     *          * 실무의 복잡한 쿼리를 해결하기에는 한계가 있다.
     *          * 실무에서는 단순할 때만 사용하고, 조금만 복잡해지면 QueryDSL을 사용하자
     *************************************/
    List<UserNameOnly> findProjectionsByUsername(@Param("username")String username);

    /*************************************
     * Projections
     * 엔티티 대신에 DTO를 편리하게 조회할 때 사용
     * 전체 엔티티가 아니라 만약 회원 이름만 딱 조회하고 싶으면?
     * DTO 기반
     * * 주의
     *          * 프로젝션 대상이 root 엔티티면, JPQL SELECT 절 최적화 가능
     *          * 프로젝션 대상이 ROOT가 아니면
     *          * LEFT OUTER JOIN 처리
     *          * 모든 필드를 SELECT해서 엔티티로 조회한 다음에 계산
     *      * 정리
     *          * 프로젝션 대상이 root 엔티티면 유용하다.
     *          * 프로젝션 대상이 root 엔티티를 넘어가면 JPQL SELECT 최적화가 안된다!
     *          * 실무의 복잡한 쿼리를 해결하기에는 한계가 있다.
     *          * 실무에서는 단순할 때만 사용하고, 조금만 복잡해지면 QueryDSL을 사용하자
     *************************************/
    List<UsernameOnlyDto> findProjectionsDtoByUsername(@Param("username")String username);

    /*************************************
     * Projections
     * 엔티티 대신에 DTO를 편리하게 조회할 때 사용
     * 전체 엔티티가 아니라 만약 회원 이름만 딱 조회하고 싶으면?
     * 중첩구조 기반
     *
     * * 주의
     *          * 프로젝션 대상이 root 엔티티면, JPQL SELECT 절 최적화 가능
     *          * 프로젝션 대상이 ROOT가 아니면
     *          * LEFT OUTER JOIN 처리
     *          * 모든 필드를 SELECT해서 엔티티로 조회한 다음에 계산
     *      * 정리
     *          * 프로젝션 대상이 root 엔티티면 유용하다.
     *          * 프로젝션 대상이 root 엔티티를 넘어가면 JPQL SELECT 최적화가 안된다!
     *          * 실무의 복잡한 쿼리를 해결하기에는 한계가 있다.
     *          * 실무에서는 단순할 때만 사용하고, 조금만 복잡해지면 QueryDSL을 사용하자
     *************************************/
    List<NestedClosedProjection> findProjectionsNestedByUsername(@Param("username")String username);

    /*************************************
     * 네이티브 쿼리
     * 가급적 네이티브 쿼리는 사용하지 않는게 좋음, 정말 어쩔 수 없을 때 사용
     * 최근에 나온 궁극의 방법 스프링 데이터 Projections 활용
     *
     * 스프링 데이터 JPA 기반 네이티브 쿼리
         * 페이징 지원
         * 반환 타입
         * Object[]
         * Tuple
         * DTO(스프링 데이터 인터페이스 Projections 지원)
     * 제약
         * Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음(믿지 말고 직접 처리)
         * JPQL처럼 애플리케이션 로딩 시점에 문법 확인 불가
         * 동적 쿼리 불가
    *************************************/
    @Query(value = "select * from member where username=?",nativeQuery = true)
    Member findNativeQuery(String username);


    /*************************************
     * 네이티브 쿼리
     * 가급적 네이티브 쿼리는 사용하지 않는게 좋음, 정말 어쩔 수 없을 때 사용
     * 최근에 나온 궁극의 방법 스프링 데이터 Projections 활용
     *
     * 스프링 데이터 JPA 기반 네이티브 쿼리
     * 페이징 지원
     * 반환 타입
     * Object[]
     * Tuple
     * DTO(스프링 데이터 인터페이스 Projections 지원)
     * 제약
     * Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음(믿지 말고 직접 처리)
     * JPQL처럼 애플리케이션 로딩 시점에 문법 확인 불가
     * 동적 쿼리 불가
     *
     * native query + projection 기반
     *  메소드 : findByNativeProjection
     *  DTO 반환시 사용 : 나름 괜찮아 보임
     *************************************/
    @Query(value = "SELECT m.member_id as id, m.username, t.name as teamName " +
            "FROM member m left join team t ON m.team_id = t.team_id",
            countQuery = "SELECT count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);
}
