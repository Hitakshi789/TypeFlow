package com.typeflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping(value = {
        "/",
        "/test",
        "/leaderboard",
        "/stats",
        "/profile",
        "/about",
        "/login",
        "/register"
    })
    public String index() {
        return "forward:/index.html";
    }
}
