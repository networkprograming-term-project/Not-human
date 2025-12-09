/*---------------게임 화면 클래스--------------------*/

package client;

import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;


//-----------------------------메인 클래스----------------------------------------
public class InGameViewManager extends JPanel implements KeyListener, ActionListener{
	private Image backgroundImage; // 배경 이미지
	public static final int MAX_W = 1300; // 맵 수편
	public static final int MAX_H = 800; // 맵 수직
	
	private Timer timer; // 게임 업데이트를 위한 타이머
	private boolean[] keys; // 키 입력 상태를 추적하는 불리언 배열
	
	// 애니메이션 속도 조절용 변수
	private int animationCounter = 0;
	private static final int ANIMATION_FRAME_DELAY = 20;
	
	// UI용 이미지 및 변수
    private Image img_heart_full;
    private Image img_heart_empty;
    private int currentKillerLife = 3;
    private int remainingTime = 0;
    private String currentGameState = "RUNNING";
    
    //-----------------------게임 내 객체들---------------------------------------//
	private PlayerRender playerRender; // 도망자
  	private KillerRender killerRender; // 킬러
  	private Vector<PlayerRender> playerRenderVec = new Vector<>(); // 도망자 player를 관리하는 벡터
  	private Vector<GameAiRender> gameAiRenderVec = new Vector<>(); // Ai 클래스, Ai 관리하는 벡터
    
	//---------------데이터 송수신 관련-----------------//
  	NetworkClient networkClient; // 서버와 송수신할 함수를 가지고있는 객체
	
  	
  	//-------------- 연막탄 관련 변수--------------------//
    private Image img_smoke;      // 연막 효과 이미지
    private Image img_smoke_item; // UI 아이콘 이미지
    private int mySmokeCount = 3; // 내 남은 연막탄 개수
    
    // 연막탄 렌더링 정보 저장을 위한 간단한 클래스 (내부 클래스로 사용)
    class SmokeInfo {
        int x, y, size;
        public SmokeInfo(int x, int y, int size) { this.x=x; this.y=y; this.size=size; }
    }
    // 연막탄 관리 백터 
    private Vector<SmokeInfo> smokeList = new Vector<>();
    
	
	public InGameViewManager(NetworkClient networkClient) {
		// 서버와 통신할 객체 등록
		this.networkClient = networkClient;
		
		backgroundImage = new ImageIcon("src/img/map.png").getImage(); // 배경 이미지 로드
		keys = new boolean[256]; // 키 입력 상태를 저장할 충분한 크기의 배열을 초기화
		
        // 도망자 생성
		playerRender = new PlayerRender();
		
		// 킬러 렌더 객체 초기화
        killerRender = new KillerRender();
		
		// GameAi 생성
		for(int i=0; i<10; i++) {
			gameAiRenderVec.add(new GameAiRender(i));
		}
		
		// UI 이미지 로드 (이미지가 없으면 try-catch에서 예외 처리됨)
        try {
            img_heart_full = new ImageIcon("src/img/heart.png").getImage(); // 파일명 확인 필요
            img_heart_empty = new ImageIcon("src/img/heart_empty.png").getImage(); 
        } catch (Exception e) {
            // 이미지가 없을 경우를 대비해 null 처리
        }
		
        // 연막탄, 연막탄 아이템 ui 로드
        try {
            img_smoke = new ImageIcon("src/img/smoke.png").getImage();
            img_smoke_item = new ImageIcon("src/img/smoke_item.png").getImage(); 
        } catch (Exception e) { }
        
		addKeyListener(this); // 키 리스너 추가
		setFocusable(true); // 키 입력을 받기 위해 포커스 가능하도록 설정
	    setPreferredSize(new Dimension(MAX_W, MAX_H)); // 패널의 선호 사이즈 설정
	    
	    // 타이머를 15ms 간격으로 설정, 두번째 인자는 ActionListener를 구현한 현재 객체
		timer = new Timer(15, this); 
	    timer.start(); // 타이머 시작
	}
	
