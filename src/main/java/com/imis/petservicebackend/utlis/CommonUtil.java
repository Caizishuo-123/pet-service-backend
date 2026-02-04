package com.imis.petservicebackend.utlis;

import java.util.Random;

public class CommonUtil {
    /**
     * 随机获取头像路径
     */
    public static String getRandomAvatar() {
        // 头像编号 1-3
        int index = new Random().nextInt(10); // 0 ~ 9
        // 返回数据库存储的路径，前端访问即可
        return "/img/" + index + ".jpg";
    }

    /**
     * 隐藏手机号
     *
     * @param phone
     * @return String
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
