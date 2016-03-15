/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.simulator;

/**
 *
 * @author bbphuc
 */
public interface IBlock {
    public int getX();
    public int getY();
    public int getIndex();
    public int getLength();
    
    public void setXY(int x, int y);
}
