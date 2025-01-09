package com.github.p3.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    @Column(name = "user_password", nullable = false)
    private String userPassword;

    @Column(name = "user_nickname", unique = true, nullable = false)
    private String userNickname;

    @Column(name = "user_phone", unique = true)
    private String userPhone;

    @Column(name = "user_created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at")
    @UpdateTimestamp
    private LocalDateTime userUpdatedAt;

    @Column(name = "user_is_deleted", nullable = false)
    private Boolean userIsDeleted = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude // 순환 참조 방지
    private RefreshToken refreshToken;

    public User(String userEmail) {
        this.userEmail = userEmail;
    }

    @OneToMany(fetch = FetchType.EAGER)
    private Set<Bid> bids;

    public boolean hasAward(Long productId) {
        return bids.stream().anyMatch(bid -> bid.getProduct().getProductId().equals(productId) && bid.getBidStatus() == BidStatus.낙찰);
    }

    // 프로필 이미지 URL 필드 추가
    private String profileImageUrl;

}