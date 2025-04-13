package tech.duhacks.duhacks.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.duhacks.duhacks.dto.MedicationScheduleDto;
import tech.duhacks.duhacks.dto.ProductLogDto;
import tech.duhacks.duhacks.dto.ProductLogTotalDto;
import tech.duhacks.duhacks.exception.AuthException;
import tech.duhacks.duhacks.model.HealthProduct;
import tech.duhacks.duhacks.model.ProductLog;
import tech.duhacks.duhacks.model.User;
import tech.duhacks.duhacks.repository.HealthProductRepo;
import tech.duhacks.duhacks.repository.MedicationScheduleRepo;
import tech.duhacks.duhacks.repository.ProductLogRepo;
import tech.duhacks.duhacks.repository.UserRepo;
import tech.duhacks.duhacks.model.MedicationSchedule;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductLogRepo productRepo;
    private final UserRepo userRepo;
    private final HealthProductRepo healthProductRepo;
    private final MedicationScheduleRepo medicationScheduleRepo;
//    private final MedicationScheduleDto medicationScheduleDto;
    @Transactional
    public void add(ProductLogDto pd){
//        ZonedDateTime kolkataTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        var user = userRepo.findById(pd.getUserId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        var hp = healthProductRepo.findById(pd.getHealthProductId()).orElseThrow(()->new EntityNotFoundException("Product Not Found"));

        if(pd.getIsTaken()) {

            if (hp.getQuantity() - hp.getAmount() < 0.0f) {
                throw new AuthException("Amount is Insefisent");
            }

            healthProductRepo.updateQuantityById(hp.getId(), hp.getQuantity() - hp.getAmount());

        }

        var pl = ProductLog.builder()
                .isTaken(pd.getIsTaken())  // Setting the 'isTaken' field from the pd object
                .user(user)                // Setting the user of the log
                .healthProduct(hp)         // Setting the health product involved
                .build();                  // Building the ProductLog object

        productRepo.save(pl);
    }

    public List<ProductLogTotalDto> getLogForTime(Long userId,Integer days) {
        userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        ZonedDateTime kolkataTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

        ZonedDateTime dateFromKolkata = kolkataTime.minusDays(days);
        LocalDateTime dateFrom = dateFromKolkata.toLocalDateTime();

        var res = productRepo.findAllByUserIdAndCreatedAtIsAfter(userId, dateFrom);
        System.out.println("res:"+res);
        Map<Long, List<ProductLog>> groupedByHealthProduct = res.stream()
                .collect(Collectors.groupingBy(product -> product.getHealthProduct().getId()));

        return groupedByHealthProduct.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    List<ProductLog> logs = entry.getValue();

                    ProductLogTotalDto dto = new ProductLogTotalDto();
                    dto.setHealthProductId(productId);
                    dto.setHealthProductName(logs.getFirst().getHealthProduct().getName());

                    long takenCount = logs.stream().filter(ProductLog::getIsTaken).count();
                    dto.setIsTakenCount(takenCount);
                    dto.setMisCount(logs.size() - takenCount);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ProductLogTotalDto> getOneDay(Long userId) {
        // validate that the user exists.
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        ZonedDateTime kolkataTime = ZonedDateTime.now(zoneId);
        LocalDate today = kolkataTime.toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        LocalTime currentTime = kolkataTime.toLocalTime();

        List<HealthProduct> products = healthProductRepo.findAllByUserId(userId);
        List<ProductLogTotalDto> result = new ArrayList<>();

        for (HealthProduct product : products) {
            long takenCount = 0;
            long missedCount = 0;

            // iterate through each scheduled dose for the product.
            for (MedicationSchedule schedule : product.getMedicationSchedules()) {
                // consider only scheduled doses that are due (scheduled time is ≤ current time).
                if (currentTime.compareTo(schedule.getTime()) >= 0) {
                    // count logs with isTaken=true and isTaken=false for this schedule.
                    long countTaken = productRepo.countByUserAndHealthProductAndMedicationScheduleAndIsTakenAndCreatedAtBetween(
                            user, product, schedule, true, startOfDay, endOfDay);
                    long countMissed = productRepo.countByUserAndHealthProductAndMedicationScheduleAndIsTakenAndCreatedAtBetween(
                            user, product, schedule, false, startOfDay, endOfDay);

                    // If logs exist for this scheduled dose, add them to the counts.
                    if ((countTaken + countMissed) > 0) {
                        takenCount += countTaken;
                        missedCount += countMissed;
                    }
                    // If no log exists for that schedule, leave the counts unchanged.
                }
            }

            ProductLogTotalDto dto = ProductLogTotalDto.builder()
                    .healthProductId(product.getId())
                    .healthProductName(product.getName())
                    .isTakenCount(takenCount)
                    .MisCount(missedCount)
                    .build();
            result.add(dto);
        }
        return result;
    }


    public List<ProductLogDto> createProductLog(ProductLogDto dto) {
        // validate user and product.
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.getUserId()));
        HealthProduct product = healthProductRepo.findById(dto.getHealthProductId())
                .orElseThrow(() -> new EntityNotFoundException("Health Product not found with id: " + dto.getHealthProductId()));

        // If medication schedule IDs are empty or null, fetch schedules by health product id.
        if (dto.getMedicationScheduleIds() == null || dto.getMedicationScheduleIds().isEmpty()) {
            // Create a formatter for HH:mm time format.
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            // Query the repository for schedules associated with the given health product.
            List<MedicationSchedule> schedules = medicationScheduleRepo.findByHealthProductId(product.getId());
            if (!schedules.isEmpty()) {
                List<MedicationScheduleDto> scheduleDtos = schedules.stream()
                        .map(schedule -> {
                            MedicationScheduleDto msDto = new MedicationScheduleDto();
                            msDto.setId(schedule.getId());
                            msDto.setTime(schedule.getTime().format(formatter));
                            return msDto;
                        })
                        .collect(Collectors.toList());
                dto.setMedicationScheduleIds(scheduleDtos);
            }
        }

        // Ensure the list of schedule objects is provided.
        if (dto.getMedicationScheduleIds() == null || dto.getMedicationScheduleIds().isEmpty()) {
            throw new IllegalArgumentException("MedicationSchedule information is required for logging a dose.");
        }

        // Use Asia/Kolkata timezone and 24-hour format.
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        ZonedDateTime nowZdt = ZonedDateTime.now(zoneId);
        LocalTime currentTime = nowZdt.toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate today = nowZdt.toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // Filter the provided schedule DTOs to only include those that are due and not already logged.
        List<MedicationScheduleDto> eligible = dto.getMedicationScheduleIds().stream()
                .filter(msDto -> {
                    LocalTime schedTime = LocalTime.parse(msDto.getTime(), formatter);
                    // Only if scheduled time is due.
                    if (schedTime.isAfter(currentTime)) {
                        return false;
                    }
                    // Check if a log already exists for this schedule today.
                    Optional<MedicationSchedule> optSched = medicationScheduleRepo.findById(msDto.getId());
                    if (optSched.isEmpty()) {
                        return false;
                    }
                    MedicationSchedule schedule = optSched.get();
                    boolean exists = productRepo.existsByUserAndHealthProductAndMedicationScheduleAndCreatedAtBetween(
                            user, product, schedule, startOfDay, endOfDay);
                    return !exists;
                })
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            throw new IllegalArgumentException("No eligible scheduled dose is available for logging at current time (" + currentTime + ").");
        }

        // From eligible schedules, select the one with the earliest scheduled time.
        MedicationScheduleDto selectedDto = eligible.stream()
                .min(Comparator.comparing(msDto -> LocalTime.parse(msDto.getTime(), formatter)))
                .get();

        // Load the corresponding MedicationSchedule entity.
        MedicationSchedule schedule = medicationScheduleRepo.findById(selectedDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Medication Schedule not found with id: " + selectedDto.getId()));

        // Determine if the response is more than 5 hours after the scheduled time.
        LocalTime schedTime = LocalTime.parse(selectedDto.getTime(), formatter);
        Duration diff = Duration.between(schedTime, currentTime);
        boolean finalIsTaken = dto.getIsTaken();
        if (diff.toHours() >= 5) {
            finalIsTaken = false;
        }

        if (finalIsTaken) {
            // Decrease the quantity by 1.
            product.setQuantity(product.getQuantity() - 1);
            // Save the updated product.
            healthProductRepo.save(product);
        }

        // Create a log entry for the selected schedule.
        ProductLog productLog = ProductLog.builder()
                .user(user)
                .healthProduct(product)
                .medicationSchedule(schedule)
                .isTaken(finalIsTaken)
                .build();

        ProductLog saved = productRepo.save(productLog);
        return List.of(mapToDto(saved));  // Return as a list with one entry.
    }


    private ProductLogDto mapToDto(ProductLog productLog) {
        return ProductLogDto.builder()
                .id(productLog.getId())
                .userId(productLog.getUser().getId())
                .healthProductId(productLog.getHealthProduct().getId())
                // Wrap the associated schedule into a list of MedicationScheduleDto objects.
                .medicationScheduleIds(productLog.getMedicationSchedule() != null
                        ? List.of(MedicationScheduleDto.builder()
                        .id(productLog.getMedicationSchedule().getId())
                        .time(productLog.getMedicationSchedule().getTime().toString())
                        .build())
                        : null)
                .isTaken(productLog.getIsTaken())
                .build();
    }





    public ProductLogDto updateProductLog(Long id, ProductLogDto dto) {
        ProductLog productLog = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductLog not found with id: " + id));
        productLog.setIsTaken(dto.getIsTaken());
        ProductLog updated = productRepo.save(productLog);
        return mapToDto(updated);
    }

//    private ProductLogDto mapToDto(ProductLog productLog) {
//        return ProductLogDto.builder()
//                .id(productLog.getId())
//                .userId(productLog.getUser().getId())
//                .healthProductId(productLog.getHealthProduct().getId())
//                .isTaken(productLog.getIsTaken())
//                .build();
//    }
}
