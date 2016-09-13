package searchCustom;

import java.util.Random;

public class CustomBreadthFirstSearch  extends CustomGraphSearch{

	public   CustomBreadthFirstSearch(int maxDepth){
		super(false); // add nodes to back of frontier
		System.out.println("Change line above in \"CustomBreadthFirstSearch.java\"!");
	}
};
