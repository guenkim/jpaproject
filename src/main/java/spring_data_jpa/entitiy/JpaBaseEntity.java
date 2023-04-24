package spring_data_jpa.entitiy;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;


/***************************************
  - 순수 JPA 사용
 *
 * Auditing
 * 엔티티를 생성, 변경할 때 변경한 사람과 시간을 추적하고 싶으면?
 * 등록일
 * 수정일
 * 등록자
 * 수정자
 * 순수 JPA 사용
 * 우선 등록일, 수정일 적용
 *
 * JPA 주요 이벤트 어노테이션
 * @PrePersist, @PostPersist
 * @PreUpdate, @PostUpdate
 *************************************/
@MappedSuperclass
@Getter
public class JpaBaseEntity {
    @Column(updatable = false)
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }
    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