	// ActionListener 인터페이스를 구현한 메소드, 타이머 이벤트가 발생할 때마다 호출
    public void actionPerformed(ActionEvent e) {
    	//15ms 마다 게임의 전체 상황을 받아와 변화를 감지
    	String serverMsg = networkClient.getServerGameStateMSG();
    	
    	// 아직 서버에서 아무 것도 못 받은 경우 방어
    	if (serverMsg == null || !serverMsg.contains("@")) return;

    	// split limit을 -1로 주어 빈 데이터도 배열에 포함되게 함
        String[] arr = serverMsg.split("@", -1); 
        if (arr.length < 4) return;
    	
        
    	//--------- gameAI 정보 파싱------------------------//
    	// 예: 100,200,imgState/ ...
    	String gameAisMsg = arr[0];
    	if(!gameAisMsg.equals("") && !gameAisMsg.equals("a")) {
            String [] gameAiMsg = gameAisMsg.split("/");
            // AI 개수가 맞는지 확인 후 루프
            int len = Math.min(gameAiMsg.length, gameAiRenderVec.size());
            
            for(int i=0; i<len; i++) {
                if(gameAiMsg[i].isEmpty()) continue;
                
                String [] commend = gameAiMsg[i].split(","); 
                if(commend.length < 3) continue;

                GameAiRender gar = gameAiRenderVec.get(i); 
                
                gar.setPosX(Integer.parseInt(commend[0])); 
                gar.setPosY(Integer.parseInt(commend[1])); 
                // 서버에서 받은 방향 코드를 AI에게 적용 (이미지 변경용)
                gar.setDirection(Integer.parseInt(commend[2]));
            }
        }
    	
    	//--------- player 정보 파싱------------------------//
        // 예: UserA,100,200,1/UserB,300,400,2/
    	String playersMsg = arr[1];
    	if(!playersMsg.equals("") && !playersMsg.equals("a") && !playersMsg.startsWith("dummy")) {
    	    String[] pDatas = playersMsg.split("/");
    	    
    	    for(String pData : pDatas) {
    	        String[] info = pData.split(",");
    	        
    	        // 데이터 길이가 5개여야 함 (Alive 정보 추가됨)
    	        if(info.length < 5) continue; 

    	        String pName = info[0];
    	        int x = Integer.parseInt(info[1]);
    	        int y = Integer.parseInt(info[2]);
    	        int dir = Integer.parseInt(info[3]);
    	        int aliveStatus = Integer.parseInt(info[4]); // 1=Alive, 0=Dead
    	        int itemCount = Integer.parseInt(info[5]); // 아이템 개수
    	        boolean isAlive = (aliveStatus == 1);
    	        
    	        // 내 캐릭터라면 아이템 개수 업데이트 (UI 표시용)
                if (pName.equals(networkClient.getUserName())) {
                    mySmokeCount = itemCount;
                }

    	        // 해당 이름의 플레이어가 벡터에 있는지 확인
    	        boolean found = false;
    	        for(PlayerRender pr : playerRenderVec) {
    	            if(pr.getName().equals(pName)) {
    	                pr.setPosX(x);
    	                pr.setPosY(y);
    	                pr.setDirection(dir);
    	                pr.setAlive(isAlive); // 생존 상태 업데이트
    	                found = true;
    	                break;
    	            }
    	        }
    	        
    	        // 없으면 새로 생성
    	        if(!found) {
    	            PlayerRender newPr = new PlayerRender(pName);
    	            newPr.setPosX(x);
    	            newPr.setPosY(y);
    	            newPr.setDirection(dir);
    	            newPr.setAlive(isAlive); // [추가]
    	            playerRenderVec.add(newPr);
    	        }
    	    }
            // 접속 끊긴 유저 제거 로직
            // playerRenderVec.removeIf(pr -> !currentFrameNames.contains(pr.getName()));
        }
    	
    	//--------- killer 정보 파싱------------------------//
    	// 예: KillerName,100,200,1
        // 서버에서 데이터가 없으면 "a" 등을 보냄
    	String killerMsg = arr[2];
        if (!killerMsg.equals("a") && !killerMsg.isEmpty()) {
            String[] kInfo = killerMsg.split(",");
            if (kInfo.length >= 5) { 
                killerRender.setName(kInfo[0]); 
                killerRender.setPosX(Integer.parseInt(kInfo[1]));
                killerRender.setPosY(Integer.parseInt(kInfo[2]));
                killerRender.setDirection(Integer.parseInt(kInfo[3]));
                
                // 킬러 목숨 업데이트
                currentKillerLife = Integer.parseInt(kInfo[4]);
                
                killerRender.setVisible(true);
            }
        }
    	
    	//--------- gameState 정보 파싱------------------------//
        String gameStateMsg = arr[3];
        if (!gameStateMsg.isEmpty()) {
            String[] stateParts = gameStateMsg.split(":");
            currentGameState = stateParts[0];
            if (stateParts.length > 1) {
                remainingTime = Integer.parseInt(stateParts[1]);
            }

            // 게임 종료 시 팝업 처리 (한 번만 뜨게 하려면 플래그 필요)
            if (!currentGameState.equals("RUNNING")) {
                timer.stop(); // 게임 루프 정지
                repaint(); // 마지막 화면 그림
                
                String resultMsg = currentGameState.equals("RUNNER_WIN") ? "도망자 승리!" : "술래 승리!";
                JOptionPane.showMessageDialog(this, resultMsg);
                // 필요 시 로비로 이동하거나 종료 코드 추가
            }
        }
        
        //------------------   Smoke 파싱 ---------------------------//
        smokeList.clear(); // 매 프레임 새로 받으므로 초기화
        if (arr.length > 4) {
            String smokeMsg = arr[4];
            if (!smokeMsg.isEmpty()) {
                String[] smokes = smokeMsg.split("/");
                for (String sData : smokes) {
                    String[] sInfo = sData.split(",");
                    if (sInfo.length >= 3) {
                        int sx = Integer.parseInt(sInfo[0]);
                        int sy = Integer.parseInt(sInfo[1]);
                        int size = Integer.parseInt(sInfo[2]);
                        smokeList.add(new SmokeInfo(sx, sy, size));
                    }
                }
            }
        }
    	
    	//---------  애니메이션 프레임 업데이트  ------------------------//
        animationCounter++;
        if (animationCounter >= ANIMATION_FRAME_DELAY) {
            animationCounter = 0; // 카운터 리셋
            
            // 1. 모든 AI의 다음 프레임 호출
            for(GameAiRender ai : gameAiRenderVec) {
                ai.nextAnimationFrame();
            }
            
            // 2. 모든 플레이어의 다음 프레임 호출
            for(PlayerRender pr : playerRenderVec) {
                pr.nextAnimationFrame();
            }
            
            // 3. 킬러의 다음 프레임 호출
            if(killerRender != null) {
                killerRender.nextAnimationFrame();
            }
        }
    	repaint(); // 패널을 다시 그림 (paintComponent 호출)
    }
	
	
	// 화면 그리기
	public void paintComponent(Graphics g) {
        super.paintComponent(g); // 상위 클래스의 paintComponent 호출
        // 배경 이미지 그리기.
        g.drawImage(backgroundImage, 0, 0, MAX_W, MAX_H, this);
        
        // 현재 내가 술래인지 확인하는 플래그
        boolean isAmKiller = false;
        if (killerRender != null && networkClient.getUserName().equals(killerRender.getName())) {
            isAmKiller = true;
        }
        
        // 플레이어(도망자) 이미지 그리기.
        for (PlayerRender pr : playerRenderVec) {
             // 1. 플레이어 캐릭터 그리기
             g.drawImage(pr.getImg(), pr.getPosX(), pr.getPosY(), this);
             
             // [수정] 내가 술래라면 도망자들의 이름표를 그리지 않고 넘어감 (continue)
             if (isAmKiller) {
                 continue;
             }
             
             // --- 이 아래는 내가 도망자일 때만 실행됨 ---
             
             // 2. 이름표 설정
             String nameToDraw = "-" + pr.getName()+ "-";
             Color nameColor = Color.pink; // 기본 색상 (다른 유저)
             
             // 본인인 경우: 이름 앞에 화살표 추가 및 노란색 설정
             if(pr.getName().equals(networkClient.getUserName())) {
            	 nameToDraw = "▼ " + pr.getName()+ " ▼ "; 
            	 nameColor = Color.YELLOW;
             } 
             
             // 3. 이름표 그리기
             // 위치 계산 (캐릭터 머리 위 중앙)
             int textX = pr.getPosX() - 7; 
             int textY = pr.getPosY() - 10;

             // 글자 테두리
             g.setColor(Color.BLACK);
             g.drawString(nameToDraw, textX - 1, textY);
             g.drawString(nameToDraw, textX + 1, textY);
             g.drawString(nameToDraw, textX, textY - 1);
             g.drawString(nameToDraw, textX, textY + 1);
             
             // 메인 글씨
             g.setColor(nameColor); 
             g.drawString(nameToDraw, textX, textY);
        }
        
        // GameAI 이미지 그리기.
        for (GameAiRender gameAiRender : gameAiRenderVec) {
            g.drawImage(gameAiRender.getImg(), gameAiRender.getPosX(), gameAiRender.getPosY(), this);
        }
        
        // Killer 이미지 그리기
        if (killerRender != null && killerRender.isVisible()) {
            g.drawImage(killerRender.getImg(), killerRender.getPosX(), killerRender.getPosY(), this);
            g.setColor(Color.RED);
            
            if(killerRender.getName().equals(networkClient.getUserName())) {
            	// 본인 디스플레이에만 이름 표시
            	String name = "▼ " + killerRender.getName() + " ▼"; // 이름 앞에 화살표 추가
                
                // 위치 계산 (캐릭터 머리 위 중앙)
                int textX = killerRender.getPosX() - 7; 
                int textY = killerRender.getPosY() - 10;
       
                g.setColor(Color.BLACK);
                g.drawString(name, textX - 1, textY);
                g.drawString(name, textX + 1, textY);
                g.drawString(name, textX, textY - 1);
                g.drawString(name, textX, textY + 1);

                g.setColor(Color.RED); 
                g.drawString(name, textX, textY);
            } 
        }
        
        // 연막탄
        if (img_smoke != null) {
            for (SmokeInfo s : smokeList) {
                // 중심 좌표 기준으로 그리기 (x - size/2, y - size/2)
                g.drawImage(img_smoke, s.x - s.size/2, s.y - s.size/2, s.size, s.size, this);
            }
        }
        
        // UI 그리기 (최상단 레이어)
        drawUI(g);
	}
	
