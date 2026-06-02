package com.clubmanager.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "club")
public class Club extends AbstractEntity {

    private String name;
    private String description;
    private String colour1;
    private String colour2;
}

