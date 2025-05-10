package com.example.project.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Pagina2Controller {

    @GetMapping("/pagina2")
    public String mostrarPagina2() {
        return "pagina2"; 
    }
}