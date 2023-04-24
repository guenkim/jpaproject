package spring_data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_data_jpa.entitiy.Team;

/******************************************
 * spring data jpa 공통 인터페이스 repository
 *****************************************/
//@Repository //생략가능 함
public interface TeamSpringDataJpaRepository  extends JpaRepository<Team,Long> {
}
