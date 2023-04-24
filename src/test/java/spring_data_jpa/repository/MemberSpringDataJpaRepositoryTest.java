package spring_data_jpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import spring_data_jpa.dto.MemberDto;
import spring_data_jpa.dto.MemberJDBCDto;
import spring_data_jpa.entitiy.Member;
import spring_data_jpa.entitiy.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberSpringDataJpaRepositoryTest {

    /******************************************
     * spring data jpa 공통 인터페이스 repository 테스트
     *****************************************/
    @Autowired MemberSpringDataJpaRepository memberSpringDataJpaRepository;
    @Autowired TeamSpringDataJpaRepository teamSpringDataJpaRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember(){
        Member newMember = new Member("geun");
        Member savedMember = memberSpringDataJpaRepository.save(newMember);
        Member findMember = memberSpringDataJpaRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(newMember.getId());
        assertThat(findMember.getUsername()).isEqualTo(newMember.getUsername());
        assertThat(findMember).isEqualTo(newMember);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberSpringDataJpaRepository.save(member1);
        memberSpringDataJpaRepository.save(member2);
        //단건 조회 검증
        Member findMember1 =
                memberSpringDataJpaRepository.findById(member1.getId()).get();
        Member findMember2 =
                memberSpringDataJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);
        //리스트 조회 검증
        List<Member> all = memberSpringDataJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);
        //카운트 검증
        long count = memberSpringDataJpaRepository.count();
        assertThat(count).isEqualTo(2);
        //삭제 검증
        memberSpringDataJpaRepository.delete(member1);
        memberSpringDataJpaRepository.delete(member2);
        long deletedCount = memberSpringDataJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUserNameAndAgeGraterThan(){
        Member m1 = new Member("geun",10);
        Member m2 = new Member("geun",20);

        memberSpringDataJpaRepository.save(m1);
        memberSpringDataJpaRepository.save(m2);

        List<Member> result = memberSpringDataJpaRepository.findByUsernameAndAgeGreaterThan("geun" , 15);
        assertThat(result.get(0).getUsername()).isEqualTo("geun");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    /**
     JPA NamedQuery :실무에서 거의 사용하지 않음

     Member entity에 선언
     @NamedQuery(name="Member.searchUser",
     query="select m from Member m where username= :username"

     MemberSpringDataJpaRepository 함수 구현
     )
     **/
    @Test
    public void testNamedQuery(){
        Member m1 = new Member("geun",10);
        Member m2 = new Member("bbb",20);

        memberSpringDataJpaRepository.save(m1);
        memberSpringDataJpaRepository.save(m2);

        List<Member> result = memberSpringDataJpaRepository.searchUser("geun");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }


    /**
     @Query

     스프링 데이터 JPA를 사용하면 실무에서 Named Query를 직접 등록해서 사용하는 일은 드물다.
     대신 @Query 를 사용해서 리파지토리 메소드에 쿼리를 직접 정의한다.

     > 참고: 실무에서는 메소드 이름으로 쿼리 생성 기능은 파라미터가 증가하면 메서드 이름이 매우
     지저분해진다. 따라서 @Query 기능을 자주 사용하게 된다
     **/
    @Test
    public void testQuery(){
        Member m1 = new Member("geun",10);
        Member m2 = new Member("bbb",20);

        memberSpringDataJpaRepository.save(m1);
        memberSpringDataJpaRepository.save(m2);

        List<Member> result = memberSpringDataJpaRepository.findUser("geun",10);
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void testfindUsernameList(){
        Member m1 = new Member("geun",10);
        Member m2 = new Member("bbb",20);

        memberSpringDataJpaRepository.save(m1);
        memberSpringDataJpaRepository.save(m2);

        List<String> result = memberSpringDataJpaRepository.findUsernameList();
        for(String s : result){
            System.out.println("findName :" + s);
        }
    }

    /**
     DTO로 직접 조회
     **/
    @Test
    public void testfindMemberDto(){
        Member m1 = new Member("geun",10);
        Team team = new Team("teamA");
        teamSpringDataJpaRepository.save(team);

        m1.setTeam(team);
        memberSpringDataJpaRepository.save(m1);

        List<MemberDto> result = memberSpringDataJpaRepository.findMemberDto();
        for(MemberDto s : result){
            System.out.println("MemberDto :" + s);

            System.out.println("findName :" + s.id);
            System.out.println("findName :" + s.username);
            System.out.println("findName :" + s.temaName);
        }
    }

    /**
     컬렉션 파라미터 바인딩
     * Collection 타입으로 in절 지원
     **/
    @Test
    public void testFindMembers(){
        Member m1 = new Member("geun",10);
        Member m2 = new Member("bbb",20);

        memberSpringDataJpaRepository.save(m1);
        memberSpringDataJpaRepository.save(m2);

        List<Member> result = memberSpringDataJpaRepository.findMembers(Arrays.asList("geun","bbb"));
        for(Member m : result){
            System.out.println("Member :" + m);
        }
    }

    /**
     - 반환 타입
     스프링 데이터 JPA는 유연한 반환 타입 지원
     List<Member> findByUsername(String name); //컬렉션
     Member findByUsername(String name); //단건
     Optional<Member> findByUsername(String name); //단건 Optional
     **/
    @Test
    public void testReturnType(){
        Member m1 = new Member("ccc",10);
        Member m2 = new Member("ccc",20);

        memberSpringDataJpaRepository.save(m1);
        memberSpringDataJpaRepository.save(m2);

        List<Member> result = memberSpringDataJpaRepository.findListByUsername("ccc"); //컬렉션
        //Member member = memberSpringDataJpaRepository.findByUsername("ccc"); //단건
        //Optional<Member> optionalMember = memberSpringDataJpaRepository.findOptionalByUsername("ccc"); //단건 Optional
        //System.out.println("optionalMember :" + optionalMember);
    }


    @Test
    public void paging(){
        memberSpringDataJpaRepository.save(new Member("member1",10));
        memberSpringDataJpaRepository.save(new Member("member2",10));
        memberSpringDataJpaRepository.save(new Member("member3",10));
        memberSpringDataJpaRepository.save(new Member("member4",10));
        memberSpringDataJpaRepository.save(new Member("member5",10));

        int age = 10;
        int offset = 0;
        int limit =3;

        /********************************************************
         * Page
        *********************************************************/

        PageRequest pageRequest =PageRequest.of(0,3, Sort.by(Sort.Direction.DESC,"username"));
        //Page<Member> memberPage = memberSpringDataJpaRepository.findPageByAge(age,pageRequest);
        Page<Member> memberPage = memberSpringDataJpaRepository.upgradeFindPageByAge(age,pageRequest);


        /****************************************
         * API에 DTO로 반환 할 경우
         * Entity를 DTO로 변경하는 방법
         ***************************************/
        Page<MemberDto> toMap = memberPage.map(member -> new MemberDto(member.getId(),member.getUsername(),null));


        List<Member> content = memberPage.getContent(); 
        long totalElements = memberPage.getTotalElements(); //total count
        long pageNumber = memberPage.getNumber(); //page 번호
        long totalPage = memberPage.getTotalPages(); //전체 페이지 수
        long contentSize = content.size(); //쿼리 결과값 반환 개수
        boolean firstPage = memberPage.isFirst(); //첫번째 페이지냐
        boolean nextPage = memberPage.hasNext(); //다음 페이지가 존재하냐

        //then
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(memberPage.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(memberPage.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(memberPage.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(memberPage.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(memberPage.hasNext()).isTrue(); //다음 페이지가 있는가?


        /********************************************************
         * Slice (더보기)
         *********************************************************/
        PageRequest sliceRequest =PageRequest.of(0,3, Sort.by(Sort.Direction.DESC,"username"));
        Slice<Member> slicePage = memberSpringDataJpaRepository.findSliceByAge(age,pageRequest);

        List<Member> sliceContent = slicePage.getContent();
        //then
        assertThat(sliceContent.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(slicePage.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(slicePage.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(slicePage.hasNext()).isTrue(); //다음 페이지가 있는가?

        /********************************************************
         * List
         *********************************************************/
        PageRequest listRequest =PageRequest.of(0,3, Sort.by(Sort.Direction.DESC,"username"));
        List<Member> memberList = memberSpringDataJpaRepository.findListByAge(age,listRequest);

    }


    /**
     * 스프링 데이터 JPA를 사용한 벌크성 수정 쿼리
     */
    @Test
    public void bulkUpdate() throws Exception {
        //given
        memberSpringDataJpaRepository.save(new Member("member1", 10));
        memberSpringDataJpaRepository.save(new Member("member2", 19));
        memberSpringDataJpaRepository.save(new Member("member3", 20));
        memberSpringDataJpaRepository.save(new Member("member4", 21));
        memberSpringDataJpaRepository.save(new Member("member5", 40));

        //when
        /**
         * 스프링 데이터 JPA를 사용한 벌크성 수정 쿼리
         * 벌크 연산 이후 에는 아래와 같이 영속성 컨텍스트를 반드시 초기화 해 준다.
         */
        int resultCount = memberSpringDataJpaRepository.bulkAgePlus(20);

        /**
         * 벌크 연산 이후 에는 아래와 같이 영속성 컨텍스트를 반드시 초기화 해 준다.
         * 또는 repository에 아래와 같이 선언
         * @Modifying(clearAutomatically = true) //벌크 수정, 영속성 컨텍스트 초기화
         **/
        em.flush();
        em.clear();

        /** 벌크 연산 실행 **/
        Member result = memberSpringDataJpaRepository.findByUsername("member5");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("member =" + result);
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        //then
        assertThat(resultCount).isEqualTo(3);
    }


    @Test
    public void findMemberLazy() throws Exception {
        //given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamSpringDataJpaRepository.save(teamA);
        teamSpringDataJpaRepository.save(teamB);
        memberSpringDataJpaRepository.save(new Member("member1", 10, teamA));
        memberSpringDataJpaRepository.save(new Member("member2", 20, teamB));
        em.flush();
        em.clear();
        //when
        /******************
         * member team은 지연로딩 관계이다. 따라서 다음과 같이 team의 데이터를 조회할 때 마다 쿼리가 실행된다. (N+1 문제 발생)
         * fetch join을 활용하여 해결
         *********************************/
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

        List<Member> members = memberSpringDataJpaRepository.findAll(); //lazy join
        //List<Member> members = memberSpringDataJpaRepository.findMemberFetchJoin(); //fetch join
        //List<Member> members = memberSpringDataJpaRepository.findEntityGraphByUsername("member1"); //EntityGraph
        //then
        for (Member member : members) {
            System.out.println("member:"+member.getUsername());
            System.out.println("team:"+member.getTeam().getName());
        }
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }



    /*****************************************
     * JPA Hint & Lock
     * JPA Hint
     * JPA 쿼리 힌트(SQL 힌트가 아니라 JPA 구현체에게 제공하는 힌트)
     * return 받은 member 엔티티는 dirty checking 안됨 (즉, 수정 안됨)
     * @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
     **************************************************/
    @Test
    public void queryHint() throws Exception {
        //given
        memberSpringDataJpaRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();
        //when
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        Member member = memberSpringDataJpaRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");
        em.flush(); //Update Query 실행X
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    }

    /********************
     select for update를 위해 lock을 지원
     ******************/
    @Test
    public void lock() throws Exception {
        //given
        memberSpringDataJpaRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();
        //when
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        List<Member> member = memberSpringDataJpaRepository.findLockByUsername("member1");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    }

    /****************************************************
      MemberSpringDataJpaCustomRepository
      MemberSpringDataJpaRepositoryImpl
    
     >> 인터페이스 상속
     public interface MemberSpringDataJpaRepository extends JpaRepository<Member,Long> , MemberSpringDataJpaCustomRepository{
     *
     * 사용자 정의 리포지토리 구현
     * 스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동 생성
     * 스프링 데이터 JPA가 제공하는 인터페이스를 직접 구현하면 구현해야 하는 기능이 너무 많음
     * 다양한 이유로 인터페이스의 메서드를 직접 구현하고 싶다면?
     * JPA 직접 사용( EntityManager )
     * 스프링 JDBC Template 사용
     * MyBatis 사용
     * 데이터베이스 커넥션 직접 사용 등등...
     * Querydsl 사용
     *
     *
     * 사용자 정의 구현 클래스
     * 규칙: 리포지토리 인터페이스 이름 + Impl
     * 스프링 데이터 JPA가 인식해서 스프링 빈으로 등록
     *
     * 참고: 실무에서는 주로 QueryDSL이나 SpringJdbcTemplate을 함께 사용할 때 사용자 정의
     * 리포지토리 기능 자주 사용
     *****************************************************/
    @Test
    public void callCustom(){

        List<Member> member = memberSpringDataJpaRepository.findMemberCustom();

        /**************************************************
         스프링 JDBC Template 사용
         **************************************************/
        memberSpringDataJpaRepository.save(new Member("member1",10));
        memberSpringDataJpaRepository.save(new Member("member2",10));
        memberSpringDataJpaRepository.save(new Member("member3",10));
        memberSpringDataJpaRepository.save(new Member("member4",10));
        memberSpringDataJpaRepository.save(new Member("member5",10));

        em.flush();
        em.clear();
        /**************************************************
         스프링 JDBC Template 사용
         **************************************************/
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        List<MemberJDBCDto> findMember = memberSpringDataJpaRepository.jdbcTemplateFindMember();
        for(MemberJDBCDto m : findMember){
            System.out.println("id:"+m.getId());
            System.out.println("username:"+m.getUsername());
            System.out.println("age:"+m.getAge());
        }
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

    }

    /*************************************
     * Auditing
     * 엔티티를 생성, 변경할 때 변경한 사람과 시간을 추적하고 싶으면?
     * 등록일
     * 수정일
     * 등록자
     * 수정자
     * 순수 JPA 사용
     * 우선 등록일, 수정일 적용
     * JPA 주요 이벤트 어노테이션
     * @PrePersist, @PostPersist
     * @PreUpdate, @PostUpdate
     *************************************/
    @Test
    public void JpaEventBaseEntity() throws Exception {
        //given
        Member member = new Member("member1");
        memberSpringDataJpaRepository.save(member); //@PrePersist
        Thread.sleep(100);
        member.setUsername("member2");
        em.flush(); //@PreUpdate
        em.clear();
        //when
        Member findMember = memberSpringDataJpaRepository.findById(member.getId()).get();
        //then
        System.out.println("findMember.createdDate = " +
                findMember.getCreatedDate());
        System.out.println("findMember.updatedDate = " +
                findMember.getLastModifiedDate());
        System.out.println("findMember.getCreatedBy" +
                findMember.getCreatedBy());
        System.out.println("findMember.getLastModifiedBy" +
                findMember.getLastModifiedBy());


    }

    /*************************************
     * Projections
     * 엔티티 대신에 DTO를 편리하게 조회할 때 사용
     * 전체 엔티티가 아니라 만약 회원 이름만 딱 조회하고 싶으면?
     *
     * 주의
         * 프로젝션 대상이 root 엔티티면, JPQL SELECT 절 최적화 가능
         * 프로젝션 대상이 ROOT가 아니면
         * LEFT OUTER JOIN 처리
         * 모든 필드를 SELECT해서 엔티티로 조회한 다음에 계산
     * 정리
         * 프로젝션 대상이 root 엔티티면 유용하다.
         * 프로젝션 대상이 root 엔티티를 넘어가면 JPQL SELECT 최적화가 안된다!
         * 실무의 복잡한 쿼리를 해결하기에는 한계가 있다.
         * 실무에서는 단순할 때만 사용하고, 조금만 복잡해지면 QueryDSL을 사용하자
     *************************************/
    @Test
    public void projections() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();
        //when
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        /**  UserNameOnly interface 기반  **/
        List<UserNameOnly> result = memberSpringDataJpaRepository.findProjectionsByUsername("m1");

        for (UserNameOnly us : result){
            System.out.println("name:"+ us.getUsername());
        }

        /**  UsernameOnlyDto interface 기반  **/
        List<UsernameOnlyDto> result2 = memberSpringDataJpaRepository.findProjectionsDtoByUsername("m1");

        for (UsernameOnlyDto us : result2){
            System.out.println("name:"+ us.getUsername());
        }

        /**  중첩구조 기반 기반  **/
        List<NestedClosedProjection> result3 = memberSpringDataJpaRepository.findProjectionsNestedByUsername("m1");

        for (NestedClosedProjection us : result3){
            System.out.println("name:"+ us.getUsername());
            System.out.println("team name:"+ us.getTeam().getName());
        }

        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        //then
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

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

    @Test
    public void nativeQuery() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();
        //when

        /**  native query 기반  **/
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        Member result = memberSpringDataJpaRepository.findNativeQuery("m1");
         System.out.println("name:" + result.getUsername());
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        /**
            메소드 : findByNativeProjection
            native query + projection 기반
             DTO 반환시 사용 : 나름 괜찮아 보임
         * **/

        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        Page<MemberProjection> page = memberSpringDataJpaRepository.findByNativeProjection(PageRequest.of(0,10));

        List<MemberProjection> content = page.getContent();
        for(MemberProjection mp : content){
            System.out.println("memberProject =" + mp.getUsername());
            System.out.println("memberProject =" + mp.getTeamName());
        }
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    }
}