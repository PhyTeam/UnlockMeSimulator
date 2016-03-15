/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bbphuc
 */
public class GameState {
    List<IBlock> lstBlocks = new ArrayList<IBlock>();
    
    final int MAX_X  = 6;
    final int MAX_Y = 6;
    
    int mState[][] = new int[MAX_X][MAX_Y];
    public List<IBlock> getBlocks(){
        return lstBlocks;
    }
    
    public int[][] getStateArray(){
        return mState;
    }
    
    List<Step> lstStep = new LinkedList<Step>();
    
    public class Step {
        private final IBlock block;
        private final int dx;
        private final int dy;

        public Step(IBlock block, int dx, int dy) {
            this.block = block;
            this.dx = dx;
            this.dy = dy;
        }

        public IBlock getBlock() {
            return block;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }
        
        
    }
    
    public List<Step> loadSolution(String path){
        File solFile = new File(path);
        try {
            Scanner sn = new Scanner(solFile);
            while(sn.hasNextInt()){
                int index = sn.nextInt();
                // File block
                IBlock iblock = null;
                for(IBlock b : lstBlocks){
                    if(b.getIndex() == index)
                    {
                        iblock = b;
                        break;
                    }
                }
                
                if(iblock == null)
                    break;
                
                int dx = sn.nextInt();
                int dy = sn.nextInt();
                
                Step step = new Step(iblock, dx, dy);
                lstStep.add(step);
                
            }
            sn.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GameState.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lstStep;
    }
    
    public static GameState loadFromFile(String path, IBlockFactory ibf){
        GameState state = new GameState();
        List<IBlock> lb = state.lstBlocks;
        File file = new File(path);
        try {
            Scanner scanner = new Scanner(file);
            int i = 0;
            while(scanner.hasNextInt()){
                int value = scanner.nextInt();
                int col = i % 6;
                int row = i / 6;
                state.mState[row][col] = value;
                i++;
            }
            // Xu li du lieu
            boolean checked[][] = new boolean[6][6];
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    if (checked[j][k])
                        continue;
                    checked[j][k] = true;
                    if(state.mState[j][k] != 0){
                        //System.out.println("INDEX = " + state.mState[j][k]);
                        int index = state.mState[j][k],
                            count = 1;
                       
                        for (int l = 1; l < 6; l++) {
                            if (inbound(j + l, 0, 5) && (
                                    state.mState[j + l][k] == index)){
                                checked[j+l][k] = true;
                                count++;
                            }
                            else break;
                        }
                        
                        if(count > 1){
                            IBlock b = ibf.createBlock(j, k, 1, 0, index, count);
                            lb.add(b);
                            continue;
                        }
                        //System.out.println("OK");
                        count = 1;
                        for (int l = 1; l < 6; l++) {
                            if (
                                    inbound(k + l, 0, 5) && 
                                    (state.mState[j][k + l] == index)){
                                checked[j][k + l] = true;
                                count++;
                            }
                            else break;
                        }
                        System.out.println(count);
                        if(count > 1){
                            IBlock b = ibf.createBlock(j, k, 0, 1, index, count);
                            lb.add(b);
                        }
                    }
                }
            }
            
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            //Logger.getLogger(State.class.getName()).log(Level.SEVERE, null, ex);
        }
        return state;
    }
    
    static boolean inbound(int value, int min, int max){
        return (value >= min && value <= max);
    }
}