	// UI 그리는 메서드
    private void drawUI(Graphics g) {
        // ---------------------------------------------------------
        //  술래 목숨 (좌측 상단 하트) 술래 본인에게만 보이도록 수정
        if (killerRender != null && networkClient.getUserName().equals(killerRender.getName())) {
            int heartX = 20;
            int heartY = 20;
            int heartSize = 40;
            int padding = 5;
    
            for (int i = 0; i < 3; i++) {
                if (i < currentKillerLife) {
                    if (img_heart_full != null) {
                        g.drawImage(img_heart_full, heartX + (heartSize + padding) * i, heartY, heartSize, heartSize, this);
                    } else {
                        // 이미지가 없으면 빨간 원으로 대체
                        g.setColor(Color.RED);
                        g.fillOval(heartX + (heartSize + padding) * i, heartY, heartSize, heartSize);
                    }
                } else {
                    if (img_heart_empty != null) {
                        g.drawImage(img_heart_empty, heartX + (heartSize + padding) * i, heartY, heartSize, heartSize, this);
                    } else {
                        // 이미지가 없으면 빈 원으로 대체
                        g.setColor(Color.GRAY);
                        g.drawOval(heartX + (heartSize + padding) * i, heartY, heartSize, heartSize);
                    }
                }
            }
            
            // (선택사항) "공격 기회" 텍스트 표시
            g.setColor(Color.WHITE);
            g.setFont(new Font("Malgun Gothic", Font.BOLD, 15));
            g.drawString("Attack Chance", heartX, heartY + heartSize + 15);
        }
        
        // ---------------------------------------------------------
        // 도망자 연막탄 아이템 UI (왼쪽 상단, 하트와 비슷한 위치)
        // 현재 내가 도망저 인지 확인
        boolean isMeRunner = false;
        for(PlayerRender pr : playerRenderVec) {
            if(pr.getName().equals(networkClient.getUserName())) {
                isMeRunner = true;
                break;
            }
        }

        if (isMeRunner) {
            int itemX = 20;
            int itemY = 20;
            int itemSize = 40;
            int padding = 5;
            
            // 아이템 개수만큼 그리기
            for (int i = 0; i < 3; i++) {
                if (i < mySmokeCount) {
                    if (img_smoke_item != null) {
                        g.drawImage(img_smoke_item, itemX + (itemSize + padding) * i, itemY, itemSize, itemSize, this);
                    } else {
                        // 이미지가 없으면 회색 원으로 표시
                        g.setColor(Color.DARK_GRAY);
                        g.fillOval(itemX + (itemSize + padding) * i, itemY, itemSize, itemSize);
                    }
                }
                // 사용한 것은 그리지 않음 (visible false 효과)
            }
            
            // 텍스트 표시 (선택)
            g.setColor(Color.WHITE);
            g.setFont(new Font("Malgun Gothic", Font.BOLD, 15));
            g.drawString("Smoke Grenade (D)", itemX, itemY + itemSize + 15);
        }

        // ---------------------------------------------------------
        // 남은 시간 (중앙 상단) - 모두에게 보임
        g.setFont(new Font("Malgun Gothic", Font.BOLD, 30));
        g.setColor(Color.WHITE);
        String timeStr = "Time: " + remainingTime;
        int timeWidth = g.getFontMetrics().stringWidth(timeStr);
        g.drawString(timeStr, (MAX_W - timeWidth) / 2, 50);

        // ---------------------------------------------------------
        // 생존자 수 (우측 상단) - 모두에게 보임
        int survivorCount = 0;
        for(PlayerRender pr : playerRenderVec) {
            if(pr.isAlive()) survivorCount++;
        }
        String survivorStr = "Alive: " + survivorCount;
        int survivorWidth = g.getFontMetrics().stringWidth(survivorStr);
        g.drawString(survivorStr, MAX_W - survivorWidth - 30, 50);
        
        // ---------------------------------------------------------
        // 게임 결과 메시지 (화면 중앙) - 종료 상태일 때만 보임
        if (!currentGameState.equals("RUNNING")) {
            g.setColor(new Color(0, 0, 0, 150)); // 반투명 배경
            g.fillRect(0, 0, MAX_W, MAX_H);
            
            g.setFont(new Font("Malgun Gothic", Font.BOLD, 80));
            String resultText = currentGameState.equals("RUNNER_WIN") ? "RUNNERS WIN!" : "KILLER WINS!";
            
            // 텍스트 색상 설정 (도망자 승: 파랑, 술래 승: 빨강)
            if (currentGameState.equals("RUNNER_WIN")) g.setColor(Color.CYAN);
            else g.setColor(Color.RED);
            
            int textW = g.getFontMetrics().stringWidth(resultText);
            g.drawString(resultText, (MAX_W - textW) / 2, MAX_H / 2);
        }
    }
	
    // KeyListener 인터페이스를 구현한 메소드, 키가 눌렸을 때 호출
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true; // 해당 키가 눌렸다면 배열에 true를 설정
        
        // 내 키 입력을 서버로 전송 (화면 움직임 X, 전송 O)
        networkClient.SendMessage("/KEY PRESS " + e.getKeyCode());
    }

    // KeyListener 인터페이스를 구현한 메소드, 키에서 손을 떼었을 때 호출
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false; // 해당 키에서 손을 떼었다면 배열에 false를 설정
        
        networkClient.SendMessage("/KEY RELEASE " + e.getKeyCode());
    }

    // KeyListener 인터페이스의 메소드, 키 타이핑 이벤트를 처리
    public void keyTyped(KeyEvent e) { }

}