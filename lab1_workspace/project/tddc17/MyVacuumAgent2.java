package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.Stack;


class MyAgentProgram2 implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 450;
	public MyAgentState state = new MyAgentState();

	// own states
	/*public int isCorner = 0;
	public int isWall = 0;
	public int goHome = 0;
	public int goCorner = 0;*/
	public int agent_vacuum_state = 0; // 0 = follow line, 1 = vacuuuuuuuuuuuuum, 2 = go home;
	public int bump_counter = 0;
	public int u_turn_counter = 0;
	public List<Action> actionQ = new ArrayList<Action>(); 
	Deque<Action> actionStack = new ArrayDeque<Action>();
	Stack<Integer> intActionStack = new Stack<Integer>();


	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
			state.agent_direction = ((state.agent_direction-1) % 4);
			if (state.agent_direction<0) 
				state.agent_direction +=4;
			state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
			state.agent_last_action = state.ACTION_TURN_RIGHT;
			return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}


	@Override
	public Action execute(Percept percept) {

		// DO NOT REMOVE this if condition!!!
		if (initnialRandomActions>0) {
			return moveToRandomStartPosition((DynamicPercept) percept);
		} else if (initnialRandomActions==0) {
			// process percept for the last step of the initial random actions
			initnialRandomActions--;
			state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
			return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// This example agent program will update the internal agent state while only moving forward.
		// START HERE - code below should be modified!

		System.out.println("x=" + state.agent_x_position);
		System.out.println("y=" + state.agent_y_position);
		System.out.println("dir=" + state.agent_direction);
		System.out.println("state=" + agent_vacuum_state);
		//System.out.println("ut_turn_counter=" + u_turn_counter);    	

		iterationCounter--;

		if (iterationCounter==0)
			return NoOpAction.NO_OP;

		DynamicPercept p = (DynamicPercept) percept;
		Boolean bump = (Boolean)p.getAttribute("bump");
		Boolean dirt = (Boolean)p.getAttribute("dirt");
		Boolean home = (Boolean)p.getAttribute("home");
		System.out.println("percept: " + p);

		// State update based on the percept value and the last action
		state.updatePosition((DynamicPercept)percept);
		if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
		}
		if (dirt)
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
		else
			state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);

		state.printWorldDebug();


		// Next action selection based on the percept value
		switch (agent_vacuum_state){
		case 0:	// follow line
		{
			if (dirt){
				System.out.println("DIRT -> choosing SUCK action!");
				actionStack.push(LIUVacuumEnvironment.ACTION_SUCK);
				intActionStack.push(state.ACTION_SUCK);
			} else if (bump && !dirt){
				// append right forward left forward
				actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				intActionStack.push(state.ACTION_MOVE_FORWARD);
				actionStack.push(LIUVacuumEnvironment.ACTION_TURN_LEFT);
				intActionStack.push(state.ACTION_TURN_LEFT);
				actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				intActionStack.push(state.ACTION_MOVE_FORWARD);
				actionStack.push(LIUVacuumEnvironment.ACTION_TURN_RIGHT);
				intActionStack.push(state.ACTION_TURN_RIGHT);
			} else { // find home 2-times?
				/*
				int pointInFront = state.UNKNOWN;
				switch (state.agent_direction) {
				case MyAgentState.NORTH:
					pointInFront = state.world[state.agent_x_position][state.agent_y_position-1];
					break;
				case MyAgentState.EAST:
					pointInFront = state.world[state.agent_x_position+1][state.agent_y_position];
					break;
				case MyAgentState.SOUTH:
					pointInFront = state.world[state.agent_x_position][state.agent_y_position+1];
					break;
				case MyAgentState.WEST:
					pointInFront = state.world[state.agent_x_position-1][state.agent_y_position];
					break;
				default: pointInFront = state.UNKNOWN;
				}
				*/
				if(intActionStack.isEmpty()){
					intActionStack.push(state.ACTION_MOVE_FORWARD);
					actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				}
				/*
				if (pointInFront == state.CLEAR){
					//agent_vacuum_state = 1;
					//intActionStack.clear();
					//actionStack.clear();
					intActionStack.push(state.ACTION_MOVE_FORWARD);
					actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				} else {
					if(intActionStack.isEmpty()){
						intActionStack.push(state.ACTION_MOVE_FORWARD);
						actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
					}
					System.out.println("point in front=" + pointInFront);
					//state.agent_last_action=state.ACTION_MOVE_FORWARD;
					//return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				} */
			}
			break;
		}

		case 1: // explore rest of map
		{
			//return NoOpAction.NO_OP;
			//return LIUVacuumEnvironment.ACTION_SUCK;
			System.out.println("state1");
			intActionStack.push(state.ACTION_NONE);
			actionStack.push(NoOpAction.NO_OP);
			
			break;
		}
		case 2: // u-turn at wall
		{
			if (bump)
				bump_counter++;
			if (bump_counter > 1){
				agent_vacuum_state = 3;
				state.agent_last_action=state.ACTION_SUCK;
				return LIUVacuumEnvironment.ACTION_SUCK;
			}
			
			break;
		}
		case 3: 
			// go home get drunk
		{
			if (home)
				return NoOpAction.NO_OP;
			else {
				// move towards home
				switch (state.agent_direction){
				case 0: // north
				{
					if (state.agent_y_position == 1){
						state.agent_last_action=state.ACTION_TURN_LEFT;
						state.agent_direction = ((state.agent_direction-1) % 4);
						if (state.agent_direction<0) 
							state.agent_direction +=4;
						return LIUVacuumEnvironment.ACTION_TURN_LEFT;
					} else {
						state.agent_last_action=state.ACTION_MOVE_FORWARD;
						return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
					}
				}
				case 1: // east
				{
					state.agent_last_action=state.ACTION_TURN_LEFT;
					state.agent_direction = ((state.agent_direction-1) % 4);
					if (state.agent_direction<0) 
						state.agent_direction +=4;
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
				case 2: // south
				{
					state.agent_last_action=state.ACTION_TURN_RIGHT;
					state.agent_direction = ((state.agent_direction+1) % 4);
					return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
				}
				case 3: // west
				{
					if (state.agent_x_position == 1){
						state.agent_last_action=state.ACTION_TURN_RIGHT;
						state.agent_direction = ((state.agent_direction+1) % 4);
						return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
					} else {
						state.agent_last_action=state.ACTION_MOVE_FORWARD;
						return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
					}
				}
				}
			}
			break;
		}
		default:
			intActionStack.push(state.ACTION_NONE);
			actionStack.push(NoOpAction.NO_OP);
			// state.agent_last_action=state.ACTION_SUCK;
			//return LIUVacuumEnvironment.ACTION_SUCK;
		}

		// return action from stack
		state.agent_last_action = intActionStack.pop();
		
		System.out.println("last action=" + state.agent_last_action);
		System.out.println("next action=" + intActionStack.toString());
		
		if (state.agent_last_action == state.ACTION_TURN_LEFT) {
			state.agent_direction = ((state.agent_direction-1) % 4);
			if (state.agent_direction<0) 
				state.agent_direction +=4;
			//state.agent_last_action = state.ACTION_TURN_LEFT;
			//return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (state.agent_last_action == state.ACTION_TURN_RIGHT) {
			state.agent_direction = ((state.agent_direction+1) % 4);
			//state.agent_last_action = state.ACTION_TURN_RIGHT;
			//return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		return actionStack.pop();
	}
}

public class MyVacuumAgent2 extends AbstractAgent {
	public MyVacuumAgent2() {
		super(new MyAgentProgram2());
	}
}