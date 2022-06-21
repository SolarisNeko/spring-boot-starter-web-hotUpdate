package com.neko233.hotdeploy;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author SolarisNeko
 * Date on 2022-06-21
 */
public class ClassLoaderTest {

    @Test
    public void getResource() throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(".");

        List<String> fileNameList = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            File directory = new File(url.getFile());

            if (directory.isDirectory()) {
                List<String> classes = recursiveFindClasses(directory);
                fileNameList.addAll(classes);
            } else {
                fileNameList.add(directory.getAbsolutePath());
            }
        }
        fileNameList.forEach(System.out::println);
    }

    private List<String> recursiveFindClasses(File directory) {
        ArrayList<String> fileNameList = new ArrayList<>();

        File[] fileArray = directory.listFiles();
        if (fileArray == null) {
            return new ArrayList<>();
        }

        for (File file : fileArray) {
            if (file.isDirectory()) {
                List<String> recursiveFileNameList = recursiveFindClasses(file);
                fileNameList.addAll(recursiveFileNameList);
            } else {
                if (file.getName().endsWith(".class")) {
                    fileNameList.add(file.getAbsolutePath());
                }
            }
        }

        return fileNameList;
    }
}
