package queryDsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.StringUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;
import queryDSL.dto.MemberDto;
import queryDSL.dto.QMemberDto;
import queryDSL.dto.UserDto;
import queryDSL.entitiy.Member;
import queryDSL.entitiy.QMember;
import queryDSL.entitiy.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static queryDSL.entitiy.QMember.member;
import static queryDSL.entitiy.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        //member1을 찾아라.
        String qlString =
                "select m from Member m " +
                        "where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        //member1을 찾아라.

        /**
         기본 Q-Type 활용
         Q클래스 인스턴스를 사용하는 2가지 방법
         QMember qMember = new QMember("m"); //별칭 직접 지정
         QMember qMember = QMember.member; //기본 인스턴스 사용
         **/
        QMember m = new QMember("m");
        QMember qMember = member;

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))//파라미터 바인딩 처리
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");

        /**
         * 기본 인스턴스를 static import와 함께 사용
         */

        findMember = queryFactory
                .select(member)  // static import
                .from(member)
                .where(member.username.eq("member1"))//파라미터 바인딩 처리
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 검색 조건 쿼리
     * 기본 검색 쿼리
     * <p>
     * 검색 조건은 .and() , . or() 를 메서드 체인으로 연결할 수 있다.
     * > 참고: select , from 을 selectFrom 으로 합칠 수 있음
     * <p>
     * member.username.eq("member1") // username = 'member1'
     * member.username.ne("member1") //username != 'member1'
     * member.username.eq("member1").not() // username != 'member1'
     * member.username.isNotNull() //이름이 is not null
     * member.age.in(10, 20) // age in (10,20)
     * member.age.notIn(10, 20) // age not in (10, 20)
     * member.age.between(10,30) //between 10, 30
     * member.age.goe(30) // age >= 30
     * member.age.gt(30) // age > 30
     * member.age.loe(30) // age <= 30
     * member.age.lt(30) // age < 30
     * member.username.like("member%") //like 검색
     * member.username.contains("member") // like ‘%member%’ 검색
     * member.username.startsWith("member") //like ‘member%’ 검색
     */
    @Test
    public void search() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    /********************************
     * AND 조건을 파라미터로 처리
     * AND 문 생성 방법
     * 1.METHOD 사용 ( AND)
     * 2. (,) 사용 >> 권장
     * where() 에 파라미터로 검색조건을 추가하면 AND 조건이 추가됨
     * 이 경우 null 값은 무시 메서드 추출을 활용해서 동적 쿼리를 깔끔하게 만들 수 있음 뒤에서 설명
     ******************************/
    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(
                        /********************************
                         * AND 조건을 파라미터로 처리
                         * AND 문 생성 방법
                         * 1.METHOD 사용 ( AND)
                         * 2. (,) 사용 >> 권장
                         * where() 에 파라미터로 검색조건을 추가하면 AND 조건이 추가됨
                         * 이 경우 null 값은 무시 메서드 추출을 활용해서 동적 쿼리를 깔끔하게 만들 수 있음 뒤에서 설명
                         ******************************/
                        //member.username.eq("member1").and(member.age.eq(10)))
                        member.username.eq("member1"),
                        (member.age.eq(10)))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /********************************
     * 결과 조회
     * fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
     * fetchOne() : 단 건 조회
     * 결과가 없으면 : null
     * 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
     * fetchFirst() : limit(1).fetchOne()
     * fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
     * fetchCount() : count 쿼리로 변경해서 count 수 조회
     ******************************/
    @Test
    public void result() {

        //List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();
        //단 건
        Member findMember1 = queryFactory
                .selectFrom(member)
                .fetchOne();
        //처음 한 건 조회
        Member findMember2 = queryFactory
                .selectFrom(member)
                .fetchFirst();
        //페이징에서 사용
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        //count 쿼리로 변경
        long count = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    /*************************
     *정렬
     *desc() , asc() : 일반 정렬
     * nullsLast() , nullsFirst() : null 데이터 순서 부여
     *
     * /**
     *  * 회원 정렬 순서
     *  * 1. 회원 나이 내림차순(desc)
     *  * 2. 회원 이름 올림차순(asc)
     *  * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     **************************/
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> list = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = list.get(0);
        Member member6 = list.get(1);
        Member memberNull = list.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    /***********
     * 페이징
     * 조회 건수 제한
     *************/
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }

    /***********
     * 페이징
     * 전체 조회 수가 필요하면?
     *
     * 주의: count 쿼리가 실행되니 성능상 주의!
     * > 참고: 실무에서 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만,
     * count 쿼리는 조인이 필요 없는 경우도 있다. 그런데 이렇게 자동화된 count 쿼리는 원본 쿼리와 같이 모두
     * 조인을 해버리기 때문에 성능이 안나올 수 있다. count 쿼리에 조인이 필요없는 성능 최적화가 필요하다면,
     * count 전용 쿼리를 별도로 작성해야 한다.
     *************/
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }


    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이
     * from Member m
     */
    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /*******************
     *GroupBy
     * groupBy(), having() 예시
     * .groupBy(item.price)
     * .having(item.price.gt(1000))
     *******************/
    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /*************************
     * 조인 - 기본 조인
     * 기본 조인
     * 조인의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고, 두 번째 파라미터에 별칭(alias)으로 사용할
     * Q 타입을 지정하면 된다.
     * join(조인 대상, 별칭으로 사용할 Q타입)
     ***********************/

    @Test
    public void join() {
        /**
         * 팀 A에 소속된 모든 회원
         */
        List<Member> list =
                queryFactory
                        .selectFrom(member)
                        .join(member.team, team)
                        .where(team.name.eq("teamA"))
                        .fetch();

        assertThat(list)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인) , 일명 막조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /******************
     조인 - on절
     ON절을 활용한 조인(JPA 2.1부터 지원)
     1. 조인 대상 필터링
     2. 연관관계 없는 엔티티 외부 조인
     ****************/

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and
     * t.name='teamA'
     */
    @Test
    public void join_on_filtering() throws Exception {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple t : result) {
            System.out.println("tuple :" + t);
        }
    }

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     * <p>
     * 주의! 문법을 잘 봐야 한다. leftJoin() 부분에 일반 조인과 다르게 엔티티 하나만 들어간다.
     * 일반조인: leftJoin(member.team, team)
     * on조인: from(member).leftJoin(team).on(xxx)
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))  //막 조인인 경우 join 함수 내에 조인 할 대상 테이블을 명시
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }

    /***************************
     * 조인 - 페치 조인
     * 페치 조인은 SQL에서 제공하는 기능은 아니다. SQL조인을 활용해서 연관된 엔티티를 SQL 한번에
     * 조회하는 기능이다. 주로 성능 최적화에 사용하는 방법이다.
     * 페치 조인 미적용
     * 지연로딩으로 Member, Team SQL 쿼리 각각 실행
     **************************/

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    /***************************
     * 조인 - 페치 조인
     * 페치 조인은 SQL에서 제공하는 기능은 아니다. SQL조인을 활용해서 연관된 엔티티를 SQL 한번에
     * 조회하는 기능이다. 주로 성능 최적화에 사용하는 방법이다.
     즉시로딩으로 Member, Team SQL 쿼리 조인으로 한번에 조회
     **************************/
    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                //.join(member.team, team) //지연
                .join(member.team, team).fetchJoin() //fetch join 설정
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**********************
     서브 쿼리
     com.querydsl.jpa.JPAExpressions 사용
     ***********************/
    /**
     * 나이가 가장 많은 회원 조회
     *
     *    * from 절의 서브쿼리 한계
     *          * JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl
     *          * 도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도
     *          * 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.
     *      * from 절의 서브쿼리 해결방안
     *          * 1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
     *          * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
     *          * 3. nativeSQL을 사용한다.
     */
    @Test
    public void subQuery() throws Exception {
        //subquery용 QMember 추가
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

     /**
     * 나이가 평균 나이 이상인 회원
     *
     * from 절의 서브쿼리 한계
         * JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl
         * 도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도
         * 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.
     * from 절의 서브쿼리 해결방안
         * 1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
         * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
         * 3. nativeSQL을 사용한다.
     */
    @Test
    public void subQueryGoe() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(30,40);
    }

    /**
     * select 절에 subquery
     *
     * from 절의 서브쿼리 한계
         * JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl
         * 도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도
         * 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.
     * from 절의 서브쿼리 해결방안
         * 1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
         * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
         * 3. nativeSQL을 사용한다.
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }


    /**
     * select 절에 subquery
     *
     * from 절의 서브쿼리 한계
         * JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl
         * 도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도
         * 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.
     * from 절의 서브쿼리 해결방안
         * 1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
         * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
         * 3. nativeSQL을 사용한다.
     */
    @Test
    public void subSelectQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> fetch = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }
    }

    /*****************************************
     * Case 문
     * select, 조건절(where), order by에서 사용 가능
     * 간단한 case문
     ****************************************/
    @Test
    public void simpleCase(){
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : result){
            System.out.println("s=" + s);
        }
    }


    /*****************************************
     * Case 문
     * select, 조건절(where), order by에서 사용 가능
     * 복잡한 case문
     ****************************************/
    @Test
    public void complexCase(){
        List<String> result = queryFactory
                .select(new CaseBuilder() //복잡한 케이스 문 일 때 사용
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result){
            System.out.println("s=" + s);
        }
    }

    /*************************
     * orderBy에서 Case 문 함께 사용하기 예제
     * 예를 들어서 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
     * 1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력
     * 2. 0 ~ 20살 회원 출력
     * 3. 21 ~ 30살 회원 출력
     ***********************/

    @Test
    public void orderByCase(){
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }
    }

        /**  ***********************
         * 상수, 문자 더하기
         *
         * 참고: member.age.stringValue() 부분이 중요한데, 문자가 아닌 다른 타입들은 stringValue() 로
         * 문자로 변환할 수 있다. 이 방법은 ENUM을 처리할 때도 자주 사용한다.
         **************************/
        @Test
        public void plusCharacterConstant(){
            
            // 상수 더하기
            List<Tuple> result = queryFactory
                    .select(member.username, Expressions.constant("A"))
                    .from(member)
                    .fetch();

            for (Tuple t : result){
                System.out.println("tuple =" +t);
            }
            
            //문자 더하기
            String sresult = queryFactory
                    .select(member.username.concat("_").concat(member.age.stringValue()))
                    .from(member)
                    .where(member.username.eq("member1"))
                    .fetchOne();

            System.out.println("sresult =" +sresult);
        }

    /*************************
     * 프로젝션과 결과 반환 - 기본
     * 프로젝션: select 대상 지정
     *************************/
    @Test
    public void projections(){
        //project 대상이 하나
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        System.out.println("list :" + result);


        //project 대상이 하나
        List<Member> result2 = queryFactory
                .select(member)
                .from(member)
                .fetch();
        System.out.println("list :" + result2);



    }

    /*****************************
     프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회
     튜플 조회
     프로젝션 대상이 둘 이상일 때 사용
     
     *DTO로 조회 하는 것을 권장
     ******************************/
    @Test
    public void tupleProjections(){

        List<Tuple> tupleResult = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : tupleResult) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username=" + username);
            System.out.println("age=" + age);
        }

    }

    /*****************************
     프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회
     튜플 조회
     프로젝션 대상이 둘 이상일 때 사용

     *DTO로 조회 하는 것을 권장
     ******************************/
    @Test
    public void dtoProjections(){
        /*********************************
         * 순수 JPA에서 DTO 조회 코드
         ********************************/
        List<MemberDto> result = em.createQuery(
                        "select new queryDSL.dto.MemberDto(m.username, m.age) " +
                                "from Member m", MemberDto.class)
                .getResultList();

        System.out.println("List<MemberDto>=" + result);

        /*********************************
        Querydsl 빈 생성(Bean population)
        결과를 DTO 반환할 때 사용
        다음 3가지 방법 지원
            1.프로퍼티 접근 : setter,getter 필요
            2.필드 직접 접근 : 필드 선언 필요
            3.생성자 사용 : 생성자 선언 필요
         ********************************/

        // 1.프로퍼티 접근
        List<MemberDto> result2 = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        System.out.println("List<MemberDto>=" + result2);

        // 2.필드 직접 접근
        List<MemberDto> result3 = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        System.out.println("List<MemberDto>=" + result3);

        //3.생성자 사용
        List<MemberDto> result4 = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        System.out.println("List<MemberDto>=" + result4);


        /********************
         * 프로퍼티나, 필드 접근 생성 방식에서 이름이 다를 때 해결 방안
         * ExpressionUtils.as(source,alias) : 필드나, 서브 쿼리에 별칭 적용
         * username.as("memberName") : 필드에 별칭 적용
         *******************/
        QMember memberSub = new QMember("memberSub");
        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                                member.username.as("name"),
                                ExpressionUtils.as(
                                        JPAExpressions
                                                .select(memberSub.age.max())
                                                .from(memberSub), "age")
                        )
                ).from(member)
                .fetch();
        System.out.println("List<UserDto>=" + fetch);

    }


    /*****************************
     * @QueryProjection 활용
     * 생성자에 @QueryProjection 선언해야 함
     * 이 방법은 컴파일러로 타입을 체크할 수 있으므로 가장 안전한 방법이다.
     * 다만 DTO에 QueryDSL 어노테이션을 유지해야 하는 점과 DTO까지 Q 파일을 생성해야 하는 단점이 있다.
     ******************************/
    @Test
    public void dtoQueryProjections() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for(MemberDto dto : result){
            System.out.println("memberDTO = "  + dto);
        }
    }

    /*****************************
     * 동적 쿼리를 해결하는 두가지 방식
     * BooleanBuilder
     * Where 다중 파라미터 사용
     *
     * BooleanBuilder 사용
     ******************************/

    @Test
    public void 동적쿼리_BooleanBuilder() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember1(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    /*****************************
     * 동적 쿼리를 해결하는 두가지 방식
     * BooleanBuilder
     * Where 다중 파라미터 사용
     *
     * where 조건에 null 값은 무시된다.
     * 메서드를 다른 쿼리에서도 재활용 할 수 있다.
     * 쿼리 자체의 가독성이 높아진다.
     ******************************/

    @Test
    public void 동적쿼리_WhereParam() throws Exception {
        //String usernameParam = "member1";
        String usernameParam = null;
        Integer ageParam = 0;
        List<Member> result = searchMember2(usernameParam, ageParam);
        //Assertions.assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameCond , ageCond))
                .fetch();
    }
    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }
    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanBuilder allEq(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();


        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.or(member.age.eq(ageCond));
        }
        return builder;
    }

    /*************************
     * 수정, 삭제 벌크 연산
     * 쿼리 한번으로 대량 데이터 수정
     *
     * 주의: JPQL 배치와 마찬가지로, 영속성 컨텍스트에 있는 엔티티를 무시하고 실행되기 때문에 배치 쿼리를
     * 실행하고 나면 영속성 컨텍스트를 초기화 하는 것이 안전하다
     *********************************/
    @Test
    //@Commit
    public void bulkUpdate(){
        long count = queryFactory
                .update(member)
                .set(member.username,"비회원")
                .where(member.age.lt(28))
                .execute();

        /***********************
         * 꼭 주의하자.
         * 대량 데이터 수정이후 에는 영속성 컨텍스 아래와 같이 초기화 시켜준다.
         ***********************/
        em.flush();
        em.clear();

        List<Member> result =
                queryFactory
                        .selectFrom(member)
                        .fetch();

        System.out.println("member :" + result);
    }

    /*************************
     * 대량 더하기
     * *******************/
    @Test
    //@Commit
    public void bulkAdd(){

        long count = queryFactory
                .update(member)
                .set(member.age,member.age.add(1))  //더하기
                .set(member.age,member.age.multiply(2)) //곱하기
                .execute();

        em.flush();
        em.clear();

    }

    /*************************
     * 대량 곱하기
     * *******************/
    @Test
    //@Commit
    public void bulkMultiply(){
        long count = queryFactory
                .update(member)
                .set(member.age,member.age.multiply(2)) //곱하기
                .execute();

        em.flush();
        em.clear();

    }

    /*************************
     * 쿼리 한번으로 대량 데이터 삭제
     * *******************/
    @Test
    public void bulkDelete(){
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

        em.flush();
        em.clear();
    }

    /****************************
     * SQL function 호출하기
     * SQL function은 JPA와 같이 Dialect에 등록된 내용만 호출할 수 있다.
     * member M으로 변경하는 replace 함수 사용
     ***************************/

    @Test
    public void dbMethod(){
        String result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.username, "member", "M"))
                .from(member)
                .fetchFirst();

        queryFactory.select(member.username)
                .from(member)
                .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
                .fetchFirst();

    }


}