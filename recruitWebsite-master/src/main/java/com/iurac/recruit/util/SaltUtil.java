package com.iurac.recruit.util;

import cn.hutool.core.util.RandomUtil;

/**
 * 此方法用于生成一个随机的盐值字符串。
 * 首先，将包含字母、数字和特殊字符的字符数组转换为字符数组。
 * 然后，随机选择字符数组中的字符，重复 n 次，将它们添加到一个 StringBuilder 中。
 * 最后，返回生成的随机盐值字符串。
 * */
public class SaltUtil {
    public static String getSalt(int n){
        char[] chars="qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM<>!@#$%^&*():?}{".toCharArray(); //转换为字符数组
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < n; i++) {
            stringBuilder.append(chars[RandomUtil.randomInt(chars.length)]);
        }

        return stringBuilder.toString();
    }
}
