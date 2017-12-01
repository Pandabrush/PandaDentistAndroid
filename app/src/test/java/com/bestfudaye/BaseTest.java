package com.bestfudaye;

import org.junit.After;
import org.junit.Before;

import java.util.Locale;

/**
 * Created by zhangwy on 2017/12/1 上午11:48.
 * Updated by zhangwy on 2017/12/1 上午11:48.
 * Description
 */

public class BaseTest {
    @Before
    public void setUp() throws Exception {
        System.out.println(String.format(Locale.getDefault(), "测试类%s开始", this.getClass().getSimpleName()));
    }

    @After
    public void tearDown() throws Exception {
        System.out.println(String.format(Locale.getDefault(), "测试类%s结束", this.getClass().getSimpleName()));
    }
}
