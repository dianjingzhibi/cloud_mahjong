package com.example.boom_gold.config;

import com.example.boom_gold.entity.Card;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class User  implements Serializable {

    /**
     * 骰子点数
     */
    private Integer point=0;

    private Boolean zhuang=false;

    private String sessionId;
    /**
     * 下注金额
     */
    private Integer amount;

    private Integer index;

    private Boolean fuck=false;

    private Integer[] card;

    private Integer poolSize=6;

    private Integer arrears=0;



}
