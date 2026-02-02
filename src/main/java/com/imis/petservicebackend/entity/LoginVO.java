package com.imis.petservicebackend.entity;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String phone;
    private Integer role;

}
