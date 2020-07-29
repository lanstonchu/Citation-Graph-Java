package application;

import java.util.Comparator;

/**
 * The PaperYearComparator class sort the papers by their year
 * @see PaperIdComparator
 */
public class PaperYearComparator implements Comparator<Paper>{

	@Override
	public int compare(Paper p1, Paper p2) {
		
		int year1 = Integer.parseInt(p1.getYear());
		int year2 = Integer.parseInt(p2.getYear());
		
		return year1 - year2; // normal		
		// return year2 - year1;
	}

}
