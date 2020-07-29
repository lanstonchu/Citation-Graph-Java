package application;

import java.util.Comparator;

public class PaperIdComparator implements Comparator<Paper> {

	@Override
	public int compare(Paper p1, Paper p2) {

		return p1.getIdNum() - p2.getIdNum();

	}

}
