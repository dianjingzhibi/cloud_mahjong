package com.example.boom_gold.config;

import java.util.ArrayList;
import java.util.List;

public final class CardUtil {


    private CardUtil(){

    }

    public static List<Integer> card=new ArrayList<>();


    public static List<Integer> getCard(){
        int nine=10;
        int f=4;
        if(card.size()==0){
            for (int i=0;i<nine;i++){
                for (int x=0;x<f;x++){
                    card.add(i);
                }
            }
        }
        washCard(card);
        washCard(card);
        return card;
    }

    private static void washCard(List<Integer> card) {
        for (int i=0;i<card.size();i++){
            int index = (int) (Math.random() * 40);
            int temp=card.get(i);
            card.set(i,card.get(index));
            card.set(index,temp);
        }
    }



}
