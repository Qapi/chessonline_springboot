package com.chess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chess")
public class ChessControl{

    @GetMapping("/mainMenu")
    public String mainMenu(){
        System.out.println(232323);
        return "chessGame";
    }
}
