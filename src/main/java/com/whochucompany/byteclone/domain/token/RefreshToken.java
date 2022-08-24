package com.whochucompany.byteclone.domain.token;

import com.whochucompany.byteclone.domain.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RefreshToken {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String refreshToken;

    public void updateTokenValue(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
