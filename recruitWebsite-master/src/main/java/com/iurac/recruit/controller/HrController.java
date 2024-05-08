package com.iurac.recruit.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iurac.recruit.entity.*;
import com.iurac.recruit.exception.ServiceException;
import com.iurac.recruit.security.RedisCache;
import com.iurac.recruit.security.RedisCacheManager;
import com.iurac.recruit.service.HrService;
import com.iurac.recruit.service.RoleService;
import com.iurac.recruit.service.UserService;
import com.iurac.recruit.util.Result;
import com.iurac.recruit.util.TableResult;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 *
 */
@Controller
public class HrController {

    @Autowired
    private RedisCacheManager redisCacheManager;
    @Autowired
    private HrService hrService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;


    //更新HR信息 service/company/hr.html
    @PostMapping("/hr/updateInfo/{id}")
    @RequiresRoles(value = {"hr","manager","admin"},logical = Logical.OR)
    @ResponseBody
    public Result updateInfo(@PathVariable("id")String id, @RequestParam("name")String name, @RequestParam("sex")String sex, @RequestParam("age")String age){
        if(StrUtil.hasBlank(name,sex,age)){
            return Result.fail("请输入完整信息,避免输入空格");
        }

        Hr hr = new Hr();
        hr.setId(id);
        hr.setAge(age);
        hr.setName(name);
        hr.setSex(sex);

        if(!hrService.updateById(hr)){
            return Result.fail("系统错误");
        }

        return Result.succ("操作成功");
    }

    //更新员工信息 company/staffManage.html
    @PostMapping("/hr/update/{id}")
    @RequiresRoles(value = {"manager","admin"},logical = Logical.OR)
    @ResponseBody
    public Result updateInfo(@PathVariable("id")String id, @RequestParam("name")String name,
                             @RequestParam("sex")String sex, @RequestParam("age")String age,@RequestParam("position")String position){
        if(StrUtil.hasBlank(name,sex,age,position)){
            return Result.fail("请输入完整信息,避免输入空格");
        }

        Hr hr = new Hr();
        hr.setId(id);
        hr.setAge(age);
        hr.setName(name);
        hr.setSex(sex);
        hr.setPosition(position);

        if(!hrService.updateById(hr)){
            return Result.fail("系统错误");
        }

        return Result.succ("操作成功");
    }

    //根据公司id获取公司的员工信息列表
    @RequiresRoles(value = {"manager","admin"},logical = Logical.OR)
    @ResponseBody
    @GetMapping("/hr/getByConditionInCompany/{id}")
    public TableResult<Hr> getByConditionInCompany(@PathVariable("id")String id, @RequestParam("page")Long page, @RequestParam("limit")Long limit,
                                                   @RequestParam("name")String name, @RequestParam("sex")String sex, @RequestParam("age")String age, @RequestParam("position")String position){

        QueryWrapper<Hr> hrQueryWrapper = new QueryWrapper<>();
        hrQueryWrapper.like("name",name).like("age",age).like("position",position).eq("company_id",id);
        if(!StrUtil.hasBlank(sex)){
            hrQueryWrapper.eq("sex",sex);
        }

        Page<Hr> page1 = new Page<Hr>(page,limit);
        Page<Hr> hrList = hrService.page(page1,hrQueryWrapper);

        return new TableResult(0,"",hrList.getTotal(),hrList.getRecords());
    }

    //取消员工与公司的关联(以及批量取消) company/staffManage.html
    @PostMapping("/hr/unbind/{hrId}")
    @RequiresRoles(value = {"manager","admin"},logical = Logical.OR)
    @ResponseBody
    public Result unbind(@PathVariable("hrId")String hrId) throws ServiceException {
        Hr hr = hrService.getById(hrId);
        if (hr == null) {
            return Result.fail("找不到该员工");
        }
        User user = userService.getById(hr.getUserId());
        if (user == null) {
            return Result.fail("找不到该用户");
        }
        RedisCache<Object, Object> cache = (RedisCache<Object, Object>) redisCacheManager.getCache("authorizationCacheName");
        if (cache != null) {
            cache.remove(user.toString());
        }
        hrService.unbind(hrId);
        return Result.succ("操作成功");
    }

    //根据hrId关联新员工 company/staffManage.html
    @PostMapping("/hr/bind/{companyId}")
    @RequiresRoles(value = {"manager","admin"},logical = Logical.OR)
    @ResponseBody
    public Result bind(@PathVariable("companyId")String companyId,@RequestParam("username")String username,
                       @RequestParam("phone")String phone, @RequestParam("position")String position) throws ServiceException {
        hrService.bind(companyId,username,phone,position);
        return Result.succ("操作成功");
    }

}

