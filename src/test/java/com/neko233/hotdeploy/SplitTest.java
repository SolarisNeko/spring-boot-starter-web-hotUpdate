package com.neko233.hotdeploy;

import com.neko233.hotdeploy.util.SplitUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author SolarisNeko
 * Date on 2022-06-21
 */
public class SplitTest {

    @Test
    public void splitNonComma() {
        String demo = "com";
        String result = SplitUtil.getBeanNameByClassName(demo);
        Assertions.assertEquals("com", result);
    }

    @Test
    public void splitOneMoreComma() {
        String demo = "com.neko233.App";
        String result = SplitUtil.getBeanNameByClassName(demo);
        Assertions.assertEquals("App", result);
    }
}
