/*---------------게임 플레이어 클래스--------------------*/

package client;

import java.awt.*;
import javax.swing.*;

public class GameAiRender{
	// ai를 식별할 수 있는 번호
    private int gid;
	
	// AI 애니메이션을 위한 이미지들
    private Image img_idle;
    private Image[] img_left = new Image[2];
    private Image[] img_right = new Image[2];
    private Image[] img_up = new Image[2];
    private Image[] img_down = new Image[2];
    
    // 애니메이션 상태
 	public enum Direction { IDLE, LEFT, RIGHT, UP, DOWN }
 	private Direction currentDirection = Direction.IDLE;
    
 	// 현재 애니메이션 프레임
    private int currentFrame = 0;
    
    // 이미지 크기
    private int width;
    private int height;
    
	// AI 포지션
	private int posX;
	private int posY;
    
	// 투명 여부
	private boolean isVisible;
	
	// 움직이는 중
	private boolean isMoving;
	

	public GameAiRender(int gid) {
		// 고유 식별 번호 등록
		this.gid = gid;
		initImages();
	}
	
	private void initImages() {
		try {
			// 이미지 로드
			img_idle = new ImageIcon("src/img/player_idle.png").getImage();
			img_left[0] = new ImageIcon("src/img/player_left1.png").getImage();
			img_left[1] = new ImageIcon("src/img/player_left2.png").getImage();
			img_right[0] = new ImageIcon("src/img/player_right1.png").getImage();
			img_right[1] = new ImageIcon("src/img/player_right2.png").getImage();
			img_up[0] = new ImageIcon("src/img/player_up1.png").getImage();
			img_up[1] = new ImageIcon("src/img/player_up2.png").getImage();
			img_down[0] = new ImageIcon("src/img/player_down1.png").getImage();
			img_down[1] = new ImageIcon("src/img/player_down2.png").getImage();

			width = img_left[1].getWidth(null);
			height = img_down[1].getHeight(null);
		} catch (Exception e) {
			 e.printStackTrace();
	         System.out.println("AI 이미지 로딩 실패!");
	         System.exit(1);
		}
	}

	// 서버에서 받은 방향 코드를 Enum으로 변환 및 설정
	public void setDirection(int dirCode) {
		Direction newDir = Direction.IDLE;
		switch(dirCode) {
			case 1: newDir = Direction.LEFT; break;
			case 2: newDir = Direction.RIGHT; break;
			case 3: newDir = Direction.UP; break;
			case 4: newDir = Direction.DOWN; break;
			default: newDir = Direction.IDLE; break;
		}
			
		if (this.currentDirection != newDir) {
			this.currentDirection = newDir;
			this.currentFrame = 0;
		}
	}
	
	// 애니메이션 프레임 넘기기
	public void nextAnimationFrame() {
		if (currentDirection == Direction.IDLE) {
			currentFrame = 0;
			return;
		}
		currentFrame = (currentFrame + 1) % 2;
	}
	
	// 현재 상태에 맞는 이미지 반환
	public Image getImg() {
		switch (currentDirection) {
			case LEFT: return img_left[currentFrame];
			case RIGHT: return img_right[currentFrame];
			case UP: return img_up[currentFrame];
			case DOWN: return img_down[currentFrame];
			default: return img_idle;
		}
	}
	//---------getter, setter-----------------------------------------------//
	
	
	public void setPosX(int posX) {
		this.posX = posX;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}
}