package spring_data_jpa.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import spring_data_jpa.dto.MemberDto;
import spring_data_jpa.dto.MemberJDBCDto;
import spring_data_jpa.entitiy.Member;

import javax.persistence.EntityManager;
import java.util.List;


/***********************************************************
 사용자 정의 리포지토리 구현
 스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동 생성
 스프링 데이터 JPA가 제공하는 인터페이스를 직접 구현하면 구현해야 하는 기능이 너무 많음
 다양한 이유로 인터페이스의 메서드를 직접 구현하고 싶다면?
 JPA 직접 사용( EntityManager )
 스프링 JDBC Template 사용
 MyBatis 사용
 데이터베이스 커넥션 직접 사용 등등...
 Querydsl 사용

 사용자 정의 구현 클래스
 규칙: 리포지토리 인터페이스 이름 + Impl
 스프링 데이터 JPA가 인식해서 스프링 빈으로 등록

 참고: 실무에서는 주로 QueryDSL이나 SpringJdbcTemplate을 함께 사용할 때 사용자 정의
 리포지토리 기능 자주 사용
 **********************************************************/
@RequiredArgsConstructor
public class MemberSpringDataJpaRepositoryImpl implements MemberSpringDataJpaCustomRepository {
    private final EntityManager em;

    /**************************************************
     스프링 JDBC Template 사용
     **************************************************/
    private final JdbcTemplate jdbcTemplate;

    /**************************************************
     아래 요소를 사용하고 싶다면 bean을 등록하여 아래 요소 기술을 사용

         JPA 직접 사용( EntityManager )
         스프링 JDBC Template 사용
         MyBatis 사용
         데이터베이스 커넥션 직접 사용 등등...
         Querydsl 사용

     참고: 실무에서는 주로 QueryDSL이나 SpringJdbcTemplate을 함께 사용할 때 사용자 정의
     리포지토리 기능 자주 사용
    ***************************************************/

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m")
                .getResultList();
    }

    /**************************************************
     스프링 JDBC Template 사용
     **************************************************/
    @Override
    public List<MemberJDBCDto> jdbcTemplateFindMember(){
        List<MemberJDBCDto> result = jdbcTemplate.query("select member_id , age, username from member", memberDtoRowMapper());
        return result;
    }

    private RowMapper<MemberJDBCDto> memberDtoRowMapper() {
        return (rs, rowNum) -> {
            MemberJDBCDto memberJDBCDto = new MemberJDBCDto(rs.getLong("member_id"),rs.getString("username"),rs.getInt("age"));
            return memberJDBCDto;
        };
    }
}
