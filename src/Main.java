import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Scanner;

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
public class Main {
    //TIP 캐럿을 강조 표시된 텍스트에 놓고 <shortcut actionId="ShowIntentionActions"/>을(를) 누르면
    // IntelliJ IDEA이(가) 수정을 제안하는 것을 확인할 수 있습니다.
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("server 또는 client를 입력하세요.");
            return;
        }

        if (args[0].equals("server")) {
            startServer();
        } else if (args[0].equals("client")) {
            startClient();
        }
    }

    // 서버 실행
    private static void startServer() {

        WebSocketServer server = new WebSocketServer(
                new InetSocketAddress("127.0.0.1", 8080)) {

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("클라이언트 접속: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                System.out.println("수신: " + message);

                // 접속한 모든 클라이언트에게 전달
                broadcast(message);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("클라이언트 연결 종료");
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println("서버 시작! 포트: 8080");
            }
        };

        server.start();
    }

    // 클라이언트 실행
    private static void startClient() throws Exception {

        WebSocketClient client = new WebSocketClient(
                new URI("ws://127.0.0.1:8080")) {

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("서버 연결 성공!");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("메시지: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("서버 연결 종료");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        // 서버에 연결될 때까지 대기
        client.connectBlocking();

        Scanner scanner = new Scanner(System.in);

        while (client.isOpen()) {
            String message = scanner.nextLine();

            if (message.equals("/exit")) {
                break;
            }

            client.send(message);
        }

        client.close();
    }
}
