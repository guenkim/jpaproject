package spring_data_jpa.dto;

import lombok.Data;
import spring_data_jpa.entitiy.Member;

@Data /** entity에는 @Data 어노테이션을 사용하면 안됨 , (getter ,setter 등 많은 어노테이션을 포함함) **/
public class MemberDto {
    public Long id;
    public String username;
    public String temaName;

    public MemberDto(Long id, String username, String temaName) {
        this.id = id;
        this.username = username;
        this.temaName = temaName;
    }

    public MemberDto(Member member){
        this.id = member.getId();
        this.username = member.getUsername();
    }
}
