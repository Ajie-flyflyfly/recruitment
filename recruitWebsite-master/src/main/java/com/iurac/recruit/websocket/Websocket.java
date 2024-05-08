package com.iurac.recruit.websocket;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iurac.recruit.config.WebsocketConfig;
import com.iurac.recruit.entity.ChatMessage;
import com.iurac.recruit.entity.User;
import com.iurac.recruit.service.ChatService;
import com.iurac.recruit.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 这段代码定义了一个WebSocket服务器端点，用于处理与客户端的WebSocket连接、消息接收和断开连接事件。
 * WebSocket允许服务器和客户端之间进行全双工的通信，通常用于实时通信和实时更新。
 * */

//标记这是一个WebSocket的服务器端点，指定了WebSocket的路径为/message，并指定了配置器为WebsocketConfig。
@ServerEndpoint(value = "/message", configurator = WebsocketConfig.class)
@Component
public class Websocket {

    //使用CopyOnWriteArraySet存放每个客户端对应的Websocket对象，以实现线程安全的添加和删除
    private static CopyOnWriteArraySet<Websocket> websocketSet = new CopyOnWriteArraySet<>();
    //使用ConcurrentHashMap存放sessionId和对应的WebSocket Session对象，以实现线程安全的添加和删除
    private static Map<String, Session> map = new ConcurrentHashMap<>();

    //当前WebSocket连接的用户信息
    private User user;

    //用于处理聊天相关的业务逻辑的服务类
    private static ChatService chatService;
    // 使用@Autowired注解的setter方法，用于设置chatService。
    // Spring会自动调用这个方法，将ChatService的实例注入到这个类中。
    @Autowired
    public void setChatService(ChatService chatService) {
        Websocket.chatService = chatService;
    }


    // 当WebSocket连接建立时调用。获取登录时存放在HttpSession的用户数据。
    // 将sessionId和WebSocket Session进行绑定。
    // 将当前Websocket对象添加到websocketSet。打印连接建立的日志。
    @OnOpen
    public void onOpen(Session session, EndpointConfig config){
        //由WebsocketConfig配置类中的modifyHandshake方法设置的用户信息
        //获取登录时存放httpSession的用户数据
        User userInfo = (User) config.getUserProperties().get("userInfo");

        // 将sessionId和WebSocket Session进行绑定。
        map.put(userInfo.getId(),session);
        // 设置当前WebSocket连接的用户信息。
        this.user = userInfo;
        // 将当前Websocket对象添加到websocketSet。
        websocketSet.add(this);

        // 打印连接建立的日志。
        System.out.println("++++++++++++++++++++++++++++++Websocket++++++++++++++++++++++++++++++");
        System.out.println("建立连接："+user.getUsername());
        System.out.println("++++++++++++++++++++++++++++++Websocket++++++++++++++++++++++++++++++");
    }

    // 当接收到客户端发送的消息时调用。解析接收到的JSON格式的消息。
    // 封装并处理聊天消息，设置相关属性。根据接收者是否在线来判断是否需要设置消息为未读。
    // 保存聊天记录。发送消息给接收者。打印接收到的消息日志。
    @OnMessage
    public void onMessage(String message){
        // 创建一个ObjectMapper对象，用于处理JSON数据。
        ObjectMapper objectMapper = new ObjectMapper();
        // 创建一个ChatMessage对象，用于存放解析后的消息数据。
        ChatMessage chatMessage;

        try {
            //chatMessage:{content: "...", fromUserId: "xx", toUserId: "xx"}
            //将JSON格式的字符串或流转换为Java对象
            chatMessage = objectMapper.readValue(message,ChatMessage.class);
            // 设置消息的ID、发送时间和是否是最新的消息。
            chatMessage.setId(IdUtil.simpleUUID());
            chatMessage.setSendTime(DateUtil.now());
            chatMessage.setIsLatest("1");

            //查询聊天两者的联系id
            String linkId = chatService.selectAssociation(user.getId(), chatMessage.getToUserId());
            chatMessage.setLinkId(linkId);

            //封装信息
            //将Java对象转换为JSON字符串，以便将其发送到客户端或保存到数据库
            String sendMessage = objectMapper.writeValueAsString(MessageUtil.messageResult(chatMessage));

            //发送给发送者
            //Session fromSession = map.get(chatMessage.getFromUserId());
            //fromSession.getAsyncRemote().sendText(sendMessage);

            //默认对方不能直接接收，即不在线
            boolean isUnread = true;

            //发送给接收者
            Session toSession = map.get(chatMessage.getToUserId());
            //判断两者是否第一次聊天，进行聊天列表的初始化
            chatService.isFirstChat(chatMessage.getFromUserId(), chatMessage.getToUserId());
            if(ObjectUtil.isNotNull(toSession)){
                //对方在线，且在当前对话聊天的窗口则将未读置为false,因为信息一发出，对方就会立马收到
                if(chatService.isOnline(user.getId(), chatMessage.getToUserId())){
                    isUnread = false;
                }
                //对方在线，则发送信息
                //toSession：这是一个Session对象，代表了WebSocket连接的一个会话。Session对象包含了与特定客户端的WebSocket连接的信息和操作方法。
                //getAsyncRemote()：这是Session对象的一个方法，返回一个RemoteEndpoint.Async对象。RemoteEndpoint.Async用于异步地发送消息给客户端，不会阻塞当前线程。
                //sendText(sendMessage)：这是RemoteEndpoint.Async对象的sendText方法，用于向客户端发送文本消息。sendMessage是要发送的文本消息内容。
                toSession.getAsyncRemote().sendText(sendMessage);
            }

            //保存聊天记录信息
            chatService.saveMessage(chatMessage,isUnread);

            // 打印接收到的消息日志。
            System.out.println("++++++++++++++++++++++++++++++Websocket++++++++++++++++++++++++++++++");
            System.out.println("收到信息："+chatMessage.getFromUserId()+"发送给"+chatMessage.getToUserId()+":"+chatMessage.getContent());
            System.out.println("++++++++++++++++++++++++++++++Websocket++++++++++++++++++++++++++++++");

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 当WebSocket连接关闭时调用。打印连接断开的日志。
    // 重置窗口值，以及从map和websocketSet中移除当前Websocket对象。
    @OnClose
    public void onClose(){

        // 打印连接断开的日志。
        System.out.println("++++++++++++++++++++++++++++++Websocket++++++++++++++++++++++++++++++");
        System.out.println("断开连接："+user.getUsername());
        System.out.println("++++++++++++++++++++++++++++++Websocket++++++++++++++++++++++++++++++");
        // 断开连接，重置窗口值。
        chatService.resetWindows(user.getId());//断开连接，重置窗口值
        // 断开连接,提示对方自己下线。
        map.remove(user.getId());//断开连接,提示对方自己下线
        // 从set中删除当前Websocket对象。
        websocketSet.remove(this);  //从set中删除
    }
}
