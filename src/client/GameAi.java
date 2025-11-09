/*---------------게임 AI 클래스--------------------*/

package client;

import java.awt.*;
import javax.swing.*;

public class GameAi extends JLabel {
	// AI 포지션
	private int posX;	private int posY;
	
	// 생존 여부
	private boolean isAlive;
	
	
	public GameAi() {
		this.setSize(20, 20);
		this.setBackground(Color.red);
		this.setOpaque(true);
		
		// 랜덤 스폰
		posX = (int)(Math.random() * 1191) + 10;
		posY = (int)(Math.random() * 791) + 10;
		this.setLocation(posX, posY);
	}
	
	// setter, getter ----------------------//
	public void setPosX(int posX) {
		this.posX = posX;
		this.setLocation(posX, posY);
	}
	
	public void setPosY(int posY) {
		this.posY = posY;
		this.setLocation(posX, posY);
	}
	
	public int getPosX() {
		return this.posX;
	}
	
	public int getPosY() {
		return this.posY;
	}
}