package com.neko233.hotdeploy.controller;

import com.neko233.hotdeploy.helper.SpringHotUpdateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SolarisNeko
 * @date 6/11/2022
 */
@RestController
public class HotUpdate {

    @Autowired
    private SpringHotUpdateHelper springHotUpdateHelper;

    @RequestMapping("/update")
    public String test(@RequestParam String classPath) {
        try {
            springHotUpdateHelper.registerBean(classPath);
        } catch (ClassNotFoundException e) {
            return "error. class not found = " + classPath;
        }
        return "done";
    }
}
