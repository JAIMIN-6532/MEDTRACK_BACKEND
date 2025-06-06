package tech.duhacks.duhacks.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.duhacks.duhacks.dto.ProductLogDto;
import tech.duhacks.duhacks.dto.ProductLogTotalDto;
import tech.duhacks.duhacks.service.ProductService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/logs")
public class ProductLogController {

    private final ProductService productService;

    @GetMapping("/{userId}/time/{days}")
    public ResponseEntity<List<ProductLogTotalDto>> getAllLogInDays(@PathVariable("userId") Long id,@PathVariable("days")Integer days){
        System.out.println(days);
        return ResponseEntity.ok(productService.getLogForTime(id,days));
    }

    @PostMapping("/log")
    public ResponseEntity<Void> addLog(@RequestBody ProductLogDto pd){
        productService.add(pd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create")
    public ResponseEntity<List<ProductLogDto>> createProductLog(@RequestBody ProductLogDto productLogDto) {
        List<ProductLogDto> created = (List<ProductLogDto>) productService.createProductLog(productLogDto);
        return ResponseEntity.ok((List<ProductLogDto>) created);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductLogDto> updateProductLog(@PathVariable Long id,
                                                          @RequestBody ProductLogDto productLogDto) {
        ProductLogDto updated = productService.updateProductLog(id, productLogDto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/daily-dose/{id}")
    public  ResponseEntity<List<ProductLogTotalDto>> getOneDayNotificationCount(@PathVariable("id")Long id){
        return ResponseEntity.ok(productService.getOneDay(id));
    }
}
