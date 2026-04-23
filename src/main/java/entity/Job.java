package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"company", "skills", "applications"})
@Builder

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @Column(name = "job_id")
    private String id;
    private String title;
    private String description;
    private double salary;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

    @ManyToMany
    @JoinTable(name = "jobs_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @JsonIgnore
    private Set<Skill> skills;

    @OneToMany(mappedBy = "job")
    @JsonIgnore
    private Set<Application> applications;
}