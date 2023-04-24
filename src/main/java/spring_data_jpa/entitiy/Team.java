package spring_data_jpa.entitiy;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"}) //객체 출력용 (연관관계 있는 녀석은 무한루프)
public class Team {
    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;
    @OneToMany(mappedBy = "team")
    List<Member> members = new ArrayList<>();
    public Team(String name) {
        this.name = name;
    }
}