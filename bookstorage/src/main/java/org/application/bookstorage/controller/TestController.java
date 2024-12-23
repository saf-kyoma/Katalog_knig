package org.application.bookstorage.controller;

import org.application.bookstorage.dao.Author;
import org.application.bookstorage.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private AuthorService authorService;

    @GetMapping("/index")
    public ResponseEntity<List<Author>> index(){
        List<Author> allAuthors = authorService.getAllAuthors();

        return ResponseEntity.ok(allAuthors);
    }
}