package org.application.bookstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// LOGGING ADDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class BookstorageApplication {

	// LOGGING ADDED
	private static final Logger logger = LoggerFactory.getLogger(BookstorageApplication.class);

	public static void main(String[] args) {
		// LOGGING ADDED
		logger.info("Запуск приложения BookstorageApplication...");

		SpringApplication.run(BookstorageApplication.class, args);

		// LOGGING ADDED
		logger.info("Приложение BookstorageApplication успешно запущено.");
	}
}
