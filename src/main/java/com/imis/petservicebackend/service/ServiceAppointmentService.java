package com.imis.petservicebackend.service;

import com.imis.petservicebackend.entity.ServiceAppointment;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author 64360
 * @description 针对表【service_appointment(服务预约表)】的数据库操作Service
 * @createDate 2026-03-02 00:00:59
 */
public interface ServiceAppointmentService extends IService<ServiceAppointment> {

  /**
   * 创建预约
   */
  boolean createAppointment(Long userId, ServiceAppointment appointment);

  /**
   * 我的预约列表（分页，带关联信息）
   */
  Page<Map<String, Object>> getMyAppointmentPage(Long userId, Integer status,
      Integer page, Integer pageSize);

  /**
   * 预约详情（带关联信息）
   */
  Map<String, Object> getAppointmentDetail(Long id);

  /**
   * 取消预约（只能取消自己的，且状态为已预约）
   */
  boolean cancelAppointment(Long userId, Long id);
}
