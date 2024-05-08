package com.iurac.recruit.controller;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iurac.recruit.entity.*;
import com.iurac.recruit.exception.ManageException;
import com.iurac.recruit.security.RedisCacheManager;
import com.iurac.recruit.service.CompanyService;
import com.iurac.recruit.service.HrService;
import com.iurac.recruit.service.JobService;
import com.iurac.recruit.util.ImageUtil;
import com.iurac.recruit.util.Result;
import com.iurac.recruit.util.SaltUtil;
import com.iurac.recruit.util.TableResult;
import com.iurac.recruit.vo.EchartsVo;
import com.iurac.recruit.vo.PageResultVo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 *
 */
@Controller
public class CompanyController {

    @Autowired
    private RedisCacheManager redisCacheManager;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private HrService hrService;
    @Autowired
    private JobService jobService;

    //用户根据融资阶段和公司规模查找公司 personal/company?listed=&size=.html
    @RequestMapping("/personal/company")
    public String getCompanyByCondition(@RequestParam(value = "listed",required = false) String listedStatus,
                                        @RequestParam(value = "size",required = false) String size,
                                        Model model){
        // 使用Model参数，用于向视图传递数据
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);


        QueryWrapper<Company> queryWrapper = new QueryWrapper<>();

        // 如果listedStatus参数不为空，就将它作为查询条件
        if(!"".equals(listedStatus) && listedStatus!=null){
            queryWrapper.eq("listed_status",listedStatus);
            model.addAttribute("listed",listedStatus);
        }
        // 如果size参数不为空，就将它作为查询条件
        if(!"".equals(size) && size!=null){
            queryWrapper.eq("size",size);
            model.addAttribute("size",size);
        }
        // 使用companyService的list方法，根据查询条件获取公司列表
        List<Company> companyList = companyService.list(queryWrapper);
        // 将公司列表添加到model中，键为companyList
        model.addAttribute("companyList",companyList);
        // 返回视图名为personal/company，Spring MVC会将这个视图名解析为一个实际的视图页面
        return "personal/company";
    }

    //公司入住功能（同时用户将增加manager身份，添加到hr表中）
    @PostMapping("/company/join")
    @ResponseBody
    public Result join(Company company,
                                 @RequestParam(value="imgFile",required = false) MultipartFile file) throws Exception {

        if(StrUtil.hasEmpty(company.getName(),company.getRegistrationTime(),company.getIntroduction(),company.getListedStatus(),company.getSize())){
            return Result.fail("请输入完整信息,避免输入空格");
        }

        User user = (User) SecurityUtils.getSubject().getPrincipal();
        company.setId(IdUtil.simpleUUID());

        if(ObjectUtil.isNotNull(file) && !file.isEmpty()){
            String originalFileName = file.getOriginalFilename();
            company.setImg(ImageUtil.getFileName(originalFileName.substring(originalFileName.lastIndexOf("."))));
            ImageUtil.saveImage(file,company.getImg(),"companyIcon");
        }else {
            company.setImg("company.png");//???
        }

        companyService.join(user.getId(),company);
        redisCacheManager.getCache("authorizationCacheName").remove(user.toString());
        return Result.succ("操作成功");
    }

    //返回公司的详细信息（hr的数量、岗位的数量）company/detail/34f11160ff5c4c9481209bfff3cae983.html
    @GetMapping("/company/detail/{id}")
    // 使用@RequiresUser注解，表示这个方法需要用户登录才能访问
    @RequiresUser
    public String detail(@PathVariable("id")String id,Model model) throws Exception {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        //公司详细信息
        Company company = companyService.getById(id);
        model.addAttribute("company",company);

        //hr数量
        QueryWrapper<Hr> hrQueryWrapper = new QueryWrapper<>();
        hrQueryWrapper.eq("company_id",id);
        int hrCount = hrService.count(hrQueryWrapper);
        model.addAttribute("hrCount",hrCount);

        //岗位数量
        QueryWrapper<Job> jobQueryWrapper = new QueryWrapper<>();
        jobQueryWrapper.eq("company_id",id);
        int jobCount = jobService.count(jobQueryWrapper);
        model.addAttribute("jobCount",jobCount);

        return "personal/detail_company";
    }


    //管理员主页的企业分布echart
    @GetMapping("/company/chart")
    @RequiresRoles("admin")
    @ResponseBody
    public Map<String,Object> detail() throws Exception {
        EchartsVo companyListedStatus = companyService.getNumOfCompanyInListedStatus();
        EchartsVo companySize = companyService.getNumOfCompanyInSize();
        Map<String,Object> map = new HashMap<>();
        map.put("companyListedStatus",companyListedStatus);
        map.put("companySize",companySize);
        return map;
    }

    //管理员公司管理中的搜索公司功能 返回的是TableResult<Company>
    //admin/manage/resume.html
    @RequiresRoles("admin")
    @ResponseBody
    @GetMapping("/company/getByCondition")
    public TableResult<Company> getByCondition(@RequestParam("page")Long page, @RequestParam("limit")Long limit,
                                            @RequestParam("name")String name,
                                            @RequestParam("startDate")String startDate, @RequestParam(value = "endDate",required = false)String endDate,
                                            @RequestParam("listedStatus")String listedStatus, @RequestParam("size")String size){
        PageResultVo<Company> pageResultVo = companyService.getByCondition(page,limit,name,listedStatus,size,startDate,endDate);
        return new TableResult(0,"",pageResultVo.getTotal(),pageResultVo.getRecords());
    }

    //管理员公司管理中的编辑公司功能
    //admin/manage/resume.html
    @RequiresRoles("admin")
    @ResponseBody
    @PostMapping("/company/update/{id}")
    public Result updateCompany(@PathVariable("id")String id, @RequestParam(value = "imgFile",required = false) MultipartFile file,
                             @RequestParam("name")String name, @RequestParam("registrationTime")String registrationTime,
                             @RequestParam("listedStatus")String listedStatus, @RequestParam("size")String size, @RequestParam("introduction")String introduction) throws IOException, ManageException {
        if(StrUtil.hasEmpty(name,listedStatus,size,registrationTime,introduction)){
            return Result.fail("请输入完整信息,避免输入空格");
        }

        Company company = companyService.getById(id);
        company.setIntroduction(introduction);
        company.setListedStatus(listedStatus);
        company.setSize(size);
        company.setRegistrationTime(registrationTime);
        if(ObjectUtil.isNotNull(file) && !file.isEmpty()){
            String originalFileName = file.getOriginalFilename();
            company.setImg(ImageUtil.getFileName(originalFileName.substring(originalFileName.lastIndexOf("."))));
            ImageUtil.saveImage(file,company.getImg(),"companyIcon");
        }
        return companyService.updateById(company)?Result.succ("操作成功"):Result.fail("操作失败");
    }

    //管理员公司管理中的删除公司功能
    //admin/manage/resume.html
    @RequiresRoles("admin")
    @ResponseBody
    @PostMapping("/company/delete/{id}")
    public Result deleteCompany(@PathVariable("id")String id) throws IOException, ManageException {
        companyService.deleteCompany(id);
        return Result.succ("操作成功");
    }
}

