package com.neko233.hotdeploy.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SolarisNeko
 * @date 6/11/2022
 */
@RestController
public class A {

    @RequestMapping("/A")
    public String test() {
        return "A";
    }
}
