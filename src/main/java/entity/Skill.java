package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"jobs", "candidates"})
@Builder

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @Column(name = "skill_id")
    private String id;
    private String name;

    @ManyToMany(mappedBy = "skills")
    @JsonIgnore
    private Set<Job> jobs;

    @ManyToMany
    @JoinTable(name = "candidates_skills",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "candidate_id")
    )
    @JsonIgnore
    private Set<Candidate> candidates;
}