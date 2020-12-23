package br.com.djeffersonx.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table
@Entity
public class Person {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "", sequenceName = "SQ_BATCH_ENTITY", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public Person() {

    }

    public Person(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}