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
public interface IBlockFactory {
    IBlock createBlock(int x, int y, int uvx, int uvy, int index, int length);
}
