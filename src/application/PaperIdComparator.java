package application;

import java.util.Comparator;

/**
 * The PaperIdComparator class sort the papers by their ID
 * @see PaperYearComparator
 */
public class PaperIdComparator implements Comparator<Paper> {

	@Override
	public int compare(Paper p1, Paper p2) {

		return p1.getIdNum() - p2.getIdNum();

	}

}
