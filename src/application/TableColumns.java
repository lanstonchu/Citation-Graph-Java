package application;

import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.scene.control.TableColumn;


public class TableColumns<S,T> {

	// property is a method in Paper which returns Properties, i.e. setTitleProperty
	public static <S, T> TableColumn<S,T> create(String columnHead, int columnWidth, Function<S, Property<T>> myFunc, boolean editableFlag) { 
		// myFunc's domain/range are S/Property_T
		// grayFlag = 1 means text color is gray; grayFlag = 0 means black

		TableColumn<S, T> myColumn = new TableColumn<>(columnHead);

		myColumn.setMinWidth(columnWidth);
		myColumn.setCellValueFactory(cellData -> myFunc.apply(cellData.getValue())); // binding column (i.e. this) and the variable
        		
		myColumn.setReorderable(false); // not allow columns to be re-ordered
        
		if (editableFlag == false) {
			myColumn.setStyle("-fx-text-fill: blue");	
		}
		
		myColumn.setOnEditCommit(event -> { // if value in the table is changed, the data will be updated
			int row = event.getTablePosition().getRow(); // get the revised entry
			S rowValue = event.getTableView().getItems().get(row);
			T newValue = event.getNewValue();
			myFunc.apply(rowValue).setValue(newValue); // call the sub-program "myFunc"
		});
		
		
		return myColumn;
	}

}
