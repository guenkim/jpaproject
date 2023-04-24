package spring_data_jpa.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import spring_data_jpa.entitiy.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(false)
class MemberJPARepositoryTest {

    /******************************************
     * 순수 jpa repository test
     *****************************************/
    @Autowired MemberJpaRepository memberJPARepository; //스프링 데이터

    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberJPARepository.save(member);
        Member findMember = memberJPARepository.find(savedMember.getId());
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
    }
    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJPARepository.save(member1);
        memberJPARepository.save(member2);
        //단건 조회 검증
        Member findMember1 =
                memberJPARepository.findById(member1.getId()).get();
        Member findMember2 =
                memberJPARepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);
        //리스트 조회 검증
        List<Member> all = memberJPARepository.findAll();
        assertThat(all.size()).isEqualTo(2);
        //카운트 검증
        long count = memberJPARepository.count();
        assertThat(count).isEqualTo(2);
        //삭제 검증
        memberJPARepository.delete(member1);
        memberJPARepository.delete(member2);
        long deletedCount = memberJPARepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUserNameAndAgeGraterThan(){
        Member m1 = new Member("geun",10);
        Member m2 = new Member("geun",20);

        memberJPARepository.save(m1);
        memberJPARepository.save(m2);

        List<Member> result = memberJPARepository.findByUserNameAndAgeGraterThan("geun" , 15);
        assertThat(result.get(0).getUsername()).isEqualTo("geun");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    /**
     JPA NamedQuery : 실무에서 거의 사용하지 않음

     Member entity에 선언
     @NamedQuery(name="Member.searchUser",
     query="select m from Member m where username= :username"

     MemberJpaRepository 함수 구현
     )
     **/
    @Test
    public void testNamedQuery(){
        Member m1 = new Member("geun",10);
        Member m2 = new Member("bbb",20);

        memberJPARepository.save(m1);
        memberJPARepository.save(m2);

        List<Member> result = memberJPARepository.findByUsername("geun");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void paging(){
        memberJPARepository.save(new Member("member1",10));
        memberJPARepository.save(new Member("member2",10));
        memberJPARepository.save(new Member("member3",10));
        memberJPARepository.save(new Member("member4",10));
        memberJPARepository.save(new Member("member5",10));

        int age = 10;
        int offset = 0;
        int limit =3;

        List<Member> memberList = memberJPARepository.findByPage(age,offset,limit);
        Long TotalCount = memberJPARepository.totalCount(age);

        //페이지 계산 공식 적용...
        // totalPage = totalCount / size ...
        // 마지막 페이지 ...
        // 최초 페이지 ..
        //then
        assertThat(memberList.size()).isEqualTo(3);
        assertThat(TotalCount).isEqualTo(5);
    }

    /**
     * JPA를 사용한 벌크성 수정 쿼리
     */
    @Test
    public void bulkUpdate() throws Exception {
        //given
        memberJPARepository.save(new Member("member1", 10));
        memberJPARepository.save(new Member("member2", 19));
        memberJPARepository.save(new Member("member3", 20));
        memberJPARepository.save(new Member("member4", 21));
        memberJPARepository.save(new Member("member5", 40));
        //when
        int resultCount = memberJPARepository.bulkAgePlus(20);
        //then
        assertThat(resultCount).isEqualTo(3);
    }

}