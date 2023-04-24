package spring_data_jpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import spring_data_jpa.dto.MemberDto;
import spring_data_jpa.entitiy.Member;
import spring_data_jpa.repository.MemberSpringDataJpaRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberSpringDataJpaRepository memberSpringDataJpaRepository;
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberSpringDataJpaRepository.findById(id).get();
        return member.getUsername();
    }

    /*********************************************************
     HTTP 요청은 회원 id 를 받지만 도메인 클래스 컨버터가 중간에 동작해서 회원 엔티티 객체를 반환
     도메인 클래스 컨버터도 리파지토리를 사용해서 엔티티를 찾음
     > 주의: 도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 이 엔티티는 단순 조회용으로만 사용해야 한다.
     (트랜잭션이 없는 범위에서 엔티티를 조회했으므로, 엔티티를 변경해도 DB에 반영되지 않는다.)

     * 그냥 사용하지 말자..........
     *********************************************************/
    @GetMapping("/members2/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    /**************************************************
     Web 확장 - 페이징과 정렬
     스프링 데이터가 제공하는 페이징과 정렬 기능을 스프링 MVC에서 편리하게 사용할 수 있다.

     요청 파라미터
     예) /members?page=0&size=3&sort=id,desc&sort=username,desc
     page: 현재 페이지, 0부터 시작한다.
     size: 한 페이지에 노출할 데이터 건수
     sort: 정렬 조건을 정의한다. 예) 정렬 속성,정렬 속성...(ASC | DESC), 정렬 방향을 변경하고 싶으면 sort
     파라미터 추가 ( asc 생략 가능)

     #글로벌 페이지 설정 (appalication.yml)
     data:
     web:
     pageable:
     default-page-size: 10 # 기본 페이지 사이즈/
     #max-page-size: 2000  # 최대 페이지 사이즈/

     개별 설정
     @PageableDefault 어노테이션을 사용

     주의 : 엔티티를 API로 노출하면 다양한 문제가 발생한다. 그래서 엔티티를 꼭 DTO로 변환해서 반환해야 한다.
     Page는 map() 을 지원해서 내부 데이터를 다른 것으로 변경할 수 있다.
     *************************************************/
    @GetMapping("/members")
    public Page<MemberDto> list(Pageable pageable) {
        Page<Member> page = memberSpringDataJpaRepository.findAll(pageable);

        // Member Entity를 DTO로 변경
        Page<MemberDto> pageDto = page.map(member -> new MemberDto(member));
        return pageDto;
    }


    /*************************************************
     Web 확장 - 페이징과 정렬
    개별 설정
    @PageableDefault 어노테이션을 사용

     주의 : 엔티티를 API로 노출하면 다양한 문제가 발생한다. 그래서 엔티티를 꼭 DTO로 변환해서 반환해야 한다.
    Page는 map() 을 지원해서 내부 데이터를 다른 것으로 변경할 수 있다.
     *************************************************/
    @RequestMapping(value = "/members_page", method = RequestMethod.GET)
    public Page<MemberDto> list2(@PageableDefault(size = 5, sort = "username",
            direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Member> page = memberSpringDataJpaRepository.findAll(pageable);

        // Member Entity를 DTO로 변경
        Page<MemberDto> pageDto = page.map(member -> new MemberDto(member));
        return pageDto;
    }

    @PostConstruct
    public void init(){
        for(int i=0; i < 100; i++){
            //memberSpringDataJpaRepository.save(new Member("userA"+i,i));
        }
    }
}
