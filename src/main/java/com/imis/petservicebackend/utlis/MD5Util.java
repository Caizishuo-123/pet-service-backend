package com.imis.petservicebackend.utlis;

import java.security.MessageDigest;

public class MD5Util {

    /**
     * MD5 加密（32位小写）
     * @param input 明文
     * @return MD5加密后的字符串
     */
    public static String encrypt(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            // 转成16进制
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验密码
     */
    public static boolean matches(String rawPassword, String md5Password) {
        return encrypt(rawPassword).equals(md5Password);
    }

    // 测试
    public static void main(String[] args) {
        String pwd = "123456";
        String md5 = encrypt(pwd);
        System.out.println(md5);  // 输出加密后的 MD5
        System.out.println(matches("123456", md5)); // true
    }
}
