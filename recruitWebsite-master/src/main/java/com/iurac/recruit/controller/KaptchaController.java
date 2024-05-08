package com.iurac.recruit.controller;

import com.baomidou.kaptcha.Kaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kaptcha")
public class KaptchaController {

    @Autowired
    private Kaptcha kaptcha;

    //调用 Kaptcha 对象的 render() 方法来生成验证码图片，并将其写入响应输出流
    @GetMapping("/render")
    public void render() {
        kaptcha.render();
    }

    //调用 Kaptcha 对象的 validate(String code) 方法来验证验证码。这里使用默认的有效时间，即 900 秒
    @PostMapping("/valid")
    public void validDefaultTime(@RequestParam String code) {
        //default timeout 900 seconds
        kaptcha.validate(code);
    }

    //调用 Kaptcha 对象的 validate(String code, int time) 方法来验证验证码，并指定有效时间为 60 秒
    @PostMapping("/validTime")
    public void validWithTime(@RequestParam String code) {
        kaptcha.validate(code, 60);
    }

}