package com.clubmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
@Entity
@Table(name = "field")
public class ClubField extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String location;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
