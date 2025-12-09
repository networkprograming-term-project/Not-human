package server;

import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;

public class Killer {
	private String playerName;
	
	private int posX;
    private int posY;
    private int direction = 0; // 0:IDLE, 1:LEFT, 2:RIGHT, 3:UP, 4:DOWN
    
    private int life = 3; // 목숨 3개
    
    // 술래는 도망자보다 조금 더 빠르게 설정
    private final static int speed = 2; 
    private int width = new ImageIcon("src/img/seeker_left2.png").getImage().getWidth(null);
    private int height = new ImageIcon("src/img/seeker_down2.png").getImage().getHeight(null);
    
    // 공격 애니메이션을 유지할 프레임 카운터
    private int attackFrame = 0;
    private boolean keyLeft, keyRight, keyUp, keyDown;
    
    public Killer(String playerName) {
        this.playerName = playerName;
        this.posX = (int)(Math.random() * (GameManager.MAX_W - width));
        this.posY = (int)(Math.random() * (GameManager.MAX_H - height));
    }
    
    public void setKeyInput(int keyCode, boolean isPressed) {
        switch(keyCode) {
            case KeyEvent.VK_LEFT:  keyLeft = isPressed; break;
            case KeyEvent.VK_RIGHT: keyRight = isPressed; break;
            case KeyEvent.VK_UP:    keyUp = isPressed; break;
            case KeyEvent.VK_DOWN:  keyDown = isPressed; break;
        }
    }
    
    public void update() {
    	
    	// 공격 프레임이 남아있으면 감소시킴
        if (attackFrame > 0) {
            attackFrame--;
        }
    	
        int dx = 0;
        int dy = 0;
        boolean isMoving = false;

        if (keyLeft) { 
            dx -= speed; 
            direction = 1; 
            isMoving = true; 
        }
        if (keyRight) { 
            dx += speed; 
            direction = 2; 
            isMoving = true; 
        }
        if (keyUp) { 
            dy -= speed; 
            direction = 3; 
            isMoving = true; 
        }
        if (keyDown) { 
            dy += speed; 
            direction = 4; 
            isMoving = true; 
        }

        if (!isMoving) direction = 0;

        posX += dx;
        posY += dy;

        // 맵 경계 처리
        posX = Math.max(0, Math.min(posX, GameManager.MAX_W - width));
        posY = Math.max(0, Math.min(posY, GameManager.MAX_H - height));
    }
    
    // 공격 시작 메서드 (GameManager에서 호출)
    public void triggerAttack() {
        // 약 20프레임 동안 공격 모션 유지
        attackFrame = 20; 
    }
    
    // 목숨 감소
    public void decreaseLife() {
        if (life > 0) life--;
    }
    
 // Getter
    public String getName() { return playerName; }
    public void setName(String name) { this.playerName=name; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getDirection() { 
    	if (attackFrame > 0) {
            return 5; // 5번은 공격 상태로 약속
        }
    	return direction; 
    	}
    public int getLife() { return life; }
    
    // 충돌 감지를 위해 크기 반환
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
