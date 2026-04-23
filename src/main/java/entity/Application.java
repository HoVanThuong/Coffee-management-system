package entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@IdClass(Application.ApplicationId.class)
@Entity
@Table(name = "applications")
public class Application {

    private LocalDate appliedDate;

    @Enumerated(EnumType.STRING)
    private AppStatus status;

    @Id
    @ManyToOne
    @JoinColumn(name = "candidate_id")
    @EqualsAndHashCode.Include
    private Candidate candidate;

    @Id
    @ManyToOne
    @JoinColumn(name = "job_id")
    @EqualsAndHashCode.Include
    private Job job;


    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class ApplicationId implements Serializable {
        private String candidate;
        private String job;
    }
}