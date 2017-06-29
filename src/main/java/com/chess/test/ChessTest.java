package com.chess.test;

import com.chess.pojo.Chess;
import com.chess.pojo.ChessBoard;
import com.chess.util.ChessUtil;
import org.junit.jupiter.api.Test;

/**
 * Created by Qapi on 2017/6/14.
 */
public class ChessTest {
    ChessBoard chessBoard = ChessBoard.newInstance();

    @Test
    public void test1() {
        printArray(chessBoard.getBoard());
    }

    @Test
    public void test2() {
        Chess[][] chesss = chessBoard.getBoard();
        System.out.print(ChessUtil.moveOrNot(4,9,chesss[4][0],chesss));
    }

    public void printArray(Chess[][] chesss) {
        for (int i = 0; i < chesss.length; i++) {
            for (int j = 0; j < chesss[i].length; j++) {
                if (chesss[i][j] != null) {
                    System.out.println("坐标：" + i + "-" + j + " 颜色：" + chesss[i][j].getColor() + " 角色：" + chesss[i][j].getIden());

                }
            }
        }
    }
}
