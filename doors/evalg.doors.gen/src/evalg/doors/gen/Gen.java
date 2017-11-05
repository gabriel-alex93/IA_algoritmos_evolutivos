package evalg.doors.gen;

import java.util.ArrayList;

public class Gen {

	public int x;
	public int y;
	
	public Gen(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int findClosest(ArrayList<Gen> genList) {
		//return the index of the closest gen in genList
		int closestIdx = 0;
		Gen closest = genList.get(closestIdx);
		double closestDist = this.distTo(closest);//euclidean distance.
		for(int i = 1; i < genList.size(); i++) {
			double currentDist = this.distTo(genList.get(i));
			if(currentDist < closestDist) {
				closestDist = currentDist;
				closest = genList.get(i);
				closestIdx = i;
			}
		}
		return closestIdx;
	}
	
	public int findClosestNot(ArrayList<Gen> genList, ArrayList<Integer> nonApp) {
		//return the index of the closest gen in genList whose index is not i nonApp
				int closestIdx = 0;
				Gen closest = genList.get(closestIdx);
				double closestDist = this.distTo(closest);//euclidean distance.
				for(int i = 1; i < genList.size(); i++) {
					double currentDist = this.distTo(genList.get(i));
					if(currentDist < closestDist && nonApp.indexOf(i) == -1) {
						closestDist = currentDist;
						closest = genList.get(i);
						closestIdx = i;
					}
				}
				return closestIdx;
	}
	
	public double distTo(Gen o) {
		//calculates the euclidean distance from this gen to o.
		return Math.sqrt(Math.pow(this.x-o.x, 2)+Math.pow(this.y-o.y, 2));
	}
	
	public int compareTo(Gen o) {
		if (this.x == o.x && this.y == o.y) return 0;
		if (this.x >= o.x && this.y >= o.y) return 1;
		return -1;
	}
}
