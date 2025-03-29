package tech.duhacks.duhacks.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationScheduleDto {

    private Long id;
    private String time; // "08:00"
}
