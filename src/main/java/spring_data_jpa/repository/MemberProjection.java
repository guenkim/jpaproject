package spring_data_jpa.repository;

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
public interface MemberProjection {

    Long getId();
    String getUsername();
    String getTeamName();
}
