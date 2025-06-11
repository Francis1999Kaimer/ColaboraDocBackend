package com.example.project.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; 
    }

    @GetMapping("/admin/backup")
    public String backupAdmin() {
        return "database-backup";
    }
}
