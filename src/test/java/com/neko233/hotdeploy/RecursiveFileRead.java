package com.neko233.hotdeploy;

import com.neko233.hotdeploy.helper.ClassHelper;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

/**
 * @author SolarisNeko
 * @date 6/13/2022
 */
public class RecursiveFileRead {

    @Test
    public void jarFileRecursively() {
        Set<Class<?>> clzFromPkg = ClassHelper.getClzFromPkg("");
        for (Class<?> aClass : clzFromPkg) {
            System.out.println(aClass.getName());
        }
    }



    @Test
    public void fileRecursively() {
        Collection<File> collection = FileUtils.listFiles(new File(""), null, true);
        for (File file : collection) {
            System.out.println(file.getName());
        }
    }

}
