package queryDSL.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import queryDSL.dto.MemberSearchCondition;
import queryDSL.dto.MemberTeamDto;
import queryDSL.repository.MemberJpaRepository;
import queryDSL.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    /** 순수 jpa repository **/
    private final MemberJpaRepository memberJpaRepository;

    /** spring data repository **/
    private final MemberRepository memberRepository;


    /****************************************************
     *  queryDSL repository 조회 메서드 호출 api
     ***************************************************/
    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition){
        return memberJpaRepository.searchWhere(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition,
                                              Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }
    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition,
                                              Pageable pageable) {
        return memberRepository.searchPageComplex(condition, pageable);
    }
}
