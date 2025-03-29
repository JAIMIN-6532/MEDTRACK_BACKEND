package tech.duhacks.duhacks.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProductDto{
        Long id;
        String name;
        Float quantity;
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expiryDate;

        public HealthProductDto(Long id, String name, Float quantity, LocalDate expiryDate, Float amount, Long userId, List<String> times, String createdAt) {
                this.id = id;
                this.name = name;
                this.quantity = quantity;
                this.expiryDate = expiryDate;
                this.amount = amount;
                this.userId = userId;
                this.times = times;
                this.createdAt = createdAt;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getCreatedAt() {
                return createdAt;
        }

        public void setCreatedAt(String createdAt) {
                this.createdAt = createdAt;
        }

        public LocalDate getExpiryDate() {
                return expiryDate;
        }

        public void setExpiryDate(LocalDate expiryDate) {
                this.expiryDate = expiryDate;
        }

        public Float getQuantity() {
                return quantity;
        }

        public void setQuantity(Float quantity) {
                this.quantity = quantity;
        }

        public Float getAmount() {
                return amount;
        }

        public void setAmount(Float amount) {
                this.amount = amount;
        }

        public List<String> getTimes() {
                return times;
        }

        public void setTimes(List<String> times) {
                this.times = times;
        }

        public Long getUserId() {
                return userId;
        }

        public void setUserId(Long userId) {
                this.userId = userId;
        }

        Float amount;
        Long userId;
        List<String> times;
        private List<MedicationScheduleDto> medicationSchedules;
        String createdAt;
}
