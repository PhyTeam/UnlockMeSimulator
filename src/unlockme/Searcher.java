/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unlockme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bbphuc
 */
public class Searcher {
    
    protected class Tuple{
        public int key;
        public Tuple preNode = null;
        public State state;

        public Tuple(int key, State state) {
            this.key = key;
            this.state = state;
        }
          
    }
    
    public Searcher(){
        outfile = new File(output);
        try {
            fw = new FileWriter(outfile);
        } catch (IOException ex) {
            Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    Queue<Tuple> q = new ArrayDeque<>();
    List<State> solution = new ArrayList<>();
    List<Tuple> history = new ArrayList<>();
    
    String output = "F:\\sol.txt";
    File outfile;
    FileWriter fw;
    
    public void close() throws IOException{
        fw.close();
    }
    
    public List<State> getSolution(){
        return solution;
    }
    int k = 1;
    Tuple laststate = null;
    
    public boolean path(State state){
        return path(new Tuple(1, state));
    }
    
    public void print(){
        print(laststate);
    }
    
    private void print(Tuple t){
        if(t != null){
            print(t.preNode);
            String str = String.format("%d %d %d \n", t.state.getPreIndex(),t.state.getPreX(), t.state.getPreY());
            
            if(fw != null && t.state.getPreIndex() != 0)
            try {
                System.out.printf("%d %d %d \n",t.state.getPreIndex(),t.state.getPreX(), t.state.getPreY());
                fw.write(str);
                } catch (IOException ex) {
                    Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }
    
    private boolean path(Tuple t){
        // Ok! end now
        
        if (t.state.checkGoal()){
            laststate = t;
            return true;
        }
        
        // Generate next states
        List<State> newstates = t.state.getNewState();
        newstates.stream().forEach((n) -> {
            Tuple tp = new Tuple(t.key + 1, n);
            tp.preNode = t;
            history.add(tp);
            q.offer(tp);
            
        });
        
        if(!q.isEmpty()){
            return path(q.poll());
        }
        else
            return false;
    }
}
