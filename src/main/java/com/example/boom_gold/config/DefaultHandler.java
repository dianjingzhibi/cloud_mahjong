package com.example.boom_gold.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DefaultHandler implements WebSocketHandler {


    Set<WebSocketSession> webSocketSessions = new HashSet<>();
    Set<String> ready = new HashSet<>();
    List<User> List = new ArrayList<>();
    Map<String, String> name = new HashMap<>();
    List<Integer> card = null;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        JSONObject jsonObject = JSON.parseObject(message.getPayload().toString());
        String action = jsonObject.getString("action");
        JSONObject jsonObject1 = new JSONObject();
        switch (action) {
            //登录
            case "login":
                webSocketSessions.add(session);
                jsonObject1.put("action", "login");
                jsonObject1.put("msg", String.format("游戏玩家{%s}进入了房间,总人数{%s}人", jsonObject.getString("name"), webSocketSessions.size()));
                jsonObject1.put("timestamp", System.currentTimeMillis());
                for (WebSocketSession webSocketSession : webSocketSessions) {
                    webSocketSession.sendMessage(new TextMessage(jsonObject1.toString().getBytes()));
                }
                name.put(session.getId(), jsonObject.getString("name"));
                break;

            //准备
            case "start":
                if (webSocketSessions.size() == 1) {
                    jsonObject1.put("action", "fail");
                    jsonObject1.put("msg", "一个人无法启动");
                    jsonObject1.put("timestamp", System.currentTimeMillis());
                    session.sendMessage(new TextMessage(jsonObject1.toString().getBytes()));
                } else {
                    ready.add(session.getId());
                    if (ready.size() == webSocketSessions.size()) {
                        jsonObject1.put("action", "startNext");
                        jsonObject1.put("msg", "游戏已经启动,点击掷骰子");
                    } else {
                        jsonObject1.put("action", "start");
                        jsonObject1.put("msg", String.format("游戏玩家{%s}准备,待准备{%s}人", jsonObject.getString("name"), webSocketSessions.size() - ready.size()));
                    }
                    jsonObject1.put("timestamp", System.currentTimeMillis());
                    for (WebSocketSession webSocketSession : webSocketSessions) {
                        webSocketSession.sendMessage(new TextMessage(jsonObject1.toString().getBytes()));
                    }
                }
                break;
            //下一步
            case "next":
                set(session.getId(), jsonObject.getString("name"));
                break;
            case "xiazhu":
                xiazhu(session.getId(), jsonObject);
                break;
            default:
                break;
        }
    }

    private void xiazhu(String id, JSONObject jsonObject) throws IOException {
        Integer amount = jsonObject.getInteger("amount");
        boolean over = true;
        for (User user : List) {
            if (user.getSessionId().equalsIgnoreCase(id)) {
                user.setAmount(amount);
            }
            if (!user.getZhuang() && user.getAmount() == null) {
                over = false;
            }
        }
        //下注完毕开始发牌
        if (over) {
            pickCard();
        }
    }

    private synchronized void set(String id, String name) throws IOException {
        Random rand1 = new Random();
        int finals = rand1.nextInt(6) + rand1.nextInt(6);
        User user = new User();
        user.setPoint(0);
        user.setSessionId(id);
        user.setPoint(finals);
        user.setIndex(List.size() + 1);
        List.add(user);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "start");
        jsonObject.put("msg", String.format("游戏玩家{%s},骰子点数为{%s}", name, finals));
        jsonObject.put("timestamp", System.currentTimeMillis());
        for (WebSocketSession ss : webSocketSessions) {
            ss.sendMessage(new TextMessage(jsonObject.toString().getBytes()));
        }
        if (List.size() == webSocketSessions.size()) {
            List.sort(Comparator.comparing(User::getPoint));
            User user1 = List.get(List.size() - 1);
            user1.setPoolSize(6);
            user1.setZhuang(true);
            String name1 = this.name.get(user1.getSessionId());
            StringBuilder stringBuilder = new StringBuilder();

            for (User value : List) {
                stringBuilder.append("玩家名称:").append(this.name.get(value.getSessionId())).append("-").append("座位序号:").append(value.getIndex()).append("\n");
            }
            for (WebSocketSession ss : webSocketSessions) {
                jsonObject.put("msg", String.format("游戏玩家{%s},骰子点数为{%s}成为庄家,顺序\n%s️\n", name1, finals, stringBuilder));
                jsonObject.put("action", "beforeDown");
                ss.sendMessage(new TextMessage(jsonObject.toString().getBytes()));
            }
        }
    }



    /**
     * 开始发牌
     *
     * @throws IOException
     */
    private synchronized void pickCard() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("timestamp", System.currentTimeMillis());
        //每个人两张牌
        int two1 = 2;
        if (card == null || card.size() == 0) {
            card = CardUtil.getCard();
            //判断牌是否够发，如果不够发，直接进行洗牌
        } else if (card.size() < webSocketSessions.size() * two1) {
            card = CardUtil.getCard();
        }else{
            //询问是否结束
        }
        for (WebSocketSession ss : webSocketSessions) {
            StringBuilder stringBuilder = new StringBuilder();
            Integer one = pickAndRemove();
            Integer two = pickAndRemove();
            int result = (one + two);
            for (User user : this.List) {
                if (user.getSessionId().equalsIgnoreCase(ss.getId())) {
                    user.setCard(new Integer[]{one, two});
                }
            }
            stringBuilder.append("玩家:").append(this.name.get(ss.getId())).append("你的牌是:").append(one).append(",").append(two).append(result > 10 ? result - 10 : result);
            jsonObject.put("msg", stringBuilder.toString());
            jsonObject.put("action", "gameStart");
            ss.sendMessage(new TextMessage(jsonObject.toString().getBytes()));
        }

        User zhuang = this.List.stream().filter(User::getZhuang).findFirst().get();
        int z=getnumBer(zhuang);

        int index=zhuang.getIndex();
        for (int i=0;i<this.List.size()-1;i++){
            index=index+1<this.List.size()?index+1:0;
            User user=this.List.get(index);
            int u= getnumBer(user);
            if(z>=u){
                user.setPoint(user.getPoint()-user.getAmount());
                zhuang.setPoolSize(user.getPoolSize()+user.getAmount());
                zhuang.setPoint(zhuang.getPoint()+user.getAmount());
            }else{
                if(u>10){
                    user.setPoint(user.getPoint()+user.getAmount()*2);
                    zhuang.setPoolSize(user.getPoolSize()-user.getAmount()*2);
                    zhuang.setPoint(zhuang.getPoint()-user.getAmount()*2);
                }else{
                    user.setPoint(user.getPoint()+ user.getAmount());
                    zhuang.setPoolSize(user.getPoolSize()- user.getAmount());
                    zhuang.setPoint(zhuang.getPoint()- user.getAmount());
                }
                if(zhuang.getPoolSize()<0){
                    //TODO 输完了看续不续
                    user.setArrears(zhuang.getPoolSize());
                    break;
                }
            }
        }
        StringBuilder stringBuilder=new StringBuilder();
        for (User ss : List) {
            stringBuilder.append("用户昵称：").append(this.name.get(ss.getSessionId())).append("牌：").append(Arrays.toString(ss.getCard())).append("当前余额：").append(ss.getPoint());
        }
        for (WebSocketSession ss : webSocketSessions) {
            jsonObject.put("msg", stringBuilder.toString());
            jsonObject.put("action", "gameOver");
            ss.sendMessage(new TextMessage(jsonObject.toString().getBytes()));
        }
    }


    private int getnumBer(User zhuang) {
        int zhuangNumber= (zhuang.getCard()[0] + zhuang.getCard()[1]);
        if (zhuang.getCard()[0].equals(zhuang.getCard()[1])) {
            zhuangNumber = zhuangNumber * 10;
            zhuangNumber= zhuangNumber==0?100:zhuangNumber;
        }else{
            zhuangNumber=zhuangNumber>=10?zhuangNumber-10:zhuangNumber;
        }
        return zhuangNumber;
    }

    private Integer pickAndRemove() {
        Random random = new Random();
        int value = random.nextInt(card.size());
        int result = card.get(value);
        Iterator<Integer> integerIterator = card.iterator();
        int i = 0;
        while (integerIterator.hasNext()) {
            integerIterator.next();
            i++;
            if (value == i) {
                integerIterator.remove();
            }
        }
        return result;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        webSocketSessions.remove(session);
        ready.remove(session.getId());
        List.removeIf(user -> session.getId().equalsIgnoreCase(user.getSessionId()));
        name.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
