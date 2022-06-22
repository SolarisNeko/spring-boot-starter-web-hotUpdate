package com.neko233.hotdeploy.scanner;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author SolarisNeko
 * @date 6/22/2022
 */
public class SimplePackageScannerTest {

    @Test
    public void test() throws IOException {
        List<String> allClassPathList = SimplePackageScanner.getAllClassPathList();
        allClassPathList.forEach(System.out::println);
    }
}