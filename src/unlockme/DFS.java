package unlockme;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import unlockme.State;
public class DFS extends Searcher{
	Stack <State> dfs = new Stack <>();
	Tuple t;
	Tuple last;
	public DFS(State init){
		dfs.push(init);
		t = new Tuple(0, init);
	}
	
	private void pushArrayState(List <State> states, Tuple parent){
		for (State state : states){
			dfs.push(state);
			Tuple tp = new Tuple(parent.key+1, state);
			tp.preNode = parent;
		}
	}
        public boolean Search(){
            State s = dfs.pop();
            return Seacher(new Tuple(0, s));
        }
        
	public boolean Seacher(Tuple t){
		if (t.state.checkGoal()) {laststate = t; return true;}
		List <State> choices;
		choices = t.state.getNewState();
                pushArrayState(choices,t);
		if (dfs.isEmpty()) return false;
                boolean r =  Seacher(new Tuple(t.key + 1, dfs.pop()));
                return r; //laststate = t;
	}
}
