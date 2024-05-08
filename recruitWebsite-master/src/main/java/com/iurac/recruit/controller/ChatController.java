package com.iurac.recruit.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iurac.recruit.entity.*;
import com.iurac.recruit.exception.ServiceException;
import com.iurac.recruit.service.ChatService;
import com.iurac.recruit.service.HrService;
import com.iurac.recruit.service.UserService;
import com.iurac.recruit.util.Result;
import com.iurac.recruit.util.TableResult;
import com.iurac.recruit.vo.ChatLinkVo;
import com.iurac.recruit.vo.ChatListVo;
import com.iurac.recruit.vo.PageResultVo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserService userService;
    @Autowired
    private HrService hrService;

    //管理员中聊天关系功能中的记录按钮的请求路径，返回的是聊天记录的详细内容（根据聊天关系id搜索）
    //admin/chat/link.html
    //记录按钮！！！
    @GetMapping({"/admin/chat/message/{linkId}"})
    @RequiresRoles(value = {"admin"})
    public String toChatMessagePage(@PathVariable(value = "linkId",required = false) String linkId, Model model){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("userInfo",user);
        model.addAttribute("linkId",linkId);

        return "admin/chat/message";
    }

    //返回聊天历史
    //setting/message.html
    @RequiresUser
    @GetMapping("/message/chat/{id}")
    @ResponseBody
    public Result chat(@PathVariable("id")String id){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        String linkId = chatService.selectAssociation(user.getId(),id);

        chatService.resetRead(user.getId(),id);
        chatService.resetWindows(linkId);
        chatService.online(user.getId(),id);

        List<ChatMessage> chatMessageList = chatService.getChatMessageList(linkId);
        Map<String, Object> map = new HashMap<>();
        map.put("chatMessageList",chatMessageList);
        map.put("toUser",userService.getById(id));

        return Result.succ("操作成功",map);
    }

    //返回聊天列表
    //setting/message.html
    @RequiresUser
    @GetMapping("/message/getChatListVoList")
    @ResponseBody
    public Result getChatListVoList(){
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        List<ChatListVo> chatListVoList = chatService.getChatListVos(user.getId());
        List<Map<String, Object>> unread = chatService.getUnreadById(user.getId());

        Map<String, Object> map = new HashMap<>();
        map.put("chatListVos",chatListVoList);
        map.put("unread",unread);
        return Result.succ("操作成功",map);
    }

    //联系hr功能
    //personal/job.htmlx
    @RequiresUser
    @GetMapping("/message/newChat/{id}")
    public String newChat(@PathVariable("id")String id) throws ServiceException {
        User user = (User) SecurityUtils.getSubject().getPrincipal();

        Hr hr = hrService.getById(id);
        if(ObjectUtil.isNull(hr)){
            throw new ServiceException("找不到该hr");
        }

        String linkId = chatService.selectAssociation(user.getId(),hr.getUserId());
        if(StrUtil.hasEmpty(linkId)){
            chatService.newChat(user.getId(),hr.getUserId());
        }

        return "redirect:/setting/message";
    }

    //管理员聊天关系中的搜索聊天关系功能（根据条件搜索） 返回的是聊天关系列表
    //admin/chat/link.html
    @RequiresRoles("manager")
    @ResponseBody
    @GetMapping("/chat/getChatLinkByCondition")
    public Result getChatLinkByCondition(@RequestParam("page")Long page, @RequestParam("limit")Long limit,
                                                          @RequestParam("username")String username,
                                                          @RequestParam("startDate")String startDate,
                                                          @RequestParam(value = "endDate",required = false)String endDate){
        IPage<ChatLink> pageResult = chatService.getChatLinkByCondition(new Page<>(page,limit),username,startDate,endDate);

        return Result.succ("",pageResult);
    }

    //管理员中聊天记录功能  返回的是聊天记录的详细内容（根据条件搜索）
    //admin/chat/link.html
    @ResponseBody
    @RequiresRoles("manager")
    @GetMapping("/chat/getChatMessageByCondition")
    public Result getChatMessageByCondition(@RequestParam(value = "linkId",required = false)String linkId,
                                                           @RequestParam("page")Long page, @RequestParam("limit")Long limit,
                                                           @RequestParam("usernameA")String usernameA, @RequestParam("usernameB")String usernameB,
                                                           @RequestParam("content")String content,
                                                           @RequestParam("startDate")String startDate,
                                                           @RequestParam(value = "endDate",required = false)String endDate){
        IPage<ChatMessage> pageResult = chatService.getChatMessageByCondition(new Page<>(page,limit),linkId,usernameA,usernameB,content,startDate,endDate);
        return Result.succ("",pageResult);
    }
}

