package queryDSL.dto;


import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberDto {
    private String username;
    private int age;
    public MemberDto() {
    }

    /***************
     * 생성자 + @QueryProjection
     * memberDTO를 Q 파일로 만들어 줌
    ***************/
    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
