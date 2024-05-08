package com.iurac.recruit.controller;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iurac.recruit.entity.*;
import com.iurac.recruit.exception.ManageException;
import com.iurac.recruit.service.DicTypeService;
import com.iurac.recruit.service.DicValueService;
import com.iurac.recruit.service.RoleService;
import com.iurac.recruit.util.Result;
import com.iurac.recruit.util.TableResult;
import com.iurac.recruit.vo.PageResultVo;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 */
@Controller
public class DictionaryController {

    @Autowired
    private DicTypeService dicTypeService;
    @Autowired
    private DicValueService dicValueService;


    //获取城市列表
    //getRoleList方法：当接收到一个形如/dictionary/getCityList/{provinceId}的GET请求时，这个方法会被调用。{provinceId}是路径参数，表示省份的ID。
    // 这个方法会从ServletContext中获取键为"cityInProvince"+id的属性值，这个属性值应该是一个包含了指定省份所有城市的列表，然后将这个列表返回。
    // 返回的数据会被Spring MVC自动转换为JSON格式，并作为HTTP响应的主体发送给客户端。
    @GetMapping("/dictionary/getCityList/{provinceId}")
    @ResponseBody
    public List<City> getRoleList(@PathVariable("provinceId")Integer id, HttpServletRequest request){
        return (List<City>) request.getServletContext().getAttribute("cityInProvince"+id);
    }

    //getBusinessList方法：当接收到一个形如/dictionary/getBusinessList/{businessId}的GET请求时，这个方法会被调用。
    // {businessId}是路径参数，表示业务的ID。这个方法会从ServletContext中获取键为"businessInType"+id的属性值，这个属性值应该是一个包含了指定类型所有业务的列表，然后将这个列表返回。
    // 返回的数据会被Spring MVC自动转换为JSON格式，并作为HTTP响应的主体发送给客户端
    @GetMapping("/dictionary/getBusinessList/{businessId}")
    @ResponseBody
    public List<Business> getBusinessList(@PathVariable("businessId")String id, HttpServletRequest request){
        return (List<Business>) request.getServletContext().getAttribute("businessInType"+id);
    }

    //返回字典中的类型数据
    //返回的是TableResult
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @GetMapping("/dictionary/type/get")
    public TableResult<DicType> getDicType(@RequestParam("page")Long page, @RequestParam("limit")Long limit){

        Page<DicType> dicTypePage = new Page<>(page,limit);
        Page<DicType> pageResult = dicTypeService.page(dicTypePage, Wrappers.emptyWrapper());

        return new TableResult(0,"",pageResult.getTotal(),pageResult.getRecords());
    }

    //返回字典中的类型数据的值
    //返回的是TableResult
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @GetMapping("/dictionary/value/get")
    public TableResult<DicValue> getDicValue(@RequestParam("page")Long page, @RequestParam("limit")Long limit){

        PageResultVo<DicValue> pageResultVo = dicValueService.getDicValue(page,limit);

        return new TableResult(0,"",pageResultVo.getTotal(),pageResultVo.getRecords());
    }

    //删除字典中的类型数据（以及批量删除）
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @PostMapping("/dictionary/type/delete/{id}")
    public Result deleteType(@PathVariable("id")String id) throws ManageException {
        return dicTypeService.deleteType(id) ? Result.succ("操作成功") : Result.fail("系统错误");
    }

    // 新增字典中的数据类型
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @PostMapping("/dictionary/type/save")
    public Result saveType(DicType dicType) throws ManageException {
        if(StrUtil.hasEmpty(dicType.getType(),dicType.getDescription())){
            return Result.fail("请填写完整信息");
        }

        dicType.setId(IdUtil.simpleUUID());
        return dicTypeService.save(dicType) ? Result.succ("操作成功") : Result.fail("系统错误");
    }

    // 编辑字典中的类型数据
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @PostMapping("/dictionary/type/update/{id}")
    public Result updateType(@PathVariable("id")String id,DicType dicType){
        if(StrUtil.hasEmpty(dicType.getType(),dicType.getDescription())){
            return Result.fail("请填写完整信息");
        }
        dicType.setId(id);
        return dicTypeService.updateById(dicType) ? Result.succ("操作成功") : Result.fail("系统错误");
    }

    //删除字典中的类型数据（以及批量删除）
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @PostMapping("/dictionary/value/delete/{id}")
    public Result deleteValue(@PathVariable("id")String id) {
        return dicValueService.removeById(id) ? Result.succ("操作成功") : Result.fail("系统错误");
    }

    //返回字典中所有的类型数据
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @GetMapping("/dictionary/type/getAll")
    public List<DicType> getAllValue() {
        return dicTypeService.list();
    }

    //保存字典中的类型数据的值
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @PostMapping("/dictionary/value/save")
    public Result saveValue(DicValue dicValue) throws ManageException {
        if(StrUtil.hasEmpty(dicValue.getValue(),dicValue.getTypeId()) || dicValue.getOrderNo()<=0){
            return Result.fail("请填写完整信息");
        }

        dicValue.setId(IdUtil.simpleUUID());
        return dicValueService.saveValue(dicValue) ? Result.succ("操作成功") : Result.fail("系统错误");
    }

    //编辑字典中的类型数据的值以及优先级
    //admin/system/dictionary.html
    @ResponseBody
    @RequiresRoles("manager")
    @PostMapping("/dictionary/value/update/{id}")
    public Result updateValue(@PathVariable("id")String id,DicValue dicValue) throws ManageException {
        if(StrUtil.hasEmpty(dicValue.getValue(),dicValue.getTypeId()) || dicValue.getOrderNo()<=0){
            return Result.fail("请填写完整信息");
        }
        dicValue.setId(id);
        return dicValueService.updateValue(dicValue) ? Result.succ("操作成功") : Result.fail("系统错误");
    }

}

