package com.neko233.hotdeploy.controller;

import com.neko233.hotdeploy.helper.SpringHotUpdateHelper;
import com.neko233.hotdeploy.scanner.SimplePackageScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author SolarisNeko
 * Date on 2022-06-21
 */
@RestController
public class TestPackageScannerController {

    @Autowired
    private SpringHotUpdateHelper springHotUpdateHelper;

    @RequestMapping("/test")
    public List<String> test() throws IOException {
        return SimplePackageScanner.getAllClassPathList();
    }

    @RequestMapping("/testClasses")
    public List<Class> testClasses() throws IOException {
        return SimplePackageScanner.getAllClasses();
    }

    @RequestMapping("/refreshAll")
    public boolean refreshAll() throws IOException {
        return springHotUpdateHelper.refreshAllUserBeanDefinition();
    }
}
