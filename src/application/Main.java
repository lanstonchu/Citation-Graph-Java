/**
 * @author Lanston Hau Man Chu (hchu34@wisc.edu)
 * @version 1.0
 * Class: CS 400 - Programming III
 * Author's email: hchu34@wisc.edu
 * Project description: This is the GUI of the final project. For the structure of the GUI, please refer to the readme file. 
 */

package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.Tab;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import org.jbibtex.*;

import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;



public class Main extends Application {
	// store any command-line arguments that were entered.
	// NOTE: this.getParameters().getRaw() will get these also
	private List<String> args;

	private static final int WINDOW_WIDTH = 1100;
	private static final int WINDOW_HEIGHT = 400;
	private static final String APP_TITLE = "Citation Graph";
	private static final String DEFAULT_PATH = "/Desktop";
	private static final boolean EDITABLE_TRUE = true;
	private static final boolean EDITABLE_FALSE = false;

	private TextArea outputArea = new TextArea();
	private StringProperty statusText = new SimpleStringProperty();
	
	final Region marginArea = new Region(); // for the empty space between
	final Label labelMargin = new Label(""); // for the empty space between

	GraphJFrame myFrame = new GraphJFrame();

	private int id; // paper id counter

	
	// unhide below 2 lines for debugging; hide in normal use
	// private final int debugFlag = 1;
	// private final ObservableList<Paper> data = createSampleData();

	
	
	// unhide below 2 lines for normal use; hide when debugging
	private final int debugFlag = 0;
	private final ObservableList<Paper> data = FXCollections.observableArrayList(); // create blank list

	
	// For graph drawing
	final ArrayList<Integer> fromVertices = new ArrayList<Integer>(); // from-vertices to create graph
	final ArrayList<Integer> toVertices = new ArrayList<Integer>(); // to-vertices to create graph
	final HashMap<Integer, Paper> paperMapping = new HashMap<Integer, Paper>(); // for fast mapping purpose in some graph theory transversal algorithm
	final HashMap<Integer, ArrayList<Integer>> childrenMapping = new HashMap<Integer, ArrayList<Integer>>(); // for fast mapping purpose in some graph theory transversal algorithm

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		final HBox hBoxOverTab = new HBox();
		final VBox vboxMainLeft = new VBox();
		final TabPane tabPane = new TabPane();

		vboxMainLeft.getChildren().addAll(new Label("Status: "), outputArea);
		vboxMainLeft.setAlignment(Pos.CENTER_LEFT);

