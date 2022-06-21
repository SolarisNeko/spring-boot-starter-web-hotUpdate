package com.neko233.hotdeploy.scanner;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @title: 包扫描器
 * @description: 扫描指定 package 下的所有 Class (递归)
 * @author: SolarisNeko
 * @date: 2021/7/5
 */
public class SimplePackageScanner {

    /**
     * not suitable for dev Env, just only for prod jar
     * @return class List
     * @throws IOException
     */
    public static List<Class> getAllClasses() throws IOException {
        return getAllClassPathList().stream()
                .map(classPath -> {
                    try {
                        return Class.forName(classPath);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     *
     * @return className which can use to Class.forName
     * @throws IOException
     */
    public static List<String> getAllClassPathList() throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(".");

        List<String> fileNameList = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            File directory = new File(url.getFile());

            // recursive add class
            if (directory.isDirectory()) {
                List<String> classes = recursiveFindClasses(directory);
                int prefixLength = directory.getAbsolutePath().length();
                List<String> filterPrefixClassFileName = classes.stream()
                        // +1 for splice "/"
                        .map(fileName -> fileName.substring(prefixLength + 1))
                        // murder '.class'
                        .map(fileName -> fileName.substring(0, fileName.length() - 6))
                        .map(fileName -> fileName.replaceAll("\\/", "."))
                        .collect(Collectors.toList());
                fileNameList.addAll(filterPrefixClassFileName);
            }
        }
        return fileNameList;
    }

    /**
     *
     * @param directory
     * @return
     */
    private static List<String> recursiveFindClasses(File directory) {
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
