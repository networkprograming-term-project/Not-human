// ServerManager.java - 수정된 구조
package server;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ServerManager extends JFrame {
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;
    private ServerSocket socket;
    private Socket client_socket;
    private Vector<UserService> UserVec = new Vector<>();
    
    // 모든 유저가 공유하는 단 하나의 게임 매니저
    private GameManager gameManager = null; 

    // 게임 시작 중인지 확인하는 플래그
    private boolean isGameStarting = false;
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ServerManager frame = new ServerManager();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ServerManager() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 338, 386);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 300, 244);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblNewLabel = new JLabel("Port Number");
        lblNewLabel.setBounds(12, 264, 87, 26);
        contentPane.add(lblNewLabel);

        txtPortNumber = new JTextField();
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setText("30000");
        txtPortNumber.setBounds(111, 264, 199, 26);
        contentPane.add(txtPortNumber);
        txtPortNumber.setColumns(10);

        JButton btnServerStart = new JButton("Server Start");
        btnServerStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                AppendText("Chat Server Running...");
                btnServerStart.setText("Server is Running...");
                btnServerStart.setEnabled(false);
                txtPortNumber.setEnabled(false);

                AcceptServer accept_server = new AcceptServer();
                accept_server.start();
            }
        });
        btnServerStart.setBounds(12, 300, 300, 35);
        contentPane.add(btnServerStart);
    }

    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    client_socket = socket.accept();
                    UserService userService = new UserService(client_socket);
                    UserVec.add(userService);
                    userService.start();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    public void AppendText(String str) {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }
    
    // 게임 리셋 및 대기실 복귀 메서드
    public void resetGame() {
        AppendText("[System] 게임이 종료되어 대기실로 복귀합니다.");
        
        // 1. 게임 매니저 초기화
        this.gameManager = null;
        this.isGameStarting = false;
        
        // 2. 모든 유저의 준비 상태 해제
        for(UserService user : UserVec) {
            user.isReady = false;
        }
        
        // 3. 클라이언트들에게 리셋 신호 전송
        broadcast("/RESET");
    }


    class UserService extends Thread {
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket client_socket;
        private String UserName = "";
        private boolean isReady = false;

        public UserService(Socket client_socket) {
            this.client_socket = client_socket;
            try {
                dis = new DataInputStream(client_socket.getInputStream());
                dos = new DataOutputStream(client_socket.getOutputStream());

                String line = dis.readUTF(); // /login 이름
                String[] msg = line.split(" ");
                UserName = msg[1].trim();

                AppendText("입장: " + UserName);
                WriteAll("[" + UserName + "]님이 입장하였습니다.\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public String getUserName() { return UserName; } // 이름 getter

        public void WriteOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                // 에러 처리
            }
        }

        public void WriteAll(String str) {
            for (UserService user : UserVec) {
                user.WriteOne(str);
            }
        }
        
        public void logout() {
            UserVec.remove(this);
            WriteAll("[" + UserName + "]님이 퇴장하였습니다.\n");
        }

        // [요청하신 run 메서드 전체 코드]
        @Override
        public void run() {
            while (true) {
                try {
                    // 수신
                    String msg = dis.readUTF();
                    msg = msg.trim();
                    AppendText(msg);

                    // 분석
                    String[] parts = msg.split(" ");
                    String command = parts[0];

                    // [Case A] 게임 준비
                    if (command.equals("/READY")) {
                        isReady = true;
                        WriteAll(String.format("--- [%s]님이 준비를 완료하셨습니다! ---\n", UserName));

                        int readyCnt = 0;
                        for (UserService user : UserVec) {
                            if (user.isReady) readyCnt++;
                        }
                        WriteAll(String.format("[시스템] 준비 인원: %d / %d\n", readyCnt, UserVec.size()));

                     // 3초 카운트다운 후 시작 로직
                        if (readyCnt == UserVec.size() && UserVec.size() >= 1 && !isGameStarting) {
                            isGameStarting = true; // 중복 실행 방지 플래그 설정
                            
                            // 별도 스레드에서 카운트다운 실행 (통신 블로킹 방지)
                            new Thread(() -> {
                                try {
                                    WriteAll("\n[시스템] 모든 플레이어가 준비되었습니다.\n");
                                    
                                    for(int i=3; i>0; i--) {
                                        WriteAll("[시스템] " + i + "초 후에 게임을 시작합니다...\n");
                                        Thread.sleep(1000); // 1초 대기
                                    }
                                    
                                    WriteAll("\n\n[GM] --> 모두 준비되어 게임을 시작하겠습니다. <--");

                                    Vector<String> playerNames = new Vector<>();
                                    for (UserService user : UserVec) {
                                        playerNames.add(user.UserName);
                                    }

                                    if (gameManager == null) {
                                        gameManager = new GameManager(playerNames, ServerManager.this);
                                    }
                                    
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } finally {
                                    isGameStarting = false; // 플래그 해제 (필요 시)
                                }
                            }).start();
                        }
                    }
                    // [Case B] 키 입력 중계 (/KEY)
                    else if (command.equals("/KEY")) {
                        // 게임이 시작된 상태라면 매니저에게 전달
                        if (gameManager != null) {
                            gameManager.handleInput(UserName, msg);
                        }
                    }
                    // [Case C] 일반 채팅
                    else {
                        WriteAll(msg + "\n");
                    }

                } catch (IOException e) {
                    AppendText(UserName + " 연결 끊김");
                    try {
                        dis.close();
                        dos.close();
                        client_socket.close();
                    } catch (IOException e1) {
                    }
                    logout();
                    break;
                }
            }
        }
    }
    
    // GameManager가 브로드캐스트할 때 호출할 메서드 추가
    public void broadcast(String msg) {
        for(UserService user : UserVec) {
            user.WriteOne(msg);
        }
    }
}