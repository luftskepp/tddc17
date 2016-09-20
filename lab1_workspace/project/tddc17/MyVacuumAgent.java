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

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;

	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;

	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
		{
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
		}

	}

	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}

	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" _ ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	// Here you can define your variables!
	public int iterationCounter = 1000;
	public MyAgentState state = new MyAgentState();

	// own states
	/*public int isCorner = 0;
	public int isWall = 0;
	public int goHome = 0;
	public int goCorner = 0;*/
	public int agent_vacuum_state = 0; // 0 = go home; 1 = follow border, 2 = discover and vacum, 3 = go home;
	public int bump_counter = 0;
	public int u_turn_counter = 0;
	public int homecounter = 0;
	
	public int [] numbers = {0, 1, 2, 3};
	public int nextpos = 0;
	public int checkpos = 0;
	public int robotRealmX = 0;
	public int robotRealmY = 0; //size of the domain that uppdates while exploring the edges
	public int [] target = {15,15};
	public boolean hasTarget = false;
	
	public List<Action> actionQ = new ArrayList<Action>();  // The list of actions the agent will perform.
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
		case 0: // find way home
		{
			
			if(dirt) {
				actionStack.push(LIUVacuumEnvironment.ACTION_SUCK);
				intActionStack.push(state.ACTION_SUCK);
			} else if (home) {
				agent_vacuum_state++;
				if (actionStack.isEmpty()){
					actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
					intActionStack.push(state.ACTION_MOVE_FORWARD);
				}
				
			} else if (actionStack.isEmpty()){
			
			int cost = 10000;
			for( int i : numbers){
				int iterCost = 0;
				switch(i) {
				case MyAgentState.NORTH:
				{
					System.out.println("case north");
					checkpos = state.world[state.agent_x_position][state.agent_y_position-1];
					iterCost = Math.abs(state.agent_x_position - 1) + Math.abs(state.agent_y_position -1 - 1);
					System.out.println("itercost" + iterCost);
					break;
				}
				case MyAgentState.EAST:
				{
					System.out.println("case east");
					checkpos = state.world[state.agent_x_position+1][state.agent_y_position];
					iterCost = Math.abs(state.agent_x_position +1 - 1) + Math.abs(state.agent_y_position - 1);
					System.out.println("itercost" + iterCost);
					break;
				}
				case MyAgentState.SOUTH:
				{
					System.out.println("case south");
					checkpos = state.world[state.agent_x_position][state.agent_y_position+1];
					iterCost = Math.abs(state.agent_x_position - 1) + Math.abs(state.agent_y_position +1 - 1);
					System.out.println("itercost" + iterCost);
					break;
				}
				case MyAgentState.WEST:
				{
					System.out.println("case west");
					checkpos = state.world[state.agent_x_position-1][state.agent_y_position];
					iterCost = Math.abs(state.agent_x_position -1 - 1) + Math.abs(state.agent_y_position - 1);
					System.out.println("itercost" + iterCost);
					break;
				}
				}
				if (checkpos == state.WALL)
					iterCost = 100000; 

				if (iterCost < cost){
					nextpos = i;
					cost = iterCost;
					System.out.println("nextpos=" + nextpos);
					System.out.println("dir =" + state.agent_direction);
					System.out.println("cost=" + cost);
				}
			}
			// go to nextpos
			int diff = state.agent_direction - nextpos;
			actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
			intActionStack.push(state.ACTION_MOVE_FORWARD);
			System.out.println("diff=" + diff);
			while (diff != 0){
				if (diff > 0){
					System.out.println("borde turna left");
					actionStack.push(LIUVacuumEnvironment.ACTION_TURN_LEFT);
					intActionStack.push(state.ACTION_TURN_LEFT);
					
					diff--;
				} else {
					System.out.println("borde turna right");
					actionStack.push(LIUVacuumEnvironment.ACTION_TURN_RIGHT);
					intActionStack.push(state.ACTION_TURN_RIGHT);
					diff++;
				}
			}
			}
			break;
		}

			
		case 1:	// Finding the home tile and mapping the grid. When we implemented this we thought the home tile always was postitioned at the upper most left corner.
			//Once the home tile have been found, the agent will map the outher walls of the grid, updating the maximum size of our domain.
		{
			homecounter++;
			if (state.agent_x_position > robotRealmX)
				robotRealmX = state.agent_x_position;
			if (state.agent_y_position > robotRealmY)
				robotRealmY = state.agent_y_position;
			if (dirt){
				System.out.println("DIRT -> choosing SUCK action!");
				actionStack.push(LIUVacuumEnvironment.ACTION_SUCK);
				intActionStack.push(state.ACTION_SUCK);
			} else if (home && homecounter > 10) {
				agent_vacuum_state++;
				actionStack.clear();
				intActionStack.clear();
				actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				intActionStack.push(state.ACTION_MOVE_FORWARD);
			}
			
			else if (bump && !dirt){
				// append right forward left forward
				actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				intActionStack.push(state.ACTION_MOVE_FORWARD);
				actionStack.push(LIUVacuumEnvironment.ACTION_TURN_LEFT);
				intActionStack.push(state.ACTION_TURN_LEFT);
				actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				intActionStack.push(state.ACTION_MOVE_FORWARD);
				actionStack.push(LIUVacuumEnvironment.ACTION_TURN_RIGHT);
				intActionStack.push(state.ACTION_TURN_RIGHT);
			} else { 
				if(intActionStack.isEmpty()){
					intActionStack.push(state.ACTION_MOVE_FORWARD);
					actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				}
				
			}
			break;
		}

		case 2: // The agent checks if there is any "unknown" tiles left in the grid. If there is it choses the closest and tries to move to that tile. It will repeat the tile til all 
			//unknown tiles are discovered. Then it will move to the next state
		{	
			if (dirt) {
				actionStack.push(LIUVacuumEnvironment.ACTION_SUCK);
				intActionStack.push(state.ACTION_SUCK);
			}
			else if (actionStack.isEmpty())
			{	
				System.out.println("enter target search");
				System.out.println("realm X=" + robotRealmX + " Y=" + robotRealmY);
				
				if ((target[0] == state.agent_x_position && target[1] == state.agent_y_position) || state.world[state.agent_x_position][state.agent_y_position] != state.UNKNOWN)
					hasTarget = false;
				
				int targetdist = 1000;
				int numUnknowns = 0;
				if (!hasTarget)
				{
				//int[] nexttarget = {1,1}; 
				for (int i=1; i < robotRealmX; i++)
				{
					for (int j=1; j < robotRealmY ; j++)
					{	
						if (state.world[i][j] == state.UNKNOWN)
						{
							numUnknowns++;
							int dist = Math.abs(state.agent_x_position - i) + Math.abs(state.agent_y_position - j);
							if (dist < targetdist)
							{
								targetdist = dist;
							
								target = new int[] {i,j};
								hasTarget = true;
							}
						}

					}
				}

				if (numUnknowns == 0)
				{
					agent_vacuum_state++;
					intActionStack.clear();
					actionStack.clear();
					intActionStack.push(state.ACTION_MOVE_FORWARD);
					actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
					break;
				}
				}
				System.out.println("target(0)=" + target[0] + " target(1)=" + target[1] );




				
				double cost = 10000;
				for( int i : numbers){
					double iterCost = 0;
					switch(i) {
					case MyAgentState.NORTH:
					{
						System.out.println("case north");
						checkpos = state.world[state.agent_x_position][state.agent_y_position-1];
						iterCost = Math.sqrt( Math.pow(state.agent_x_position - target[0], 2) + Math.pow(state.agent_y_position -1 - target[1], 2));
						System.out.println("itercost" + iterCost);
						break;
					}
					case MyAgentState.EAST:
					{
						System.out.println("case east");
						checkpos = state.world[state.agent_x_position+1][state.agent_y_position];
						iterCost = Math.abs(state.agent_x_position +1 - target[0]) + Math.abs(state.agent_y_position - target[1]);
						iterCost = Math.sqrt( Math.pow(state.agent_x_position + 1 - target[0], 2) + Math.pow(state.agent_y_position - target[1], 2));
						
						System.out.println("itercost" + iterCost);
						break;
					}
					case MyAgentState.SOUTH:
					{
						System.out.println("case south");
						checkpos = state.world[state.agent_x_position][state.agent_y_position+1];
						iterCost = Math.abs(state.agent_x_position - target[0]) + Math.abs(state.agent_y_position +1 - target[1]);
						iterCost = Math.sqrt( Math.pow(state.agent_x_position - target[0], 2) + Math.pow(state.agent_y_position +1 - target[1], 2));
						System.out.println("itercost" + iterCost);
						break;
					}
					case MyAgentState.WEST:
					{
						System.out.println("case west");
						checkpos = state.world[state.agent_x_position-1][state.agent_y_position];
						iterCost = Math.abs(state.agent_x_position -1 - target[0]) + Math.abs(state.agent_y_position - target[1]);
						iterCost = Math.sqrt( Math.pow(state.agent_x_position-1 - target[0], 2) + Math.pow(state.agent_y_position - target[1], 2));
						System.out.println("itercost" + iterCost);
						break;
					}
					}
					if (checkpos == state.WALL)
						iterCost = 100000; //Double.POSITIVE_INFINITY;

					if (iterCost < cost){
						nextpos = i;
						cost = iterCost;
						System.out.println("nextpos=" + nextpos);
						System.out.println("dir =" + state.agent_direction);
						System.out.println("cost=" + cost);
					}
				}
				// go to nextpos
				int diff = state.agent_direction - nextpos;
				actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
				intActionStack.push(state.ACTION_MOVE_FORWARD);
				System.out.println("diff=" + diff);
				while (diff != 0){
					if (diff > 0){
						System.out.println("borde turna left");
						actionStack.push(LIUVacuumEnvironment.ACTION_TURN_LEFT);
						intActionStack.push(state.ACTION_TURN_LEFT);

						diff--;
					} else {
						System.out.println("borde turna right");
						actionStack.push(LIUVacuumEnvironment.ACTION_TURN_RIGHT);
						intActionStack.push(state.ACTION_TURN_RIGHT);
						diff++;
					}
				}
			}

			
			System.out.println("let the war begin");
			
			break;
		}

		
		case 3: // The agent will now try to move to the home tile, chosing the path with the shortest euklidean distance. 
		{
			if (home) {
				agent_vacuum_state++;
				if (actionStack.isEmpty()){
					actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
					intActionStack.push(state.ACTION_MOVE_FORWARD);
				}
				
			} else if (actionStack.isEmpty()){
			
			double cost = 10000;
			for( int i : numbers){
				double iterCost = 0;
				switch(i) {
				case MyAgentState.NORTH:
				{
					System.out.println("case north");
					checkpos = state.world[state.agent_x_position][state.agent_y_position-1];
					iterCost = Math.sqrt( Math.pow(state.agent_x_position - target[0], 2) + Math.pow(state.agent_y_position -1 - target[1], 2));
					System.out.println("itercost" + iterCost);
					break;
				}
				case MyAgentState.EAST:
				{
					System.out.println("case east");
					checkpos = state.world[state.agent_x_position+1][state.agent_y_position];
					iterCost = Math.abs(state.agent_x_position +1 - target[0]) + Math.abs(state.agent_y_position - target[1]);
					iterCost = Math.sqrt( Math.pow(state.agent_x_position + 1 - target[0], 2) + Math.pow(state.agent_y_position - target[1], 2));
					
					System.out.println("itercost" + iterCost);
					break;
				}
				case MyAgentState.SOUTH:
				{
					System.out.println("case south");
					checkpos = state.world[state.agent_x_position][state.agent_y_position+1];
					iterCost = Math.abs(state.agent_x_position - target[0]) + Math.abs(state.agent_y_position +1 - target[1]);
					iterCost = Math.sqrt( Math.pow(state.agent_x_position - target[0], 2) + Math.pow(state.agent_y_position +1 - target[1], 2));
					System.out.println("itercost" + iterCost);
					break;
				}
				case MyAgentState.WEST:
				{
					System.out.println("case west");
					checkpos = state.world[state.agent_x_position-1][state.agent_y_position];
					iterCost = Math.abs(state.agent_x_position -1 - target[0]) + Math.abs(state.agent_y_position - target[1]);
					iterCost = Math.sqrt( Math.pow(state.agent_x_position-1 - target[0], 2) + Math.pow(state.agent_y_position - target[1], 2));
					System.out.println("itercost" + iterCost);
					break;
				}
				}
				if (checkpos == state.WALL)
					iterCost = 100000; 

				if (iterCost < cost){
					nextpos = i;
					cost = iterCost;
					System.out.println("nextpos=" + nextpos);
					System.out.println("dir =" + state.agent_direction);
					System.out.println("cost=" + cost);
				}
			}
			// go to nextpos
			int diff = state.agent_direction - nextpos;
			actionStack.push(LIUVacuumEnvironment.ACTION_MOVE_FORWARD);
			intActionStack.push(state.ACTION_MOVE_FORWARD);
			System.out.println("diff=" + diff);
			while (diff != 0){
				if (diff < 0){
					System.out.println("borde turna left");
					actionStack.push(LIUVacuumEnvironment.ACTION_TURN_LEFT);
					intActionStack.push(state.ACTION_TURN_LEFT);
					
					diff++;
				} else {
					System.out.println("borde turna right");
					actionStack.push(LIUVacuumEnvironment.ACTION_TURN_RIGHT);
					intActionStack.push(state.ACTION_TURN_RIGHT);
					diff--;
				}
			}
			}
			break;
		}

		
		

		case 4: // stop
		{	
			intActionStack.push(state.ACTION_NONE);
			actionStack.push(NoOpAction.NO_OP);
			
			
			break;
		}
		
		default:
			intActionStack.push(state.ACTION_NONE);
			actionStack.push(NoOpAction.NO_OP);
			
		}


		state.agent_last_action = intActionStack.pop();
		
		System.out.println("last action=" + state.agent_last_action);
		System.out.println("next action=" + intActionStack.toString());
		
		if (state.agent_last_action == state.ACTION_TURN_LEFT) {
			state.agent_direction = ((state.agent_direction-1) % 4);
			if (state.agent_direction<0) 
				state.agent_direction +=4;
			
		} else if (state.agent_last_action == state.ACTION_TURN_RIGHT) {
			state.agent_direction = ((state.agent_direction+1) % 4);
			
		} 
		return actionStack.pop();
	}
}

public class MyVacuumAgent extends AbstractAgent {
	public MyVacuumAgent() {
		super(new MyAgentProgram());
	}
}
