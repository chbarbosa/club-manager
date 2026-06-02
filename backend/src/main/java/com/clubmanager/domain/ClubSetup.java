package com.clubmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "club_setup")
public class ClubSetup extends AbstractEntity {

    private String type;

    @Column(name = "json_data")
    private String jsonData;
}

