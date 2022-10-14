package Server.Decorators;

import Server.Decorator;

public class Movement extends Decorator
{
	public enum MovePriority
	{
		PRIMARY(1),
		SECONDARY(2),
		TERTIARY(3),
		LASTRESORT(4);
		
		int val;

	    MovePriority(int p) {
	        this.val = p;
	    }
	}
	
	private MovePriority priority;
	
	public int getMovePriority()
	{
		return priority.val;
	}
	
	public void setMovePriority(MovePriority p)
	{
		priority = p;
	}
	
	public String moveString = "";
}
