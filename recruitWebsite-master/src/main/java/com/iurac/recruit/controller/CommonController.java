package com.iurac.recruit.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iurac.recruit.entity.*;
import com.iurac.recruit.service.*;
import com.iurac.recruit.vo.ChatListVo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 返回前端请求的页面和页面所需的数据
 * */
@Controller
public class CommonController {

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private HrService hrService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private UserService userService;
    @Autowired
    private JobService jobService;
    @Autowired
    private ChatService chatService;

    //返回userInfo（admin）
    //admin/**
    @GetMapping({"/admin/{page}"})
    @RequiresRoles(value = {"admin"})
    public String toPage(@PathVariable("page") String page, Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        String nextPage = "admin/"+page;
        return nextPage;
    }

    //返回userInfo（admin）
    //admin/manage/**
    @GetMapping({"/admin/manage/{page}"})
    @RequiresRoles(value = {"admin"})
    public String toManagePage(@PathVariable("page") String page, Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        String nextPage = "admin/manage/"+page;
        return nextPage;
    }

    //返回userInfo（admin）
    //admin/system/**
    //admin/system/dictionary
    @GetMapping({"/admin/system/{page}"})
    @RequiresRoles(value = {"admin"})
    public String toDictionaryPage(@PathVariable("page") String page, Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        String nextPage = "admin/system/"+page;
        return nextPage;
    }

    //返回userInfo（admin）
    //admin/chat/**
    @GetMapping({"/admin/chat/{page}"})
    @RequiresRoles(value = {"admin"})
    public String toChatPage(@PathVariable("page") String page, Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        String nextPage = "admin/chat/"+page;
        return nextPage;
    }

    //返回userInfo到职位、公司页面
    //personal/job.html
    @RequiresRoles(value = {"user","hr","admin"},logical = Logical.OR)
    @GetMapping({"/personal/{page}"})
    public String toPersonalPage(@PathVariable("page") String page, Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        String nextPage = "personal/"+page;
        return nextPage;
    }

    //返回userInfo
    //setting/information
    @RequiresUser
    @GetMapping({"/setting/information"})
    public String toInformationPage(Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        return "setting/information";
    }

    //返回userInfo
    //setting/password
    @RequiresUser
    @GetMapping({"/setting/password"})
    public String toPasswordPage(Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        return "setting/password";
    }

    //返回用户信息（userInfo）、用户的简历的信息（resumeInfo）
    // service/resume.html
    @RequiresRoles(value = {"user","admin"},logical = Logical.OR)
    @GetMapping({"/service/resume"})
    public String toResumePage(Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId());
        Resume resume = resumeService.getOne(queryWrapper);
        model.addAttribute("resumeInfo", resume);

        return "service/resume";
    }

    //返回用户、hr信息（userInfo、hrInfo）
    // service/company/**.html
    @RequiresRoles(value = {"hr","manager","admin"},logical = Logical.OR)
    @GetMapping({"/service/company/{page}"})
    public String toHrPage(@PathVariable("page")String page, Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        QueryWrapper<Hr> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId());
        Hr hr = hrService.getOne(queryWrapper);
        //hr未绑定相关公司
        if(ObjectUtil.isNull(hr)){
            return "service/company/join";
        }else{
            model.addAttribute("hrInfo", hr);
            return "service/company/"+page;
        }

    }

    //返回用户、hr、公司的相关信息（userInfo、hrInfo、companyInfo）
    // service/company/info.html
    @RequiresRoles(value = {"manager","admin"},logical = Logical.OR)
    @GetMapping({"/service/company/info"})
    public String toHrPage(Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        QueryWrapper<Hr> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("user_id",user.getId());
        Hr hr = hrService.getOne(queryWrapper1);
        model.addAttribute("hrInfo", hr);

        QueryWrapper<Company> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("id",hr.getCompanyId());
        Company company = companyService.getOne(queryWrapper2);
        model.addAttribute("companyInfo", company);
        return "service/company/info";
    }

    //返回用户、hr的相关信息（userInfo、hrInfo）
    // service/company/staffManage.html
    @RequiresRoles(value = {"manager","admin"},logical = Logical.OR)
    @GetMapping({"/service/company/staffManage"})
    public String toStaffManagePage(Model model) throws Exception {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        QueryWrapper<Hr> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId());
        Hr hr = hrService.getOne(queryWrapper);
        if (Objects.isNull(hr)){
            throw new Exception("该用户没有绑定公司");
        }
        model.addAttribute("hrInfo", hr);

        return "service/company/staffManage";
    }


    //返回用户相关信息（userInfo）到setting/message.html
    @RequiresUser
    @GetMapping({"/setting/message"})
    public String toMessagePage(Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        return "setting/message";
    }

    //返回管理员主页的数据统计所需的数据
    //admin/index.html
    @GetMapping({"/admin/index"})
    @RequiresRoles(value = {"admin"})
    public String toDictionaryPage(Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);

        int userCount = userService.count();
        int resumeCount = resumeService.count();
        int hrCount = hrService.count();
        int companyCount = companyService.count();
        int jobCount = jobService.count();
        QueryWrapper<Job> jobQueryWrapper = new QueryWrapper<>();
        jobQueryWrapper.select("distinct area");
        int cityCount = jobService.count(jobQueryWrapper);

        Map<String,Integer> map = new HashMap<>();
        model.addAttribute("userCount",userCount);
        model.addAttribute("resumeCount",resumeCount);
        model.addAttribute("hrCount",hrCount);
        model.addAttribute("companyCount",companyCount);
        model.addAttribute("jobCount",jobCount);
        model.addAttribute("cityCount",cityCount);

        return "admin/index";
    }
}
