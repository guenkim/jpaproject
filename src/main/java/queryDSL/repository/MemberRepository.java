package queryDSL.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import queryDSL.entitiy.Member;

import java.util.List;

/******************************************
 * 스프링 데이터 리포지토리 , 사용자 정의 레포지토리(queryDSL) 상속
 *
 * /*****************************
 *  * 사용자 정의 리포지토리
 *  * 사용자 정의 리포지토리 사용법
 *  * 1. 사용자 정의 인터페이스 작성
 *  * 2. 사용자 정의 인터페이스 구현
 *  * 3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
 *  ******************************/

public interface MemberRepository extends JpaRepository<Member, Long>,
        MemberRepositoryCustom {
    List<Member> findByUsername(String username);
}