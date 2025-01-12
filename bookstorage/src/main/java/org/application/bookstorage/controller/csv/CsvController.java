//package org.application.bookstorage.controller.csv;
//
//import lombok.RequiredArgsConstructor;
//import org.application.bookstorage.service.csv.CsvService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/csv")
//@RequiredArgsConstructor
//public class CsvController {
//
//    private static final Logger logger = LoggerFactory.getLogger(CsvController.class);
//
//    private final CsvService csvService;
//
//    /**
//     * Эндпоинт для экспорта данных в CSV файлы.
//     * Метод: POST
//     * URL: /api/csv/export
//     */
//    @PostMapping("/export")
//    public ResponseEntity<String> exportData() {
//        try {
//            csvService.exportData();
//            return ResponseEntity.ok("Данные успешно экспортированы в CSV файлы.");
//        } catch (IOException e) {
//            logger.error("Ошибка при экспорте данных", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Ошибка при экспорте данных: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("Непредвиденная ошибка при экспорте данных", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Непредвиденная ошибка при экспорте данных: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Эндпоинт для импорта данных из CSV файлов.
//     * Метод: POST
//     * URL: /api/csv/import
//     */
//    @PostMapping("/import")
//    public ResponseEntity<String> importData() {
//        try {
//            csvService.importData();
//            return ResponseEntity.ok("Данные успешно импортированы из CSV файлов.");
//        } catch (IOException e) {
//            logger.error("Ошибка при импорте данных", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Ошибка при импорте данных: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("Непредвиденная ошибка при импорте данных", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Непредвиденная ошибка при импорте данных: " + e.getMessage());
//        }
//    }
//}
