/**
 * Created by Qapi on 2017/6/15.
 */

// 创建WebSocket
const url = '127.0.0.1:8081/control';
const webSocket = new WebSocket('ws://' + url);
webSocket.onmessage = (event => {
    vm.chessBoard = eval('(' + event.data + ')');
    if (vm.chessBoard.endFlag) {
        vm.endGame();
    }
});

// 创建VUE实例
vm = new Vue({
    el: '#main',
    data: {
        beginGame: false,     // 开始游戏按钮标志
        chessBoard: {         // 棋盘
            board: [],        // 虚拟棋格
            authority: null,  // 行权方
            endFlag: null,    // 结束标志,
        },
        // 坐标
        XA: null,
        YA: null,
        XB: null,
        YB: null,
        tableNum: null // 桌号
    },
    methods: {
        // 开始或重新游戏
        initGame (){
            if (this.beginGame && confirm('您确定要重新游戏吗？')) {
                webSocket.send(JSON.stringify({
                    way: '1'
                }));
            } else {
                webSocket.send('{}');
                this.beginGame = true;
            }
        },
        // 移动棋子
        moveChess (chess, XN, YN) {
            // 游戏尚未结束
            if (!this.chessBoard.endFlag) {
                if (this.XA == null && chess != null && chess.color == this.chessBoard.authority) {
                    this.XA = XN;
                    this.YA = YN;
                } else if (this.XA != null && (this.XA != XN || this.YA != YN)) {
                    this.XB = XN;
                    this.YB = YN;
                    webSocket.send(JSON.stringify({
                        way: "2",
                        XA: this.XA,
                        YA: this.YA,
                        XB: this.XB,
                        YB: this.YB
                    }));
                    this.empty();
                }
            }
        },
        // 结束游戏
        endGame(){
            let msg = '红棋';
            if (this.chessBoard.authority == 'BLACK') {
                msg = '黑棋';
            }
            // TODO 可用layer弹窗组件等代替
            alert("恭喜，获胜的是：" + msg);
        },
        // 清空坐标记录
        empty(){
            this.XA = null;
            this.YA = null;
            this.XB = null;
            this.YB = null;
        }
    }
})