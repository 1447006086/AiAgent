package com.zyk.aiagent.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("body")
public class HallfController {

    @GetMapping("/body")
    public String body() {
        return "ok";
    }
}
