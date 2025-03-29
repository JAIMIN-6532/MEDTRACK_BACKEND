package tech.duhacks.duhacks.service;

import org.springframework.stereotype.Service;
import tech.duhacks.duhacks.dto.HealthProductDto;
import tech.duhacks.duhacks.dto.MedicationScheduleDto;
import tech.duhacks.duhacks.model.HealthProduct;
import tech.duhacks.duhacks.model.MedicationSchedule;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthProductMapper {

    public HealthProductDto getHealthProductDto(HealthProduct hp){
        return new HealthProductDto(
                hp.getId(),
                hp.getName(),
                hp.getQuantity(),
                hp.getExpiryDate(),
                hp.getAmount(),
                hp.getUser().getId(),
                hp.getMedicationSchedules().stream().map(a -> a.getTime().format( DateTimeFormatter.ofPattern("HH:mm"))).collect(Collectors.toList()),
                hp.getCreatedAt().toString()
        );
    }
    public HealthProductDto mapToHealthProductDto(HealthProduct healthProduct) {
        List<MedicationScheduleDto> scheduleDtos = healthProduct.getMedicationSchedules().stream()
                .map(schedule -> MedicationScheduleDto.builder()
                        .id(schedule.getId())
                        .time(schedule.getTime().toString()) // Convert LocalTime to String
                        .build())
                .collect(Collectors.toList());

        return HealthProductDto.builder()
                .id(healthProduct.getId())
                .name(healthProduct.getName())
                .quantity(healthProduct.getQuantity())
                .expiryDate(healthProduct.getExpiryDate())
                .amount(healthProduct.getAmount())
                .userId(healthProduct.getUser().getId())
                .medicationSchedules(scheduleDtos)
                .build();
    }

}
