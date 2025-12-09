/*---------------게임 플레이어 클래스--------------------*/

package client;

import java.awt.*;
import javax.swing.*;

public class PlayerRender{
	// 플레이어의 현재 상태(방향)를 나타내는 열거형
    public enum Direction {
        IDLE, LEFT, RIGHT, UP, DOWN
    }
    
    private String name;
    // 모든 방향의 이미지를 배열로 저장
    private Image img_idle;
    private Image[] img_left = new Image[2];
    private Image[] img_right = new Image[2];
    private Image[] img_up = new Image[2];
    private Image[] img_down = new Image[2];
    
    private Image img_dead; // 사망 이미지 변수
    
    // 현재 상태 및 프레임 변수
    private Direction currentDirection = Direction.IDLE;
    private int currentFrame = 0; // 0 또는 1
    
    // 이미지 크기 변수
    private int width;
    private int height;
    
	// 플레이어 포지션
	private int posX;
	private int posY;
	
	// 생존 여부
	private boolean isAlive;
	// 술래 여부
	private boolean isKiller;
	// 투명 여부
	private boolean isVisible;
	
	public PlayerRender(String name) {
		this.name = name;
		initImages();
		
		// 랜덤 스폰
		posX = (int)(Math.random() * InGameViewManager.MAX_W) + 10;
		posY = (int)(Math.random() * InGameViewManager.MAX_H) + 10;
	}
	
	public PlayerRender() {
		this.name = "";
		initImages();
		
		// 랜덤 스폰
		posX = (int)(Math.random() * InGameViewManager.MAX_W) + 10;
		posY = (int)(Math.random() * InGameViewManager.MAX_H) + 10;
	}
	
	private void initImages() {
		try {
            img_idle = new ImageIcon("src/img/player_idle.png").getImage();
            img_left[0] = new ImageIcon("src/img/player_left1.png").getImage();
            img_left[1] = new ImageIcon("src/img/player_left2.png").getImage();
            img_right[0] = new ImageIcon("src/img/player_right1.png").getImage();
            img_right[1] = new ImageIcon("src/img/player_right2.png").getImage();
            img_up[0] = new ImageIcon("src/img/player_up1.png").getImage();
            img_up[1] = new ImageIcon("src/img/player_up2.png").getImage();
            img_down[0] = new ImageIcon("src/img/player_down1.png").getImage();
            img_down[1] = new ImageIcon("src/img/player_down2.png").getImage();
            img_dead = new ImageIcon("src/img/player_dead.png").getImage();

            width = img_left[1].getWidth(null);
            height = img_down[1].getHeight(null);
        } catch (Exception e) {
            // 이미지 로드 실패 시 처리
            e.printStackTrace();
            System.out.println("이미지 로드 실패: " + e.getMessage());
            System.exit(1);
        }
		isAlive = true;
	}
	
	// 서버에서 받은 정수형 방향 코드를 처리하는 메서드
	// 0:IDLE, 1:LEFT, 2:RIGHT, 3:UP, 4:DOWN
	public void setDirection(int dirCode) {
		switch(dirCode) {
			case 1: setDirection(Direction.LEFT); break;
			case 2: setDirection(Direction.RIGHT); break;
			case 3: setDirection(Direction.UP); break;
			case 4: setDirection(Direction.DOWN); break;
			default: setDirection(Direction.IDLE); break;
		}
	}
	
	// 플레이어의 방향 상태 변경
    public void setDirection(Direction dir) {
        // 방향이 바뀔 때만 프레임을 리셋
        if (this.currentDirection != dir) {
            this.currentDirection = dir;
            this.currentFrame = 0;
        }
    }
    
    // 애니메이션 프레임 교차 변경
    public void nextAnimationFrame() {
        // IDLE 상태일 때는 애니메이션 불필요
        if (currentDirection == Direction.IDLE) {
            currentFrame = 0;
            return;
        }
        currentFrame = (currentFrame + 1) % 2; // 0과 1을 반복
    }
	
    // 현재 상태(방향)와 현재 프레임에 맞는 이미지를 반환
    public Image getImg() {
    	if (!isAlive) {
            return img_dead;
        }
    	
        switch (currentDirection) {
            case LEFT:
                return img_left[currentFrame];
            case RIGHT:
                return img_right[currentFrame];
            case UP:
                return img_up[currentFrame];
            case DOWN:
                return img_down[currentFrame];
            case IDLE:
            default:
                return img_idle;
        }
    }
    
	//setter, getter ----------------------
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public String getName() {return this.name;}
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean isAlive) { this.isAlive = isAlive; }
    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean isVisible) { this.isVisible = isVisible; }
    public boolean isKiller() { return isKiller; }
    public void setKiller(boolean isKiller) { this.isKiller = isKiller; }
    public void setPosX(int posX) { this.posX = posX; }
    public void setPosY(int posY) { this.posY = posY; }
    public int getPosX() { return this.posX; }
    public int getPosY() { return this.posY; }
}