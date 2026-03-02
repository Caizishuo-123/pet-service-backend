package com.imis.petservicebackend.entity;

import lombok.Data;

@Data
public class UserInfo {
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private String address;
}
