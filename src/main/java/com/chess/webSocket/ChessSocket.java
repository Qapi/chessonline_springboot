package com.chess.webSocket;

import com.chess.encoder.ChessBoardEncoder;
import com.chess.enums.ChessEnum;
import com.chess.enums.ColorEnum;
import com.chess.pojo.Chess;
import com.chess.pojo.ChessBoard;
import com.chess.util.ChessUtil;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by tingting on 2017/6/19.
 */
@Component
@ServerEndpoint(value = "/control", encoders = ChessBoardEncoder.class)
public class ChessSocket {
    /**
     * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
     * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
     */
    // 两人对战棋局~~
    // TODO 做成只允许两人同时对战（多人桌）
//            1、修改webSocketSet为Map集合,key为桌号,value为对战List<>(2)（暂不允许围观）
//            2、ChessBoard添加桌号属性
//            3、根据客户端所传桌号进行更新
    private final static ChessBoard chessBoard = ChessBoard.getInstance();
    // 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount;
    // 存放对战双方的sessionId
    private static ConcurrentMap<ColorEnum, String> ipMap = new ConcurrentHashMap<>();
    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<ChessSocket> webSocketSet = new CopyOnWriteArraySet<ChessSocket>();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    // 消息包装对象
    private JSONObject jsonObject;

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this); // 加入set中
        // 对战人数未超2人，登记SessionId
        if (ipMap.size() < 3) {
            ipMap.put(chessBoard.getAuthority(), session.getId());
            changeColor();
        }
        addOnlineCount(); // 在线数加
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this); // 从set中删除
        subOnlineCount(); // 在线数减
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        // 群发消息
        for (ChessSocket cs : webSocketSet) {
            try {
                cs.handleResult(message, session);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 根据不同行为处理
     *
     * @param message
     * @throws IOException
     */
    public void handleResult(String message, Session session) throws IOException {
        jsonObject = JSONObject.fromObject(message);
        // 鉴定申请移动的是否参战一方~
        String way = (String) jsonObject.get("way");
        if (way != null) {
            switch (way) {
                case "1":
                    if (ipMap.containsValue(session.getId())) {
                        reset();
                    }
                    break;
                case "2":
                    if (session.getId().equals(ipMap.get(chessBoard.getAuthority()))) {
                        move(jsonObject);
                    }
                    break;
                default:
            }
        }
        //this.session.getBasicRemote().sendText(message);
        this.session.getAsyncRemote().sendObject(chessBoard);
    }

    /**
     * 重置游戏
     */
    private void reset() {
        chessBoard.emptyBoard().init();
    }

    /**
     * 移动棋子
     */
    private void move(JSONObject jsonObject) {
        // 获取移动前后坐标
        int XA = (int) (jsonObject.get("XA"));
        int YA = (int) (jsonObject.get("YA"));
        int XB = (int) (jsonObject.get("XB"));
        int YB = (int) (jsonObject.get("YB"));
        Chess[][] board = chessBoard.getBoard();
        Chess chess = board[XA][YA]; // 所移动棋子
        // 判断移动是否符合规则
        if (chess != null && ChessUtil.moveOrNot(XB, YB, chess, board)) {
            // 符合规则且未杀死敌方将军
            if (board[XB][YB] == null || board[XB][YB].getIden() != ChessEnum.KING) {
                changeColor();
            } else {
                // 一方将军死亡，结束游戏
                chessBoard.setEndFlag(Boolean.TRUE);
            }
            // 移动棋子
            board = ChessUtil.moveChessto(XB, YB, chess, board);
            chessBoard.setBoard(board);
        }
    }

    /**
     * 改变行权方
     */
    public void changeColor() {
        ColorEnum color = chessBoard.getAuthority();
        color = color == ColorEnum.BLACK ? ColorEnum.RED : ColorEnum.BLACK;
        chessBoard.setAuthority(color);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        ChessSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        ChessSocket.onlineCount--;
    }
}
