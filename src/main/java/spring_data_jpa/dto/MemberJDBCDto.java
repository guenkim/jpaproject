package spring_data_jpa.dto;

import lombok.Data;

@Data /** entity에는 @Data 어노테이션을 사용하면 안됨 , (getter ,setter 등 많은 어노테이션을 포함함) **/
public class MemberJDBCDto {
    public Long id;
    public String username;
    public int age;

    public MemberJDBCDto(Long id, String username, int age) {
        this.id = id;
        this.username = username;
        this.age = age;
    }
}
