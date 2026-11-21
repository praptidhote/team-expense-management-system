package com.expensemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
            "/login",
            "/register",
            "/dashboard",
            "/submit-expense",
            "/my-expenses",
            "/expenses/**",
            "/manager",
            "/approvals",
            "/admin",
            "/admin/**",
            "/reports"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
