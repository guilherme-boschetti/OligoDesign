package application;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class About {

	@FXML
    private ImageView imgOligoDesign;
	
	public void setImageOligoDesign() {
		Image image = new Image(getClass().getClassLoader().getResource("iconOligoDesign.png").toString());
		imgOligoDesign.setImage(image);
		imgOligoDesign.setPreserveRatio(true);
	}
}
