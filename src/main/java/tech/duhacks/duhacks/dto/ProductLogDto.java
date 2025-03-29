package tech.duhacks.duhacks.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductLogDto {
    private Long id;
    private Long userId;
    private Long healthProductId;
    private List<MedicationScheduleDto> medicationScheduleIds;
    private Boolean isTaken;
}
