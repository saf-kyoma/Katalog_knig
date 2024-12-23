package org.application.bookstorage.controller;

import org.application.bookstorage.entity.Book;
import org.application.bookstorage.repository.BookRepository;
import org.application.bookstorage.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    // Обработчик для отображения каталога книг
    @GetMapping("/catalog")
    public String getCatalog(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        return "books"; // Имя шаблона Thymeleaf без расширения
    }

    // Другие методы (добавление, обновление, удаление книг) при необходимости
}