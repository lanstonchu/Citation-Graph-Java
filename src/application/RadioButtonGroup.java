package application;

import java.util.ArrayList;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * The RadioButtonGroup class contains a hbox, in which there is a label and a group of radio buttons
 */
public class RadioButtonGroup {
	private final ToggleGroup toggleGroup;
	private final VBox vBox;
	private final HBox hBox;
	
	// constructor
	RadioButtonGroup (String optionName, ArrayList<String> buttonNames) {
		toggleGroup = new ToggleGroup();
		vBox = new VBox();
		hBox = new HBox();
				
		for (int i = 0; i < buttonNames.size(); i++) {
			RadioButton rb = new RadioButton(buttonNames.get(i));
			rb.setToggleGroup(toggleGroup);
			vBox.getChildren().add(rb);
			if (i == 0) {
				rb.setSelected(true); //pre-selected the first option
			}
		}

		hBox.getChildren().addAll(new Label(optionName), vBox);

	}
	
	public void setAlignment(Pos position) {
		this.hBox.setAlignment(position);
	}
	
	public HBox getHBox() {
		return this.hBox;
	}
	
	public ToggleGroup getToggleGroup() {
		return this.toggleGroup;
	}
	
}
