package com.imis.petservicebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.entity.PetServiceEntity;
import com.imis.petservicebackend.service.PetServiceQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/petService")
@Slf4j
public class PetServiceController {

  @Autowired
  private PetServiceQueryService petServiceQueryService;

  /**
   * 分页查询服务列表（游客可访问，仅展示启用的服务）
   */
  @GetMapping("/page")
  public Result<?> getServicePage(
      @RequestParam(required = false) Integer type,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) java.math.BigDecimal minPrice,
      @RequestParam(required = false) java.math.BigDecimal maxPrice,
      @RequestParam(required = false) String sort,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<PetServiceEntity> pageInfo = petServiceQueryService.getServicePage(type, keyword, minPrice,
        maxPrice, sort, page, pageSize);
    return Result.success(pageInfo);
  }

  /**
   * 服务详情（游客可访问）
   */
  @GetMapping("/detail/{id}")
  public Result<?> getServiceById(@PathVariable Long id) {
    PetServiceEntity service = petServiceQueryService.getById(id);
    if (service == null) {
      return Result.fail("服务不存在");
    }
    return Result.success(service);
  }

  /**
   * 获取所有启用的服务（不分页，供预约页面下拉选择）
   */
  @GetMapping("/list")
  public Result<?> getEnabledServices() {
    List<PetServiceEntity> list = petServiceQueryService.getEnabledServiceList();
    return Result.success(list);
  }
}
