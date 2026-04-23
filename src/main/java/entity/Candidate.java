package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"applications", "skills"})
@Builder

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @Column(name = "cand_id")
    private String id;
    private String name;
    private String email;
    private int experience;

    @OneToMany(mappedBy = "candidate")
    @JsonIgnore
    private Set<Application> applications;

    @ManyToMany(mappedBy = "candidates")
    @JsonIgnore
    private Set<Skill> skills;
}