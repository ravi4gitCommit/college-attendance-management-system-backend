
package com.attendance.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "semesters")
@Getter
@Setter
public class Semester {

    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Column(name = "number", nullable = false)
    private Short number;

    @Column(name = "name", nullable = false)
    private String name;
}