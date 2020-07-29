package application;

import java.util.ArrayList;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Paper {

	private final SimpleIntegerProperty id;
	private final SimpleStringProperty title;
	private final SimpleStringProperty titleNickName;
	// private final SimpleStringProperty author;
	private final SimpleStringProperty mainAuthor;
	private final SimpleStringProperty otherAuthor;
	private final SimpleStringProperty fullAuthor;
	private final SimpleIntegerProperty authorNum;
	private final SimpleStringProperty year;
	private final SimpleStringProperty arxivId;
	private final SimpleStringProperty doi;
	private final SimpleStringProperty bibCode;
	private final SimpleStringProperty listChildrenBibCode;
	private final SimpleStringProperty listChildrenBibCodeFiltered;
	private final SimpleStringProperty listChildrenId;

	private int idNum;
	private String mainAuthorStr;
	private String otherAuthorStr;
	private String fullAuthorStr;
	private int authorNumInt;

	private String arxivIdStr;
	private String arxivIdNoVerStr; // ArXiv ID without version number

	private ArrayList<String> listChildrenBibCodeArray;
	private ArrayList<String> listChildrenBibCodeFilteredArray;
	private ArrayList<Integer> listChildrenIdArray;
	
	
	Paper(int id, String title, String titleNickName, String author, String year, String arxivid, String doi) {
		this.idNum = id;
		this.id = new SimpleIntegerProperty(id);

		// remove "{" and "}" from title
		title = removeBrackets(title);
		this.title = new SimpleStringProperty(title);
		
		this.titleNickName = new SimpleStringProperty(titleNickName);

		parseAuthors(author);
		this.mainAuthor = new SimpleStringProperty(this.mainAuthorStr);
		this.otherAuthor = new SimpleStringProperty(this.otherAuthorStr);
		this.fullAuthor = new SimpleStringProperty(this.fullAuthorStr);
		this.authorNum = new SimpleIntegerProperty(this.authorNumInt);

		this.year = new SimpleStringProperty(year);
		
		parseArxivId(arxivid);
		this.arxivId = new SimpleStringProperty(this.arxivIdStr); // show ArXiv version number in TableView
		// this.arxivId = new SimpleStringProperty(this.arxivIdNoVerStr); // not show ArXiv version number in TableView
		
		this.doi = new SimpleStringProperty(doi);

		this.bibCode = new SimpleStringProperty("");
				
		this.listChildrenBibCodeArray = new ArrayList<String>();
		this.listChildrenBibCode = new SimpleStringProperty("");

		this.listChildrenBibCodeFilteredArray = new ArrayList<String>();
		this.listChildrenBibCodeFiltered = new SimpleStringProperty("");
		
		this.listChildrenIdArray = new ArrayList<Integer>();
		this.listChildrenId = new SimpleStringProperty("");
		
	}

	// this constructor is for debug purpose only, and debugFlag should be "debug"
	Paper(int id, String title, String titleNickName, String author, String year, String arxivid, String doi, String debugFlag, String bibCode, ArrayList<String> listChildrenBibCodeArray) {
		this(id, title, titleNickName, author, year, arxivid, doi); // call the original instructor
		if (!debugFlag.contentEquals("debug")) {
			throw new RuntimeException("the debugFlag should be 'debug'");
		}
		
		this.setBibCode(bibCode);
		this.setListChildrenBibCodeArray(listChildrenBibCodeArray);

	}
	
	private String removeBrackets(String strIn){

		strIn = strIn.trim();
		if ((strIn.indexOf("{") == 0 ) && (strIn.lastIndexOf("}") == strIn.length()-1)) { // if both the first and last characters are "{" and "}"
			return strIn.substring(1, strIn.lastIndexOf("}")).trim();
		}
		return strIn;
	}
	
	// parse authors in string
	// update mainAuthorStr and authorNumInt
	private void parseAuthors(String authorsStr){

		String iAuthor;
		int count;

		count = 1;
		mainAuthorStr = "";
		otherAuthorStr = "";
		fullAuthorStr = "";
		while (authorsStr.indexOf(" and ") > -1) {
			int iPosi = authorsStr.indexOf(" and ");
			iAuthor = removeBrackets(authorsStr.substring(0, iPosi));
			fullAuthorStr = fullAuthorStr + iAuthor + " || "; 
			authorsStr = authorsStr.substring(iPosi + 5, authorsStr.length()); // remove iAuthor

			if (count==1) {
				this.mainAuthorStr = iAuthor;
			}else { // for count >= 2
				this.otherAuthorStr = this.otherAuthorStr + iAuthor + " || ";
			}
			count+=1;
		}

		iAuthor = removeBrackets(authorsStr); // the last author
		fullAuthorStr = fullAuthorStr + iAuthor;		

		if (count==1) {
			this.mainAuthorStr = iAuthor;
		}else { // for count >= 2
			this.otherAuthorStr = this.otherAuthorStr + iAuthor;
		}

		authorNumInt = count;

		return;
	}
	

	private void parseArxivId(String arxivIdStr){
		
		arxivIdStr = arxivIdStr.trim();
		
		if (arxivIdStr.equals("")) {
			this.arxivIdNoVerStr = "";
			return;
		}
		
		if (arxivIdStr.substring(0, 6).equals("arXiv:")) {
			arxivIdStr = arxivIdStr.substring(6, arxivIdStr.length()).trim(); // remove the "arXiv:" at the beginning, if any
		}
		
		this.arxivIdStr = arxivIdStr;
			
		int vPosi = arxivIdStr.indexOf("v"); // position of "v" character
		
		if (vPosi == -1) { // arxivIdStr doesn't contain version number; Or arxivIdStr is empty string
			this.arxivIdNoVerStr = arxivIdStr; 
		}else {
			this.arxivIdNoVerStr = removeBrackets(arxivIdStr.substring(0, vPosi)); // remove version number from ArXiv No.
		}
	}
	
	public String generateConstructorInput(boolean debugFlag) {
		// debugFlag = false : get constructor input
		// debugFlag = true : get constructor input after the webscrapping process (for debugging)
		
		String stringTotal = "new Paper(";
		
		stringTotal = stringTotal + this.getIdNum();
		stringTotal = stringTotal + ", \"" + this.getTitle() + "\"";
		stringTotal = stringTotal + ", \"" + this.getTitleNickName() + "\"";
		stringTotal = stringTotal + ", \"" + this.getMainAuthor() + "\"";
		stringTotal = stringTotal + ", \"" + this.getYear() + "\"";
		
		if (this.getArxivId() == null) {
			stringTotal = stringTotal + ", \"" + "" + "\"";
		}else {
			stringTotal = stringTotal + ", \"" + this.getArxivId() + "\"";
		}
		
		stringTotal = stringTotal + ", \"" + this.getDoi() + "\"" + "\n";
		
		if (debugFlag ==true) {
			stringTotal = stringTotal + ", \"debug\"";
			stringTotal = stringTotal + ", \"" + this.getBibCode() + "\"";
			stringTotal = stringTotal + ", new ArrayList<String>(Arrays.asList(\"XXX\", \"YYY\"))";
		}
		
		stringTotal = stringTotal + "),";
		
		return stringTotal;
	}

	// get_xxx() functions
	public int getId() { // Don't delete this sub-program, and don't rename, or TableView will have error!!
		return id.get();
	}

	public int getIdNum() { // Don't delete this sub-program, and don't rename, or TableView will have error!!
		return idNum;
	}

	public String getTitle() {
		return title.get();
	}

	public String getTitleNickName() {
		return titleNickName.get();
	}
	
	public String getMainAuthor() {
		return mainAuthor.get();
	}

	public String getOtherAuthor() {
		return otherAuthor.get();
	}

	public String getFullAuthor() {
		return fullAuthor.get();
	}

	public int getAuthorNum() {
		return authorNum.get();
	}

	public String getYear() {
		return year.get();
	}

	public String getArxivId() {
		return arxivId.get();
	}

	
	public String getArxivIdStr() {
		return arxivIdStr;
	}

	public String getArxivIdNoVerStr() {
		return arxivIdNoVerStr;
	}
	
	public String getDoi() {
		return doi.get();
	}

	public String getBibCode() {
		return bibCode.get();
	}
	
	public String getListChildrenBibCode() {
		return listChildrenBibCode.get();
	}

	public String getListChildrenBibCodeFiltered() {
		return listChildrenBibCodeFiltered.get();
	}
	
	public ArrayList<String> getListChildrenBibCodeArray() {
		return listChildrenBibCodeArray;
	}

	public ArrayList<String> getListChildrenBibCodeFilteredArray() {
		return listChildrenBibCodeFilteredArray;
	}
	
	public String getListChildrenId() {
		return listChildrenId.get();
	}
	
	public ArrayList<Integer> getListChildrenIdArray() {
		return listChildrenIdArray;
	}
	
	// set_xxx() functions
	public void setId(int idIn) { // Don't delete this sub-program, and don't rename, or TableView will have error!!
		id.set(idIn);
	}

	public void setTitle(String titleIn) {
		title.set(titleIn);
	}

	public void setTitleNickName(String titleNickNameIn) {
		titleNickName.set(titleNickNameIn);
	}
	
	public void setMainAuthor(String authorIn) {
		mainAuthor.set(authorIn);
	}

	public void setOtherAuthor(String authorIn) {
		otherAuthor.set(authorIn);
	}

	public void setFullAuthor(String authorIn) {
		fullAuthor.set(authorIn);
	}

	public void setAuthorNum(int authorNumIn) {
		authorNum.set(authorNumIn);
	}

	public void setYear(String yearIn) {
		year.set(yearIn);
	}

	public void setArxiv(String arxivIdIn) {
		arxivId.set(arxivIdIn);
	}

	public void setDoi(String doiIn) {
		doi.set(doiIn);
	}

	public void setBibCode(String bibCodeIn) {
		bibCode.set(bibCodeIn);
	}
	
	public void setListChildrenBibCode(String listChildrenBibCodeIn) {
		listChildrenBibCode.set(listChildrenBibCodeIn);
	}

	public void setListChildrenBibCodeFiltered(String listChildrenBibCodeFilteredIn) {
		listChildrenBibCodeFiltered.set(listChildrenBibCodeFilteredIn);
	}
	
	public void setListChildrenBibCodeArray(ArrayList<String> listChildrenBibCodeArrayIn) {
		this.listChildrenBibCodeArray = listChildrenBibCodeArrayIn;
		
		String fullList = "";
		for (String childBibCode : listChildrenBibCodeArrayIn) {
			if (listChildrenBibCodeArrayIn.indexOf(childBibCode) != (listChildrenBibCodeArrayIn.size() -1)) { // if childBibCode is not the last one
				fullList = fullList + childBibCode + " || "; 	
			}else { // if childBibCode is the last one
				fullList = fullList + childBibCode;
			}
			
		}
		setListChildrenBibCode(fullList);
		return;
	}

	public void setListChildrenBibCodeFilteredArray(ArrayList<String> listChildrenBibCodeFilteredArrayIn) {
		this.listChildrenBibCodeFilteredArray = listChildrenBibCodeFilteredArrayIn;
		
		String fullList = "";
		for (String childBibCodeFiltered : listChildrenBibCodeFilteredArrayIn) {
			if (listChildrenBibCodeFilteredArrayIn.indexOf(childBibCodeFiltered) != (listChildrenBibCodeFilteredArrayIn.size() -1)) { // if childBibCode is not the last one
				fullList = fullList + childBibCodeFiltered + " || "; 	
			}else { // if childBibCode is the last one
				fullList = fullList + childBibCodeFiltered;
			}
			
		}
		setListChildrenBibCodeFiltered(fullList);
		return;
	}
	
	public void setListChildrenId(String listChildrenIdIn) {
		listChildrenId.set(listChildrenIdIn);
	}
	
	public void setListChildrenIdArray(ArrayList<Integer> listChildrenIdArrayIn) {
		this.listChildrenIdArray = listChildrenIdArrayIn;
		
		String fullList = "";
		for (int childId : listChildrenIdArrayIn) {
			
			if (listChildrenIdArrayIn.indexOf(childId) != (listChildrenIdArrayIn.size() -1)) { // if childId is not the last one
				fullList = fullList + childId + " || "; 				
			}else { // if childId is the last one
				fullList = fullList + childId;	
			}
			 
		}
		setListChildrenId(fullList);
		return;
	}
	
	// get_xxx() functions (i.e. for column setup)
	public SimpleIntegerProperty getIdProperty() {
		return id;
	}

	public SimpleStringProperty getTitleProperty() {
		return title;
	}

	public SimpleStringProperty getTitleNickNameProperty() {
		return titleNickName;
	}
	
	public SimpleStringProperty getMainAuthorProperty() {
		return mainAuthor;
	}

	public SimpleStringProperty getOtherAuthorProperty() {
		return otherAuthor;
	}

	public SimpleStringProperty getFullAuthorProperty() {
		return fullAuthor;
	}

	public SimpleIntegerProperty getAuthorNumProperty() {
		return authorNum;
	}

	public SimpleStringProperty getYearProperty() {
		return year;
	}

	public SimpleStringProperty getArxivIdProperty() {
		return arxivId;
	}

	public SimpleStringProperty getDoiProperty() {
		return doi;
	}
	
	public SimpleStringProperty getBibCodeProperty() {
		return bibCode;
	}

	public SimpleStringProperty getListChildrenBibCodeProperty() {
		return listChildrenBibCode;
	}

	public SimpleStringProperty getListChildrenBibCodeFilteredProperty() {
		return listChildrenBibCodeFiltered;
	}
	
	public SimpleStringProperty getListChildrenIdProperty() {
		return listChildrenId;
	}
	
}