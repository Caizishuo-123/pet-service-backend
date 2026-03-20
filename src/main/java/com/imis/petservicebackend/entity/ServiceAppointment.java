package com.imis.petservicebackend.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName service_appointment
 */
@TableName(value ="service_appointment")
@Data
public class ServiceAppointment {
    private Long id;

    private Long userId;

    private Long petId;

    private Long serviceId;

    private Date appointmentTime;

    private Integer status;

    private String remark;

    private Date createTime;

    private Date updateTime;
}