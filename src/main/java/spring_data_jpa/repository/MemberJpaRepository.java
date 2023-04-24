package spring_data_jpa.repository;

import org.springframework.stereotype.Repository;
import spring_data_jpa.entitiy.Member;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;


/******************************************
 * 순수 jpa repository class
 *****************************************/
@Repository
public class MemberJpaRepository {
    @PersistenceContext
    private EntityManager em;
    public Member save(Member member) {
        em.persist(member);
        return member;
    }
    public void delete(Member member) {
        em.remove(member);
    }
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }
    public long count() {
        return em.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();
    }
    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findByUserNameAndAgeGraterThan(String username , int age){
        return em.createQuery("select m from Member m where m.username=: username and m.age > : age" , Member.class)
                .setParameter("username",username)
                .setParameter("age" , age)
                .getResultList();
    }

    /**
     JPA NamedQuery : 실무에서 거의 사용하지 않음

     Member entity에 선언
     @NamedQuery(name="Member.findByUsername",
     query="select m from Member m where username= :username"
     )
     **/
    public List<Member> findByUsername(String username){
        List<Member> resultList =
                em.createNamedQuery("Member.searchUser", Member.class)
                        .setParameter("username", username)
                        .getResultList();
        return resultList;
    }

    /**
     순수 JPA 페이징과 정렬
     JPA에서 페이징을 어떻게 할 것인가?
     다음 조건으로 페이징과 정렬을 사용하는 예제 코드를 보자.
     검색 조건: 나이가 10살
     정렬 조건: 이름으로 내림차순
     페이징 조건: 첫 번째 페이지, 페이지당 보여줄 데이터는 3건
     **/
    public List<Member> findByPage(int age,int offset,int limit){
        return em.createQuery("select m from Member m where m.age = :age order by m.username desc")
                .setParameter("age" , age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long totalCount(int age){
        return em.createQuery("select count(m) from Member m where m.age=:age",Long.class)
                .setParameter("age",age)
                .getSingleResult();
    }


    /**
     * JPA를 사용한 벌크성 수정 쿼리
     */
    public int bulkAgePlus(int age) {
        int resultCount = em.createQuery(
                        "update Member m set m.age = m.age + 1" +
                                "where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
        return resultCount;
    }


}