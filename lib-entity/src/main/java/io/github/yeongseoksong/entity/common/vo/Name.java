package io.github.yeongseoksong.entity.common.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Name {

    @Column(name = "name", nullable = false)
    private String value;

    public Name(String value) {
        if (value == null || value.isBlank()) {
                     throw new IllegalArgumentException("이름은 필수이며 공백일 수 없습니다.");
                     }
               if (value.length() > 50) {
                         throw new IllegalArgumentException("이름은 50자를 초과할 수 없습니다.");
                     }

        this.value = value;
    }

    public static Name of(String value) {
        return new Name(value);
    }
}
