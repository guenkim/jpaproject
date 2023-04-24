package queryDSL.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import queryDSL.dto.MemberSearchCondition;
import queryDSL.dto.MemberTeamDto;
import queryDSL.entitiy.Member;

import java.util.List;

/*****************************
 * 사용자 정의 리포지토리
 * 사용자 정의 리포지토리 사용법
 * 1. 사용자 정의 인터페이스 작성
 * 2. 사용자 정의 인터페이스 구현
 * 3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
 *
 * 스프링 데이터 페이징 활용1 - Querydsl 페이징 연동
 * 스프링 데이터의 Page, Pageable을 활용해보자.
 * 전체 카운트를 한번에 조회하는 단순한 방법
 * 데이터 내용과 전체 카운트를 별도로 조회하는 방법
 ******************************/
public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    /**
     * 단순한 페이징, fetchResults() 사용
     */
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
                                         Pageable pageable);

    /**
     * 복잡한 페이징
     * 데이터 조회 쿼리와, 전체 카운트 쿼리를 분리
     */
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,
                                          Pageable pageable);
}
