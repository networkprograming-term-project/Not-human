/*---------------게임 플레이어 클래스--------------------*/

package client;

import java.awt.*;
import javax.swing.*;

public class Player{
	private Image playerImage;
	// 플레이어 포지션
	private int posX;
	private int posY;
	
	// 생존 여부
	private boolean isAlive;
	
	// 술래 여부
	private boolean isKiller;
	
	// 투명 여부
	private boolean isVisible;
	
	


	public Player() {
		playerImage = new ImageIcon("src/img/player.png").getImage(); // 플레이어 이미지 로드
		
		// 생명 부여
		isAlive = true;
		
		// 랜덤 스폰
		posX = (int)(Math.random() * InGameView.MAX_W) + 10;
		posY = (int)(Math.random() * InGameView.MAX_H) + 10;
	}
	
	
	//setter, getter ----------------------//
	public Image getImg() {
		return this.playerImage;
	}
	
	
	public boolean isAlive() {
		return isAlive;
	}


	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
	
	public boolean isVisible() {
		return isVisible;
	}


	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}


	public boolean isKiller() {
		return isKiller;
	}


	public void setKiller(boolean isKiller) {
		this.isKiller = isKiller;
	}


	public void setPosX(int posX) {
		this.posX = posX;
	}
	
	public void setPosY(int posY) {
		this.posY = posY;
	}
	
	public int getPosX() {
		return this.posX;
	}
	
	public int getPosY() {
		return this.posY;
	}
}