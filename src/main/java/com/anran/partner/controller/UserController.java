package com.anran.partner.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {



    @RequestMapping("/register")
    public String userRegister() {

        return "ok";
    }


    @RequestMapping("/login")
    public String userLogin() {
        return "ok";
    }
}
