package com.iurac.recruit.config;

import com.iurac.recruit.entity.User;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

@Configuration
public class WebsocketConfig extends ServerEndpointConfig.Configurator {

    // 这个方法是 ServerEndpointConfig.Configurator 类的方法，用于修改WebSocket握手过程。
    // 在这个方法中，它首先获取了当前用户的信息（使用了Shiro进行认证），然后将用户信息存储在WebSocket握手的属性中。
    // 这样，WebSocket服务器就可以在后续的通信中获取到这个用户信息。
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 获取了当前用户的信息（使用了 Shiro 进行认证），并将其存储在 WebSocket 握手的属性中。
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        sec.getUserProperties().put("userInfo", user);
    }

    // 这个方法是一个Spring的Bean定义方法，它返回一个 ServerEndpointExporter 对象。
    // ServerEndpointExporter 是Spring提供的用于注册WebSocket端点的类。
    // 当Spring容器中存在这个Bean时，Spring会自动注册所有的 @ServerEndpoint 注解的类。
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }


}
