package dto;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CompanyDto implements Serializable {
    private String id;
    private String name;
    private String industry;
}