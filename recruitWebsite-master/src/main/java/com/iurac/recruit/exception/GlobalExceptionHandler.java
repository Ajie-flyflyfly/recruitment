package com.iurac.recruit.exception;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.iurac.recruit.util.Result;
import org.apache.shiro.ShiroException;
import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * GlobalExceptionHandler 类是一个全局异常处理器，使用了 Spring Framework 的 @RestControllerAdvice 和 @ExceptionHandler 注解来统一处理应用中的异常。
 * 这种方式可以提高代码的可维护性和可读性，减少了在每个 Controller 或 Service 层都处理异常的重复代码
 * */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕捉shiro的异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)//设置了 HTTP 响应的状态码,返回的 HTTP 状态码为 401
    @ExceptionHandler(ShiroException.class)//当捕捉到 ShiroException 异常时执行此方法。
    public ModelAndView handle401(ShiroException e) {
        return errorModelAndView(401,"您没有权限");//返回一个 ModelAndView 对象，其中包含异常的相关信息，并设置 HTTP 状态码为 401（未授权）
    }

    //业务服务异常
    @ResponseStatus()
    @ExceptionHandler(value = ServiceException.class)//当捕捉到 ServiceException 异常时执行此方法。
    public Object handlerServiceException(HttpServletRequest request, ServiceException e){
        e.printStackTrace();
        if(isAjax(request)){
            return Result.fail(e.getMessage());//如果请求是 AJAX 请求，返回一个失败的 Result 对象，否则返回一个包含错误信息的 ModelAndView 对象。
        }else {
            return errorModelAndView(500,"请求超时");
        }
    }

    //后台管理异常
    @ResponseStatus()
    @ExceptionHandler(value = ManageException.class)//当捕捉到 ManageException 异常时执行此方法。
    public Object handlerManageException(HttpServletRequest request, ManageException e) {
        e.printStackTrace();
        if(isAjax(request)){
            return Result.fail(e.getMessage());
        }else {
            return errorModelAndView(500,"请求超时");
        }
    }

    //其它异常
    @ResponseStatus()
    @ExceptionHandler(value = Exception.class)
    public Object handlerRuntimeException(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        if(isAjax(request)){
            return Result.fail(e.getMessage());
        }else {
            return errorModelAndView(500,"请求超时");
        }
    }

    //返回一个 ModelAndView 对象，包含错误的时间戳、状态码、错误类型、错误消息，并设置视图名称为 error/4xx。
    private ModelAndView errorModelAndView(Integer code,String message) {
        ModelAndView mv = new ModelAndView();
        mv.getModel().put("timestamp", DateUtil.now());
        mv.getModel().put("status", code);
        mv.getModel().put("error","系统错误");
        mv.getModel().put("message",message);
        mv.setViewName("error/4xx");
        return mv;
    }

    //根据请求头中的 X-Requested-With 判断请求是否为 AJAX 请求。
    public static boolean isAjax(HttpServletRequest httpRequest) {
        return (ObjectUtil.isNotNull(httpRequest.getHeader("X-Requested-With"))
                && StrUtil.equals("XMLHttpRequest", httpRequest.getHeader("X-Requested-With")));
    }
}