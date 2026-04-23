package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"jobs"})
@Builder


@Entity
@Table(name = "companies")
public class Company {

    @Id
    @Column(name = "company_id")
    private String id;
    private String name;
    private String industry;

    @OneToMany(mappedBy = "company")
    @JsonIgnore

    private Set<Job> jobs;
}