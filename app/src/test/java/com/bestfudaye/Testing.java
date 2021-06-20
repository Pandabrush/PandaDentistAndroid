package com.bestfudaye;

import com.pandadentist.bleconnection.utils.Util;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by zhangwy on 2017/12/1 下午1:14.
 * Updated by zhangwy on 2017/12/1 下午1:14.
 * Description TODO
 */

public class Testing extends BaseTest {
    @Test
    public void getArray() {
        System.out.println(Util.array2Strings(getArray(400, 200, -10), ','));
    }
    private ArrayList<Integer> getArray(int min, int max, int interval) {
        ArrayList<Integer> array = new ArrayList<>();
        for (int item = min; item <= max; item += interval) {
            array.add(item);
        }
        return array;
    }
}
