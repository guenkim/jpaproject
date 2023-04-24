package spring_data_jpa.entitiy;


import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"}) //객체 출력용 , team은 제외 (연관관계 있는 녀석은 무한루프)

/**
 JPA NamedQuery : 실무에서 거의 사용하지 않음
 **/
@NamedQuery(name="Member.searchUser",
            query="select m from Member m where username= :username"
)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this(username, 0);
    }
    public Member(String username, int age) {
        this(username, age, null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            addTeam(team);
        }
    }
    public void addTeam(Team team) {
        this.team = team;
        team.getMembers().add(this); //team객체에 member 나 자신을 추가한다.
    }
}