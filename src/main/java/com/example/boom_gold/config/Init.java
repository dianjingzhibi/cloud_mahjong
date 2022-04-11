package com.example.boom_gold.config;

import com.example.boom_gold.constant.Constant;
import com.example.boom_gold.entity.Card;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class Init {

    public Integer mAX =13;
    public Integer f=4;
    @PostConstruct
    public void  init(){

    }
}
