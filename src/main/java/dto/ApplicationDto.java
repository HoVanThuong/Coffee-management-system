package dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder

public class ApplicationDto implements Serializable {

    private LocalDate appliedDate;
    private String status;
    private String candidateId;
    private String candidateName;
    private String jobTitle;

}