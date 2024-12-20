package org.application.bookstorage.controller;

import org.application.bookstorage.dao.PersonEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class TestController {

    @GetMapping("/index")
    public String index(){

        return "Catalog.html";
    }
}