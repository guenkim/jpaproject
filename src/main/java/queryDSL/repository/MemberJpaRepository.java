package queryDSL.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import queryDSL.dto.MemberSearchCondition;
import queryDSL.dto.MemberTeamDto;
import queryDSL.dto.QMemberTeamDto;
import queryDSL.entitiy.Member;
import queryDSL.entitiy.QMember;
import queryDSL.entitiy.QTeam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static org.thymeleaf.util.StringUtils.isEmpty;
import static queryDSL.entitiy.QMember.member;
import static queryDSL.entitiy.QTeam.team;

/*********************************
 * 순수 JPA 리포지토리와 Querydsl
 * 순수 JPA 리포지토리
 ********************************/
@Repository
public class MemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findAll_Querydsl() {
        return jpaQueryFactory
                .selectFrom(member)
                .fetch();

    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username=:username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }


    /*********
     * 동적쿼리 - Builder 사용
     * Builder를 사용한 예제
     */
 public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){

     BooleanBuilder builder = new BooleanBuilder();
     if (hasText(condition.getUsername())) {
         builder.and(member.username.eq(condition.getUsername()));
     }
     if (hasText(condition.getTeamName())) {
         builder.and(team.name.eq(condition.getTeamName()));
     }
     if (condition.getAgeGoe() != null) {
         builder.and(member.age.goe(condition.getAgeGoe()));
     }
     if (condition.getAgeLoe() != null) {
         builder.and(member.age.loe(condition.getAgeLoe()));
     }

         return
         jpaQueryFactory
         .select(new QMemberTeamDto(
                 member.id.as("memberId"),
                 member.username,
                 member.age,
                 team.id.as("teamId"),
                 team.name.as("teamName")))
         .from(member)
         .leftJoin(member.team, team)
                 .where(builder)
         .fetch();
 }

    /********************************
     * 동적 쿼리와 성능 최적화 조회 - Where절 파라미터 사용
     * Where절에 파라미터를 사용한 예제
     *********************************/

    //회원명, 팀명, 나이(ageGoe, ageLoe)
    public List<MemberTeamDto> searchWhere(MemberSearchCondition condition) {
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }
    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }
    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }

}


