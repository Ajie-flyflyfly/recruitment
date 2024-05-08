package com.iurac.recruit.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iurac.recruit.entity.ChatLink;
import com.iurac.recruit.entity.ChatList;
import com.iurac.recruit.entity.ChatMessage;
import com.iurac.recruit.entity.User;
import com.iurac.recruit.mapper.ChatLinkMapper;
import com.iurac.recruit.mapper.ChatListMapper;
import com.iurac.recruit.mapper.ChatMessageMapper;
import com.iurac.recruit.mapper.UserMapper;
import com.iurac.recruit.service.ChatService;
import com.iurac.recruit.vo.ChatLinkVo;
import com.iurac.recruit.vo.ChatListVo;
import com.iurac.recruit.vo.ChatMessageVo;
import com.iurac.recruit.vo.PageResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service("chatService")
@Transactional
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatLinkMapper chatLinkMapper;
    @Autowired
    private ChatListMapper chatListMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private UserMapper userMapper;

    //根据获取发送者和接收者的聊天关系id，判断是否为第一次聊天，如果是则添加聊天列表表
    @Override
    public void isFirstChat(String fromUserId, String toUserId) {
        // 获取发送者和接收者的聊天关系ID。
        String linkId = selectAssociation(fromUserId,toUserId);

        // 创建一个查询条件，用于查询聊天列表。
        QueryWrapper<ChatList> chatListQueryWrapper = new QueryWrapper<>();
        chatListQueryWrapper.eq("link_id",linkId).eq("from_user_id",fromUserId).eq("to_user_id",toUserId);
        // 使用ChatListMapper查询聊天列表。
        ChatList chatList = chatListMapper.selectOne(chatListQueryWrapper);

        // 如果查询结果为空，表示这是第一次聊天，需要添加聊天列表。
        if(ObjectUtil.isNull(chatList)){
            chatList = new ChatList();
            chatList.setId(IdUtil.simpleUUID());
            chatList.setLinkId(linkId);
            chatList.setFromUserId(fromUserId);
            chatList.setToUserId(toUserId);
            chatList.setFromOnline("1");
            chatList.setToOnline("0");
            chatList.setUnread(0);
            chatListMapper.insert(chatList);
        }
    }

    //获取发送者和接收者的聊天关系id
    @Override
    public String selectAssociation(String id, String toUserId) {
        // 创建一个查询条件，用于查询聊天关系。
        QueryWrapper<ChatLink> linkQueryWrapper = new QueryWrapper<>();
        linkQueryWrapper.or(wrapper -> wrapper.eq("to_user_id", id).eq("from_user_id", toUserId));
        linkQueryWrapper.or(wrapper -> wrapper.eq("to_user_id", toUserId).eq("from_user_id", id));

        // 使用ChatLinkMapper查询聊天关系。
        ChatLink chatLink = chatLinkMapper.selectOne(linkQueryWrapper);

        // 如果查询结果为空，返回null，否则返回聊天关系的ID
        return ObjectUtil.isNull(chatLink)?null:chatLink.getId();
    }

    // 保存聊天消息，并根据接收者是否在线来设置消息是否未读。
    @Override
    public void saveMessage(ChatMessage chatMessage, boolean isUnread) {
        // 创建一个更新条件，用于更新聊天消息。
        UpdateWrapper<ChatMessage> messageUpdateWrapper = new UpdateWrapper<>();
        messageUpdateWrapper.eq("link_id",chatMessage.getLinkId()).eq("is_latest","1");
        messageUpdateWrapper.set("is_latest","0");
        // 使用ChatMessageMapper更新聊天消息。
        chatMessageMapper.update(null,messageUpdateWrapper);

        // 使用ChatMessageMapper插入新的聊天消息。
        chatMessageMapper.insert(chatMessage);

        // 如果接收者不在线，将未读消息数量加1。
        if(isUnread){
            QueryWrapper<ChatList> listQueryWrapper = new QueryWrapper<>();
            listQueryWrapper.eq("from_user_id",chatMessage.getFromUserId())
                    .eq("to_user_id",chatMessage.getToUserId());
            ChatList chatList = chatListMapper.selectOne(listQueryWrapper);
            Integer unread = chatList.getUnread()+1;
            chatList.setUnread(unread);
            // 使用ChatListMapper更新聊天列表。
            chatListMapper.update(chatList,listQueryWrapper);
        }

    }

    // 当用户离线时，重置用户的在线状态。
    @Override
    public void resetWindows(String id) {
        // 创建一个更新条件，用于更新聊天列表。
        UpdateWrapper<ChatList> listUpdateWrapper1 = new UpdateWrapper<>();
        listUpdateWrapper1.eq("from_user_id",id);
        listUpdateWrapper1.set("from_online","0");
        // 使用ChatListMapper更新聊天列表。
        chatListMapper.update(null,listUpdateWrapper1);

        // 创建另一个更新条件，用于更新聊天列表。
        UpdateWrapper<ChatList> listUpdateWrapper2 = new UpdateWrapper<>();
        listUpdateWrapper2.eq("to_user_id",id);
        listUpdateWrapper2.set("to_online","0");
        // 使用ChatListMapper更新聊天列表。
        chatListMapper.update(null,listUpdateWrapper2);

    }

    // 获取用户的聊天列表。
    @Override
    public List<ChatListVo> getChatListVos(String id) {
        // 创建一个查询条件，用于查询聊天关系
        QueryWrapper<ChatLink> linkQueryWrapper = new QueryWrapper<>();
        linkQueryWrapper.eq("to_user_id", id).or().eq("from_user_id", id);
        // 使用ChatLinkMapper查询聊天关系。
        List<ChatLink> chatLinkList = chatLinkMapper.selectList(linkQueryWrapper);

        // 创建一个列表，用于存放聊天列表的视图对象。
        List<ChatListVo> chatListVos = new ArrayList<>();
        chatLinkList.forEach(chatLink ->{
            ChatListVo chatListVo = new ChatListVo();
            // 创建一个查询条件，用于查询聊天消息。
            QueryWrapper<ChatMessage> messageQueryWrapper = new QueryWrapper<>();
            messageQueryWrapper.eq("link_id",chatLink.getId()).eq("is_latest","1");
            // 使用ChatMessageMapper查询聊天消息。
            ChatMessage chatMessage = chatMessageMapper.selectOne(messageQueryWrapper);

            // 如果查询结果不为空，设置聊天列表视图对象的发送时间和内容。
            if(ObjectUtil.isNotNull(chatMessage)){
                chatListVo.setSendTime(chatMessage.getSendTime());
                chatListVo.setContent(chatMessage.getContent());
            }

            // 获取接收者的ID。
            String toUserId = id.equals(chatLink.getFromUserId())?chatLink.getToUserId():chatLink.getFromUserId();
            // 使用UserMapper查询用户
            User user = userMapper.selectById(toUserId);
            chatListVo.setToUserId(user.getId());
            chatListVo.setToUserName(user.getUsername());
            chatListVo.setToUserImg(user.getImg());
            // 将聊天列表视图对象添加到列表中。
            chatListVos.add(chatListVo);
        });
        // 对聊天列表视图对象列表进行排序。
        Collections.sort(chatListVos);
        // 返回聊天列表视图对象列表。
        return chatListVos;
    }


    // 获取聊天历史。
    @Override
    public List<ChatMessage> getChatMessageList(String linkId) {
        // 创建一个查询条件，用于查询聊天消息。
        QueryWrapper<ChatMessage> messageQueryWrapper = new QueryWrapper<>();
        messageQueryWrapper.eq("link_id",linkId).orderByAsc("send_time");
        // 使用ChatMessageMapper查询聊天消息，返回聊天消息列表。
        return chatMessageMapper.selectList(messageQueryWrapper);
    }

    // 获取当前用户的未读消息数量。
    @Override
    public List<Map<String, Object>> getUnreadById(String id) {
        return chatListMapper.getUnreadById(id);
    }

    // 判断用户是否在当前对话的聊天窗口。
    @Override
    public boolean isOnline(String id, String toUserId) {
        QueryWrapper<ChatList> listQueryWrapper = new QueryWrapper<>();
        listQueryWrapper.eq("from_user_id",id).eq("to_user_id",toUserId);
        ChatList chatList = chatListMapper.selectOne(listQueryWrapper);
        if("1".equals(chatList.getToOnline()) && toUserId.equals(chatList.getToUserId())){
            return true;
        }
        return false;
    }

    // 将用户对对方的未读消息数都置为0。
    @Override
    public void resetRead(String id, String toUserId) {
        UpdateWrapper<ChatList> listUpdateWrapper = new UpdateWrapper<>();
        listUpdateWrapper.eq("from_user_id",toUserId).eq("to_user_id",id);
        listUpdateWrapper.set("unread",0);
        chatListMapper.update(null,listUpdateWrapper);
    }

    // 用户打开了对方聊天的对话窗口，则将自身上线状态置为1。
    @Override
    public void online(String id, String toUserId) {
        UpdateWrapper<ChatList> listUpdateWrapper1 = new UpdateWrapper<>();
        listUpdateWrapper1.eq("from_user_id",toUserId).eq("to_user_id",id);
        listUpdateWrapper1.set("to_online","1");
        chatListMapper.update(null,listUpdateWrapper1);

        UpdateWrapper<ChatList> listUpdateWrapper2 = new UpdateWrapper<>();
        listUpdateWrapper1.eq("from_user_id",id).eq("to_user_id",toUserId);
        listUpdateWrapper2.set("from_online","1");
        chatListMapper.update(null,listUpdateWrapper2);
    }

    // 新建一个用户关系。
    @Override
    public void newChat(String id, String toUserId) {
        ChatLink chatLink = new ChatLink();
        chatLink.setId(IdUtil.simpleUUID());
        chatLink.setFromUserId(id);
        chatLink.setToUserId(toUserId);
        chatLink.setCreateTime(DateUtil.now());

        chatLinkMapper.insert(chatLink);
    }

    // 根据条件获取聊天关系列表。
    @Override
    public IPage<ChatLink> getChatLinkByCondition(Page<ChatLink> page, String username, String startDate, String endDate) {
        return chatLinkMapper.getChatLinkByCondition(page,username,startDate,endDate);
    }

    // 根据条件获取聊天消息列表。
    @Override
    public IPage<ChatMessage> getChatMessageByCondition(Page<ChatMessage> page, String linkId, String usernameA, String usernameB, String content, String startDate, String endDate) {
        return chatMessageMapper.getChatMessageByCondition(page,linkId,usernameA,usernameB,content,startDate,endDate);
    }
}