		// bind the text area
		outputArea.textProperty().bind(statusText);
		
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE); // not allow user to close tab
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();

		// ------- PART I - 1st Tab: Input ---------

		final HBox hbox1 = new HBox();

		// build the file chooser
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home"),DEFAULT_PATH)); // default path
		final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BIB files (*.bib)", "*.bib"); // default extension
		fileChooser.getExtensionFilters().add(extFilter);

		// layout
		final Label labelFileNameLeft = new Label("The File imported: ");
		final Label labelFileName = new Label("    (No file be chosen)    ");
		labelFileName.setTextFill(Color.web("#0076a3")); // updated label color


		HBox.setHgrow(marginArea, Priority.ALWAYS);
		final Button buttonImport = new Button("Import .bib file");

		hbox1.getChildren().addAll(labelFileNameLeft, labelFileName, buttonImport);
		hbox1.setAlignment(Pos.CENTER);

		final Tab tab1 = new Tab("Input", hbox1);
		tabPane.getTabs().add(tab1);

		// ------- PART II - 2nd Tab: Table ---------

		final HBox hbox2a = new HBox();
		final TableView<Paper> table2 = new TableView<Paper>();
		table2.setEditable(true); // make the table editable

		final Label label2a = new Label("Reference List");
		label2a.setFont(new Font("Arial", 20));
		final Label label2b = new Label("      (Double click to edit)");
		label2b.setFont(new Font("Arial", 12));

		hbox2a.getChildren().addAll(label2a, label2b);
		hbox2a.setAlignment(Pos.BOTTOM_LEFT);

		RadioButtonGroup rbg2b = new RadioButtonGroup("Title?    ", new ArrayList<String>(Arrays.asList("Title", "Nickname")));
		final HBox hbox2b = rbg2b.getHBox();
		final ToggleGroup group2b = rbg2b.getToggleGroup();
		rbg2b.setAlignment(Pos.BOTTOM_LEFT);

		RadioButtonGroup rbg2c = new RadioButtonGroup("                         References?    ", new ArrayList<String>(Arrays.asList("BibCode (Full)", "BibCode (Filtered)    ", "ID")));
		final HBox hbox2c = rbg2c.getHBox();
		final ToggleGroup group2c = rbg2c.getToggleGroup();
		rbg2c.setAlignment(Pos.BOTTOM_RIGHT);

		final HBox hbox2bc = new HBox();
		hbox2bc.getChildren().addAll(hbox2b, marginArea, hbox2c);

		Function<Paper, Property<String>> myFunctionString;
		Function<Paper, Property<Number>> myFunctionNumber; // SimpleIntegerProperty implements Property<Number> rather than Property<Integer>

		// column for ID
		myFunctionNumber = (Paper e)-> {return e.getIdProperty();};
		TableColumn<Paper, Number> idCol  = TableColumns.create("ID", 20, myFunctionNumber, EDITABLE_FALSE);
		// titleCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// column for Title
		myFunctionString = (Paper e)-> {return e.getTitleProperty();};
		TableColumn<Paper, String> titleCol = TableColumns.create("Title", 140, myFunctionString, EDITABLE_TRUE);
		titleCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// column for Title Nickname
		myFunctionString = (Paper e)-> {return e.getTitleNickNameProperty();};
		TableColumn<Paper, String> titleNickNameCol = TableColumns.create("Paper \n Nickname", 140, myFunctionString, EDITABLE_TRUE);
		titleNickNameCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// bind the column width of titleCol and titleNickNameCol  
		titleNickNameCol.prefWidthProperty().bind(titleCol.widthProperty());
		titleCol.prefWidthProperty().bind(titleNickNameCol.widthProperty());
		
		// column for main author
		myFunctionString = (Paper e)-> {return e.getMainAuthorProperty();};
		TableColumn<Paper, String> mainAuthorCol = TableColumns.create("Main", 100, myFunctionString, EDITABLE_TRUE);
		mainAuthorCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// column for other author(s)
		myFunctionString = (Paper e)-> {return e.getOtherAuthorProperty();};
		TableColumn<Paper, String> otherAuthorCol = TableColumns.create("Other", 150, myFunctionString, EDITABLE_TRUE);
		otherAuthorCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// column for number of author(s)
		myFunctionNumber = (Paper e)-> {return e.getAuthorNumProperty();};
		TableColumn<Paper, Number> authorNumCol = TableColumns.create("#", 20, myFunctionNumber, EDITABLE_TRUE);
		authorNumCol.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter())); // make the column editable

		// column for year
		myFunctionString = (Paper e)-> {return e.getYearProperty();};
		TableColumn<Paper, String> yearCol  = TableColumns.create("Year", 50, myFunctionString, EDITABLE_TRUE);
		yearCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// column for ArVix ID
		myFunctionString = (Paper e)-> {return e.getArxivIdProperty();};
		TableColumn<Paper, String> arxivIdCol  = TableColumns.create("ArXiv ID", 50, myFunctionString, EDITABLE_TRUE);
		arxivIdCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable	

		// column for DOI
		myFunctionString = (Paper e)-> {return e.getDoiProperty();};
		TableColumn<Paper, String> doiCol  = TableColumns.create("DOI", 50, myFunctionString, EDITABLE_TRUE);
		doiCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// column for BibCode
		myFunctionString = (Paper e)-> {return e.getBibCodeProperty();};
		TableColumn<Paper, String> bibCodeCol  = TableColumns.create("BibCode", 50, myFunctionString, EDITABLE_FALSE);
		// bibCodeCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// column for Children's BibCode (full)
		myFunctionString = (Paper e)-> {return e.getListChildrenBibCodeProperty();};
		TableColumn<Paper, String> childrenBibCodeCol  = TableColumns.create("References \n BibCode (Full)", 120, myFunctionString, EDITABLE_FALSE);
		// childrenBibCodeCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// column for Children's BibCode (filtered)
		myFunctionString = (Paper e)-> {return e.getListChildrenBibCodeFilteredProperty();};
		TableColumn<Paper, String> childrenBibCodeFilteredCol  = TableColumns.create("References \n BibCode (Filtered)", 120, myFunctionString, EDITABLE_FALSE);
		// childrenBibCodeCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable
		
		// column for Childre's ID
		myFunctionString = (Paper e)-> {return e.getListChildrenIdProperty();};
		TableColumn<Paper, String> childrenIdCol  = TableColumns.create("References \n ID (Filtered)", 120, myFunctionString, EDITABLE_FALSE);
		// childrenBibCodeCol.setCellFactory(TextFieldTableCell.forTableColumn()); // make the column editable

		// bind the column width of titleCol and titleNickNameCol  
		childrenIdCol.prefWidthProperty().bind(childrenBibCodeCol.widthProperty());
		childrenBibCodeCol.prefWidthProperty().bind(childrenBibCodeFilteredCol.widthProperty());
		childrenBibCodeFilteredCol.prefWidthProperty().bind(childrenIdCol.widthProperty());
		
		// Binding data and table
		table2.setItems(data);

		// Adding columns
		final TableColumn authorHeadCol = new TableColumn("Author(s)");
		authorHeadCol.getColumns().addAll(mainAuthorCol, otherAuthorCol, authorNumCol);
		
		table2.getColumns().addAll(idCol, titleCol, titleNickNameCol, authorHeadCol, yearCol, 
				childrenBibCodeCol, childrenBibCodeFilteredCol, childrenIdCol, arxivIdCol, doiCol, bibCodeCol);
		
		titleNickNameCol.setVisible(false);
		childrenBibCodeFilteredCol.setVisible(false);
		childrenIdCol.setVisible(false);

		// Create button to add paper in data
		final HBox hbox2d = new HBox();

		final TextField addTitle = new TextField();
		addTitle.setPromptText("Title");
		addTitle.setPrefWidth(titleCol.getPrefWidth());
		final TextField addAllAuthor = new TextField();
		addAllAuthor.setPrefWidth(authorHeadCol.getPrefWidth());
		addAllAuthor.setPromptText("Person1 and Person2 and Person3");
		final TextField addYear = new TextField();
		addYear.setPrefWidth(yearCol.getPrefWidth());
		addYear.setPromptText("Year");

		final Label label2Error = new Label("");
		label2Error.setTextFill(Color.web("#eb5534")); // updated label color

		final Button addButton = new Button("Add");

		hbox2d.getChildren().addAll(addTitle, addAllAuthor, addYear, addButton, label2Error);
		hbox2d.setSpacing(3);

		// Create button to remove paper from data
		final HBox hbox2e = new HBox();

		final Label label2Remove = new Label("To remove ");

		final TextField removeId = new TextField();
		removeId.setPromptText("ID");
		removeId.setPrefWidth(idCol.getPrefWidth());

		final Label label2Error_b = new Label("");
		final Button removeButton = new Button("Go");

		hbox2e.getChildren().addAll(label2Remove, removeId, removeButton, label2Error_b);

		// Create button to do web-scraping
		final Button webScrappingButton = new Button("Web scrapping to get BibCode and references");
		final Button exportStringButton = new Button("Export as string (for debugging)");
		exportStringButton.setStyle("-fx-text-fill: gray");

		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.setAlignment(Pos.CENTER);
		
		if (debugFlag == 1) {
			vbox.getChildren().addAll(hbox2a, hbox2bc, table2, hbox2d, hbox2e, exportStringButton, webScrappingButton); // have an extra export-string button when debugging
		}else if(debugFlag == 0) {
			vbox.getChildren().addAll(hbox2a, hbox2bc, table2, hbox2d, hbox2e, webScrappingButton);
			// vbox.getChildren().addAll(hbox2a, hbox2bc, table2, hbox2d, hbox2e, webScrappingButton, exportStringButton); // have an extra export-string button when debugging
		}else {
			throw new RuntimeException("Please input either 0 for 1 for the debug flag");
		}

		final Tab tab2= new Tab("Table", vbox);
		tabPane.getTabs().add(tab2);

		// ------- PART III - 3rd Tab: Graph ---------

		final HBox hbox3d = new HBox();
		final HBox hbox3e = new HBox();
		
		final VBox vbox3R = new VBox();

		final Button buttonRefresh = new Button("Generate / Refresh");

		// Group A: Title vs Nickname
		RadioButtonGroup rbg3a = new RadioButtonGroup("Show nickname?    ", new ArrayList<String>(Arrays.asList("Title     ", "Nickname  ")));
		final HBox hbox3a = rbg3a.getHBox();
		final ToggleGroup group3a = rbg3a.getToggleGroup();

		// Group B: Show author vs hide author
		RadioButtonGroup rbg3b = new RadioButtonGroup("Main author?          ", new ArrayList<String>(Arrays.asList("Show         ", "Hide         ")));
		final HBox hbox3b = rbg3b.getHBox();
		final ToggleGroup group3b = rbg3b.getToggleGroup();

		// Group C: Show year vs hide year
		RadioButtonGroup rbg3c = new RadioButtonGroup("Year?                       ", new ArrayList<String>(Arrays.asList("Show         ", "Hide         ")));
		final HBox hbox3c = rbg3c.getHBox();
		final ToggleGroup group3c = rbg3c.getToggleGroup();

		// Group D: Year range		
		final TextField  textIn3d1 = new TextField(); // year from
		final TextField  textIn3d2 = new TextField(); // year to
		final TextField  textIn3d3 = new TextField(); // paper root from
		textIn3d1.setPrefWidth(60); // set TextField width
		textIn3d2.setPrefWidth(60);
		textIn3d3.setPrefWidth(60);

		hbox3d.getChildren().addAll(new Label("Year from "), textIn3d1, new Label(" to "), textIn3d2);
		textIn3d1.setPromptText("YYYY");
		textIn3d2.setPromptText("YYYY");

		final Label label3Error = new Label("");
		label3Error.setTextFill(Color.web("#eb5534")); // updated label color

		// Group E: Specific root
		hbox3e.getChildren().addAll(new Label("Only show ancestors of "), textIn3d3, new Label(" (paper ID)"));
		textIn3d3.setPromptText("ID");

		// Group F: Show singleton vs hide singleton
		RadioButtonGroup rbg3f = new RadioButtonGroup("Singleton?                  ", new ArrayList<String>(Arrays.asList("Hide         ", "Show         ")));
		final HBox hbox3f = rbg3f.getHBox();
		final ToggleGroup group3f = rbg3f.getToggleGroup();
				
		vbox3R.getChildren().addAll(hbox3a, hbox3b, hbox3c, hbox3d, label3Error, hbox3e, buttonRefresh, hbox3f);

		hbox3a.setAlignment(Pos.CENTER_RIGHT);
		hbox3b.setAlignment(Pos.CENTER_RIGHT);
		hbox3c.setAlignment(Pos.CENTER_RIGHT);
		hbox3d.setAlignment(Pos.CENTER_RIGHT);
		label3Error.setAlignment(Pos.CENTER_RIGHT);
		hbox3e.setAlignment(Pos.CENTER_RIGHT);
		// buttonRefresh.setAlignment(Pos.CENTER);
		hbox3f.setAlignment(Pos.CENTER_RIGHT);
		
		vbox3R.setAlignment(Pos.CENTER); // for button 

		final Tab tab3= new Tab("Graph", vbox3R);
		tabPane.getTabs().add(tab3);
		
		// VBox vBox = new VBox(tabPane);
		
		hBoxOverTab.getChildren().addAll(vboxMainLeft, tabPane);
		
		final Scene scene = new Scene(hBoxOverTab, WINDOW_WIDTH, WINDOW_HEIGHT);

		primaryStage.setScene(scene);
		primaryStage.setTitle(APP_TITLE);

		primaryStage.show();



		// ### Testing codes : START ###
		if (debugFlag == 1) {
			parseChildrenBibCodeToID(data);
		}else if(debugFlag == 0) {
			// do nothing
		}else {
			throw new RuntimeException("Please input either 0 for 1 for the debug flag");
		}
		// ### Testing codes : END ###



		// PART VI: buttons' definition


		// listener for statusText
		// i.e. to be bind to the text area 
		statusText.addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				// from stackoverflow.com/a/30264399/1032167
				// for some reason setScrollTop will not scroll properly
				//consoleTextArea.setScrollTop(Double.MAX_VALUE);
				outputArea.selectPositionCaret(outputArea.getLength()); 
				outputArea.deselect(); 
			}
		});
		
		// clicking buttonImport of Tab1
		buttonImport.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {			
				
				System.out.println("A file is now being chosen ... ");
				updateStatusText(statusText, "A file is now being chosen ... ");
				
				File bibFile = fileChooser.showOpenDialog(primaryStage);			

				if (bibFile != null) {
					String bibFileName = bibFile.getName().toString();

					// update the text next to button		        
					labelFileName.setText(bibFileName);

					System.out.println("The File is chosen. ");
					updateStatusText(statusText, "The File is chosen. ");
					selectionModel.select(tab2); //select by object

					// Parse the .bib file
					BibTeXDatabase database;

					try {
						database = JbibtexExtra.parseBibTeX(bibFile); // parse the .bib file to get BibTeXDatabase object

						Map<Key, BibTeXEntry> entryMap = database.getEntries();	    
						Collection<BibTeXEntry> entries = entryMap.values(); // entries include the papers in the .bib file

						String titleStr, titleNickNameStr, authorStr, yearStr, arxivIdStr, doiStr;

						data.clear(); // clear papers' info

						id = 0;
						for(BibTeXEntry entry : entries){ // for each paper in the .bib file
							id += 1;
							// valueTitle = entry.getField(BibTeXEntry.KEY_TITLE);
							try {
								titleStr = entry.getField(BibTeXEntry.KEY_TITLE).toUserString();	
							}catch (NullPointerException eNull) {
								titleStr = "";
							}

							try {
								titleNickNameStr = entry.getKey().toString();

							}catch (NullPointerException eNull) {
								titleNickNameStr = ""; 
							}

							try {
								authorStr = entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString();
							}catch (NullPointerException eNull) {
								authorStr = "";
							}

							try {
								yearStr = entry.getField(BibTeXEntry.KEY_YEAR).toUserString();
							}catch (NullPointerException eNull) {
								yearStr = "";
							}

							try {
								arxivIdStr = entry.getField(new Key("arxivId")).toUserString();
							}catch (NullPointerException eNull) {
								arxivIdStr = "";
							}

							try {
								doiStr = entry.getField(BibTeXEntry.KEY_DOI).toUserString();
							}catch (NullPointerException eNull) {
								doiStr = ""; 
							}

							// add paper to the table data
							data.add(new Paper(id, titleStr,titleNickNameStr, authorStr,yearStr, arxivIdStr, doiStr));
						}

					} catch (IOException | ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						throw new RuntimeException("Some error happened.");
					}



				}
			}
		});

		// Showing full title or nickname
		group2b.selectedToggleProperty().addListener(new ChangeListener<Toggle>() { 
			public void changed(ObservableValue<? extends Toggle> ob, Toggle o, Toggle n) 
			{ 

				RadioButton rb = (RadioButton)group2b.getSelectedToggle(); 

				if (rb != null) { 
					String str = rb.getText().trim(); 

					if (str.equals("Title")) {                  	
						titleCol.setVisible(true);
						titleNickNameCol.setVisible(false);

					}else if(str.equals("Nickname")) {
						titleCol.setVisible(false);
						titleNickNameCol.setVisible(true);
					}else {
						throw new RuntimeException("Shouldn't reach this line");
					}

				} 
			} 
		}); 

		// Showing children BibCode or children ID
		group2c.selectedToggleProperty().addListener(new ChangeListener<Toggle>() { 
			public void changed(ObservableValue<? extends Toggle> ob, Toggle o, Toggle n) 
			{ 

				RadioButton rb = (RadioButton)group2c.getSelectedToggle(); 

				if (rb != null) { 
					String str = rb.getText().trim(); 

					if (str.equals("BibCode (Full)")) {
						childrenBibCodeCol.setVisible(true);
						childrenBibCodeFilteredCol.setVisible(false);
						childrenIdCol.setVisible(false);
					}else if (str.equals("BibCode (Filtered)")) {
						childrenBibCodeCol.setVisible(false);
						childrenBibCodeFilteredCol.setVisible(true);
						childrenIdCol.setVisible(false);
					}else if(str.equals("ID")) {
						childrenBibCodeCol.setVisible(false);
						childrenBibCodeFilteredCol.setVisible(false);
						childrenIdCol.setVisible(true);
					}else {
						throw new RuntimeException("Shouldn't reach this line");
					}

				} 
			} 
		}); 

		// clicking addButton of Tab2
		addButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {

					String yearStr = addYear.getText().trim();

					if (!yearStr.contentEquals("")) {
						int yearNum = Integer.parseInt(yearStr); // this convertion is used to catch exception limiting "year should be integer"	
					}					


					int maxId = getMaxID(data);

					int idNew = maxId + 1;
					data.add(new Paper(
							idNew,
							addTitle.getText(),
							"Paper " + Integer.toString(idNew),
							addAllAuthor.getText(),
							yearStr,
							"",
							""));
					label2Error.setText("");
					id = idNew; // update id if there is no error

					addTitle.clear();
					addAllAuthor.clear();

					updateStatusText(statusText, "Paper " + idNew + " is added.");
					
				}catch(NumberFormatException e2) {
					// System.out.println("this is error man");
					label2Error.setText("Year should be integer");
				}			

				addYear.clear();
			}
		});

		// clicking removeButton of Tab2
		removeButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {

				try {
					// remove paper from data if matching the ID
					int removeIdNum = Integer.parseInt(removeId.getText().trim());
					data.removeIf(iPaper -> (iPaper.getIdNum() == removeIdNum)); // lambda expression
					removeId.clear();
					
					updateStatusText(statusText, "Paper " + removeIdNum + " is removed.");
					
				}catch(NumberFormatException e2) {
					label2Error_b.setText("ID should be integer");
				}
			}
		});

		// clicking webScrappingButton of Tab2
		webScrappingButton.setOnAction(new EventHandler<ActionEvent>() { 

			String linkPrefix = "https://ui.adsabs.harvard.edu/#abs/";
			String absLinkSuffix = "/abstract";
			String refLinkSuffix = "/references";

			String projectPathEclipse = System.getProperty("user.dir");
			Path projectPath = Paths.get(projectPathEclipse, "../");
			// String driverPath = projectPathEclipse + "/chrome_driver/chromedriver_win32.exe"; // use this line in Eclipse
			String driverPath = projectPath + "/chrome_driver/chromedriver_win32.exe"; // use this line in cmd
			
			@Override
			public void handle(ActionEvent e) {

				int numPapers = data.size(); // number of papers

				System.out.println(numPapers);

				// WEB SCRAPPING
				try {

					System.setProperty("webdriver.chrome.driver", driverPath);

					WebDriver driver = new ChromeDriver();
					driver.get("http://www.google.com/");
					Thread.sleep(3000);  // Wait for 3 seconds

					int j = 0;
					for (Paper paper : data) {
						j+=1;

						// print out the index of paper being scrapped
						System.out.println("Paper: Scrapping " + j + " of " + numPapers);
						updateStatusText(statusText, "Paper: Scrapping " + j + " of " + numPapers);
						
						String arXivID = paper.getArxivIdNoVerStr();

						if (arXivID.trim()=="") { // jump to next paper if there is no arXivID
							continue;
						}

						// get Bibcode and title of the paper
						String absLink = linkPrefix + arXivID + absLinkSuffix;
						driver.get(absLink);

						Thread.sleep(3000);  // Wait for 3 seconds

						String pageSourceAbs = driver.getPageSource();
						String bibCode = search_by_head_tail(pageSourceAbs,"bibcode=","\"");
						String ScrappedTitle = search_by_head_tail(pageSourceAbs,"<title>","</title>");

						// use Chrome to check Reference page
						String refLink = linkPrefix + arXivID + refLinkSuffix;

						// get reference info
						driver.get(refLink);
						Thread.sleep(3000);

						// get source code
						String pageSourceRef=driver.getPageSource();

						String num_Ref = search_by_head_tail(pageSourceRef,"References\n","</span>\n");
						num_Ref = search_by_head_tail(num_Ref, "(", ")");
						
						// print out the no. of references
						System.out.println("Refrences: (" + num_Ref + ")");
						updateStatusText(statusText, "Refrences: (" + num_Ref + ")");
						
						// find the position of papers' titles
						ArrayList<Integer> positions = new ArrayList<Integer>();
						positions.add(0);

						int posi = pageSourceRef.indexOf("h3 class");
						while (posi >= 0) {
							positions.add(posi);
							posi = pageSourceRef.indexOf("h3 class", posi + 1);
						}
						int num_papers_one_page = positions.size() - 1;

						String link_partial;
						ArrayList<String> list_children = new ArrayList<String>(); 
						for (int i=0; i < num_papers_one_page; i++) {
							int posi_start=positions.get(i);
							int posi_end=positions.get(i+1);

							link_partial = pageSourceRef.substring(posi_start, posi_end);

							int posi_a_start = link_partial.lastIndexOf("<a href=\"#"); // search for the last "<a href=\"#" at the end 
							link_partial = link_partial.substring(posi_a_start + 10, link_partial.length());
							int posi_a_end = link_partial.indexOf("\" class=\"");
							link_partial = link_partial.substring(0, posi_a_end);

							String bibCode_child_i = search_by_head_tail(link_partial,"abs/","/abstract");
							list_children.add(bibCode_child_i);							
						}

						paper.setBibCode(bibCode);
						paper.setListChildrenBibCodeArray(list_children);
					}

					driver.quit();
					updateStatusText(statusText, "Web scrapping done. \n"
							+ "Please go to tab \"Graph\" to draw graph.");


				}catch (InterruptedException e1) {
					throw new RuntimeException("InterruptedException");
				}catch (SessionNotCreatedException e2) {
					e2.printStackTrace();

					System.out.println("");
					System.out.println("SessionNotCreatedException: It is very likely that your Google Chrome Driver is not in line with your current version (nor OS)");
					System.out.println("Please visit    `https://sites.google.com/a/chromium.org/chromedriver/downloads`    to download the driver that suits your Google Chrome version, ");
					System.out.println("and then replace the file in `" + driverPath + "` by the downloaded file");

					updateStatusText(statusText, "");
					updateStatusText(statusText, "SessionNotCreatedException: It is very likely that your Google Chrome Driver is not in line with your current version (nor OS)");
					updateStatusText(statusText, "Please visit    `https://sites.google.com/a/chromium.org/chromedriver/downloads`    to download the driver that suits your Google Chrome version, ");
					updateStatusText(statusText, "and then replace the file in `" + driverPath + "` by the downloaded file");
					
					throw new RuntimeException("RuntimeException.");
				}


				// add matched ID to listChildrenIdArray of each papers
				parseChildrenBibCodeToID(data);

			}


		});

		// clicking exportStringButton of Tab2
		exportStringButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {

				for (Paper paper : data) {
					System.out.println(paper.generateConstructorInput(true));
				}
			}
		});

		// Graph theory
		
		// clicking buttonRefresh of Tab3
		buttonRefresh.setOnAction(new EventHandler<ActionEvent>() {
			private static final int MAX_LINE_LENGTH = 25;

			boolean showSingletonFlag;

			@Override
			public void handle(final ActionEvent e) {
				RadioButton rb3a = (RadioButton)group3a.getSelectedToggle(); // title vs. nickname
				RadioButton rb3b = (RadioButton)group3b.getSelectedToggle(); // Main author show vs. hide
				RadioButton rb3c = (RadioButton)group3c.getSelectedToggle(); // year show vs. hide
				RadioButton rb3f = (RadioButton)group3f.getSelectedToggle(); // Singleton show vs. hide

				String str3a = rb3a.getText().trim();
				String str3b = rb3b.getText().trim();
				String str3c = rb3c.getText().trim();
				String str3f = rb3f.getText().trim();
				String yearFrom = textIn3d1.getText().trim();
				String yearTo = textIn3d2.getText().trim();
				String selectedRootStr = textIn3d3.getText().trim(); // paper root from
				int yearFromNum = 0;
				int yearToNum = 9999;
				int selectedRootNum = -1;

				try {
					if (rb3a == null || rb3b == null || rb3c == null ) {
						throw new RuntimeException("The radio button(s) shouldn't be null.");	
					}

					if (!yearFrom.contentEquals("")) {
						yearFromNum = Integer.parseInt(yearFrom); // this convertion is used to catch exception limiting "year should be integer"	
					}

					if (!yearTo.contentEquals("")) {
						yearToNum = Integer.parseInt(yearTo); // this convertion is used to catch exception limiting "year should be integer"	
					}

					if (!selectedRootStr.contentEquals("")) {
						selectedRootNum = Integer.parseInt(selectedRootStr); // this convertion is used to catch exception limiting "integer"	
					}

					if (yearToNum < yearFromNum) {
						throw new IllegalArgumentException("Year-from should not be larger than Year-to");
					}
			
					try {
					Collections.sort(data, new PaperYearComparator()); // sort paper by year
					}catch(NumberFormatException e1) {
						throw new IllegalArgumentException("Please make sure all papers have the \"year\" information. \n"
								+ "Put \"0\" if you want to leave it blank.");
					}
					
					// Construct validIdWithinYears and paperMapping
					ArrayList<Integer> validIdWithinYears = new ArrayList<Integer>(); // Store ID that is within the selected year range
					paperMapping.clear();
					for (Paper paper : data) {
						String yearStr = paper.getYear();
						if(!yearStr.contentEquals("")) {
							int yearNum = Integer.parseInt(yearStr);
							if (yearNum >= yearFromNum && yearNum <= yearToNum) {
								validIdWithinYears.add(paper.getIdNum());
								paperMapping.put(paper.getIdNum(), paper);							
							}
						}
					}

					// further narrow down validIdWithinYears and construct childrenMapping
					childrenMapping.clear();
					ArrayList<Integer> childArrayFiltered = new ArrayList<Integer>();
					for (int paperId : validIdWithinYears) {

						ArrayList<Integer> childArray = paperMapping.get(paperId).getListChildrenIdArray();

						childArrayFiltered.clear();	
						for (int childPaperId : childArray) {

							if(validIdWithinYears.contains(childPaperId)) {						
								childArrayFiltered.add(childPaperId);							
							}
						}
						childrenMapping.put(paperId, (ArrayList<Integer>) childArrayFiltered.clone());
					}

					if (selectedRootNum!=-1) { // if the selected root is non-empty

						if (!validIdWithinYears.contains(selectedRootNum)) { // if the selected root is not within the year range
							throw new IllegalArgumentException("The selected root should be within the year range");
						}

						validIdWithinYears = filterAncestor(selectedRootNum, childrenMapping);						
					}


					// Construct fromVertices, toVertices and vertexLabels
					ArrayList<String> vertexLabels = new ArrayList<String>();

					// initialize vertexLabels
					for (int i = 0; i < getMaxID(data); i++) {
						vertexLabels.add("");
					}

					fromVertices.clear();
					toVertices.clear();
					for (Paper paper : data) {

						if (validIdWithinYears.contains(paper.getIdNum())) { // only add vertices for paper within the selected year range

							String titleLabel;
							if (str3a.equals("Title")) {                  	
								titleLabel = "\n" + paper.getIdNum() + "\n" + paper.getTitle();
							}else if(str3a.equals("Nickname")) {
								titleLabel = "\n" + paper.getIdNum() + "\n" + paper.getTitleNickName();
							}else {
								throw new RuntimeException("Shouldn't reach this line");
							}

							String mainAuthorLabel;
							if (str3b.equals("Show")) {                  	
								mainAuthorLabel = "-- " + paper.getMainAuthor() + " --";
							}else if(str3b.equals("Hide")) {
								mainAuthorLabel = "";
							}else {
								throw new RuntimeException("Shouldn't reach this line");
							}

							String yearLabel;
							if (str3c.equals("Show")) {                  	
								yearLabel = "-- " + paper.getYear() + " --";
							}else if(str3c.equals("Hide")) {
								yearLabel = "";
							}else {
								throw new RuntimeException("Shouldn't reach this line");
							}

							String labelLines = cutString(titleLabel, MAX_LINE_LENGTH) + "\n"
									+ cutString(mainAuthorLabel, MAX_LINE_LENGTH) + "\n"
									+ cutString(yearLabel, MAX_LINE_LENGTH);
							vertexLabels.set(paper.getIdNum()-1, labelLines);

							for (int childID : paper.getListChildrenIdArray()) { // create fromVertices and toVertices					
								if (validIdWithinYears.contains(childID)) { // check whether child-paper is within selected year range as well
									fromVertices.add(childID); // the older paper
									toVertices.add(paper.getIdNum()); // the newer paper
								}
							}
						}	
					}
					
					if (str3f.equals("Show")) {                  	
						showSingletonFlag = true;
					}else if(str3f.equals("Hide")) {
						showSingletonFlag = false;
					}else {
						throw new RuntimeException("Shouldn't reach this line");
					}

					myFrame.drawJFrame(fromVertices, toVertices, vertexLabels, validIdWithinYears, showSingletonFlag);
					
					Collections.sort(data, new PaperIdComparator()); // sort paper by ID
					
					updateStatusText(statusText, "A new graph is drawn. ");
					label3Error.setText("");
				}catch(NumberFormatException e1) {
					label3Error.setText("Year/ID should be integer.");
				}catch(IllegalArgumentException e2) {
					label3Error.setText(e2.getMessage());
				}
			}
		});

	} // end of "public void start()"

	static private String search_by_head_tail(String longText, String head, String tail) {
		// this methd will search for the first head matching the result
		// and then search for the first tail-after-1st-head
		// and return the text between first head and first tail-after-1st-head
		// e.g. search_by_head_tail("<id>123</id>", "<id>", "</id>") outputs "123"

		int headPosi = longText.indexOf(head);
		String longTextCropped = longText.substring(headPosi + head.length(), longText.length());
		int tailPosi = longTextCropped.indexOf(tail);
		String phase_extracted = longTextCropped.substring(0, tailPosi);
		return phase_extracted;
	}

	static private void parseChildrenBibCodeToID(ObservableList<Paper> data) {
		// add matched ID to listChildrenIdArray of each papers

		for (Paper paper : data) {

			ArrayList<String> list_children = paper.getListChildrenBibCodeArray();
			ArrayList<Integer> filteredChildrenId = new ArrayList<Integer>();
			ArrayList<String> filteredChildrenBibCode = new ArrayList<String>();
			
			for (String bibCodeChild : list_children) { // try to find ID of the children that exists in selected list
				for (Paper paperIn : data) {
					if (paperIn.getBibCode().equals(bibCodeChild)) {
						filteredChildrenId.add(paperIn.getIdNum());
						filteredChildrenBibCode.add(paperIn.getBibCode());
					}
				}

			}
			paper.setListChildrenIdArray(filteredChildrenId); // add the matched ID to the paper
			paper.setListChildrenBibCodeFilteredArray(filteredChildrenBibCode); // add the matched ID to the paper

		}

	}

	static private int getMaxID(ObservableList<Paper> data) {
		int maxId = 0;
		for (Paper paper: data) {
			maxId = Math.max(maxId, paper.getIdNum());
		}
		return maxId;

	}	

	static private String cutString(String strIn, int maxLength) {

		String strRemained = strIn;

		String strTotal = "";	
		while(strRemained.length() > maxLength) {

			String strFront = strRemained.substring(0, maxLength);
			int posiSpace = strFront.lastIndexOf(" ");
			
			if (posiSpace == -1) { // if there is no space in strFront, just take the entire string 
				strTotal = strTotal + strFront  + "\n"; 
				strRemained = strRemained.substring(maxLength, strRemained.length());
			}else {
				strTotal = strTotal + strRemained.substring(0, posiSpace) + "\n";
				strRemained = strRemained.substring(posiSpace + 1, strRemained.length());	
			}

			
		}
		strTotal = strTotal + strRemained; 

		return strTotal;

	}

	static private ArrayList<Integer> filterAncestor(int subRoot, HashMap<Integer, ArrayList<Integer>> childrenMapping) {
		ArrayList<Integer> listExplored = new ArrayList<Integer>();
		ArrayList<Integer> listToBeExplored = new ArrayList<Integer>();

		listToBeExplored.add(subRoot);

		while (listToBeExplored.size() >= 1) { // loop until listToBeExplored reach 0
			int iParent = listToBeExplored.get(0);
			listExplored.add(iParent);
			listToBeExplored.remove(0); // remove iParent from list


			ArrayList<Integer> iChildren = childrenMapping.get(iParent);

			for (int iChild : iChildren) {
				if(!listExplored.contains(iChild)) {
					listToBeExplored.add(iChild);
				}
			}
		}		

		return listExplored;

	}
	
	static private void updateStatusText(StringProperty statusText, String strIn) {
		if (statusText.getValue() == null) {
			statusText.setValue(strIn);
		}else {
			statusText.setValue(statusText.getValue() +"\n" + strIn);
		}
		return;
	}

	static ObservableList<Paper> createSampleData() {
		// the sample data is for debugging purpose only

		ObservableList<Paper> tempData =
				FXCollections.observableArrayList(

						new Paper(1, "Deep Defense: Training DNNs with Improved Adversarial Robustness", "Yan2018", "Yan, Ziang", "2018", "1803.00404", ""
								, "debug", "2018arXiv180300404Y", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(2, "Sparse DNNs with Improved Adversarial Robustness", "Guo2018", "Guo, Yiwen", "2018", "1810.09619", ""
								, "debug", "2018arXiv181009619G", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(3, "Towards Robust Detection of Adversarial Examples", "Pang2017", "Pang, Tianyu", "2017", "1706.00633", ""
								, "debug", "2017arXiv170600633P", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(4, "Towards the Science of Security and Privacy in Machine Learning", "Papernot2016", "Papernot, Nicolas", "2016", "1611.03814", ""
								, "debug", "2016arXiv161103814P", new ArrayList<String>(Arrays.asList("2015arXiv151104599M", "2016arXiv160507277P"))),
						new Paper(5, "Characterizing Adversarial Subspaces Using Local Intrinsic Dimensionality", "Ma2018", "Ma, Xingjun", "2018", "1801.02613", ""
								, "debug", "2018arXiv180102613M", new ArrayList<String>(Arrays.asList("2014arXiv1412.6572G", "2016arXiv160507277P", "2017arXiv170403453T"))),
						new Paper(6, "KDGAN: Knowledge Distillation with Generative Adversarial Networks", "Wang2018", "Wang, Xiaojie", "2018", "", ""
								, "debug", "", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(7, "ShapeShifter: Robust Physical Adversarial Attack on Faster R-CNN Object Detector", "Chen2019", "Chen, Shang-Tse", "2019", "1804.05810v3", ""
								, "debug", "2018arXiv180405810C", new ArrayList<String>(Arrays.asList("2017arXiv171209665B", "YYY"))),
						new Paper(8, "Synthesizing Robust Adversarial Examples", "Athalye2018", "Athalye, Anish", "2018", "1707.07397v3", ""
								, "debug", "2017arXiv170707397A", new ArrayList<String>(Arrays.asList("2015arXiv151104599M", "YYY"))),
						new Paper(9, "Adversarial Patch", "Brown2018", "Brown, Tom B", "2018", "1712.09665v2", ""
								, "debug", "2017arXiv171209665B", new ArrayList<String>(Arrays.asList("2017arXiv170707397A", "2014arXiv1412.6572G"))),
						new Paper(10, "Generating Adversarial Malware Examples for Black-Box Attacks Based on GAN", "Hu2017", "Hu, Weiwei", "2017", "1702.05983v1", ""
								, "debug", "2017arXiv170205983H", new ArrayList<String>(Arrays.asList("2016arXiv160507277P", "2014arXiv1412.6572G"))),
						new Paper(11, "Generative Adversarial Nets", "Goodfellow2014", "Goodfellow, Ian J", "2014", "1406.2661v1", ""
								, "debug", "2014arXiv1406.2661G", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(12, "Spatial Pyramid Pooling in Deep Convolutional Networks for Visual Recognition", "He", "He, Kaiming", "2014", "1406.4729v4", ""
								, "debug", "2014arXiv1406.4729H", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(13, "Faster R-CNN: Towards Real-Time Object Detection with Region Proposal Networks", "Ren", "Ren, Shaoqing", "2015", "1506.01497v3", ""
								, "debug", "2015arXiv150601497R", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(14, "APE-GAN: Adversarial Perturbation Elimination with GAN", "ShenICT2017", "{Shen ICT}, Shiwei", "2017", "1707.05474v3", ""
								, "debug", "2017arXiv170705474S", new ArrayList<String>(Arrays.asList("2016arXiv160507277P", "2014arXiv1412.6572G"))),
						new Paper(15, "MITIGATING ADVERSARIAL EFFECTS THROUGH RAN-DOMIZATION", "Xie2018", "Xie, Cihang", "2018", "1711.01991v3", ""
								, "debug", "2017arXiv171101991X", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(16, "Towards Evaluating the Robustness of Neural Networks", "Carlini2017", "Carlini, Nicholas", "2017", "1608.04644v2", "10.1109/SP.2017.49"
								, "debug", "2016arXiv160804644C", new ArrayList<String>(Arrays.asList("2016arXiv160507277P", "2014arXiv1412.6572G", "2015arXiv151104599M"))),
						new Paper(17, "Improving the Robustness of Deep Neural Networks via Stability Training", "Zheng2016", "Zheng, Stephan", "2016", "1604.04326v1", ""
								, "debug", "2016arXiv160404326Z", new ArrayList<String>(Arrays.asList("2014arXiv1412.6572G", "YYY"))),
						new Paper(18, "Universal adversarial perturbations", "Moosavi-Dezfooli2017", "Moosavi-Dezfooli, Seyed-Mohsen", "2017", "", ""
								, "debug", "", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(19, "DeepFool: a simple and accurate method to fool deep neural networks", "Moosavi-Dezfooli2015", "Moosavi-Dezfooli, Seyed-Mohsen", "2015", "1511.04599v3", ""
								, "debug", "2015arXiv151104599M", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(20, "Distillation as a Defense to Adversarial Perturbations against Deep Neural Networks", "Papernot2016a", "Papernot, Nicolas", "2016", "1511.04508v2", ""
								, "debug", "2015arXiv151104508P", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(21, "Defense against Adversarial Attacks Using High-Level Representation Guided Denoiser", "Liao2018", "Liao, Fangzhou", "2018", "1712.02976v2", ""
								, "debug", "2017arXiv171202976L", new ArrayList<String>(Arrays.asList("2014arXiv1412.6572G", "2016arXiv160507277P", "2016arXiv161103814P"))),
						new Paper(22, "Evaluating the Robustness of Neural Networks: An Extreme Value Theory Approach", "Weng2018", "Weng, Tsui-Wei", "2018", "1801.10578", ""
								, "debug", "2018arXiv180110578W", new ArrayList<String>(Arrays.asList("2014arXiv1412.6572G", "YYY"))),
						new Paper(23, "The Space of Transferable Adversarial Examples", "Tramer2017", "Tramer, Florian", "2017", "1704.03453", ""
								, "debug", "2017arXiv170403453T", new ArrayList<String>(Arrays.asList("2014arXiv1412.6572G", "2016arXiv160507277P", "2016arXiv161103814P","2017arXiv170205983H"))),
						new Paper(24, "Transferability in Machine Learning: from Phenomena to Black-Box Attacks using Adversarial Samples", "Papernot2016b", "Papernot, Nicolas", "2016", "1605.07277", ""
								, "debug", "2016arXiv160507277P", new ArrayList<String>(Arrays.asList("XXX", "YYY"))),
						new Paper(25, "Explaining and Harnessing Adversarial Examples", "Goodfellow2014a", "Goodfellow, Ian J.", "2014", "1412.6572", ""
								, "debug", "2014arXiv1412.6572G", new ArrayList<String>(Arrays.asList("XXX", "YYY")))

						);
		return tempData;
	}

}