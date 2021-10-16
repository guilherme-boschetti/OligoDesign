package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

public class AlignmentConfigs {
	
	// ========== FXML attributes ========== ---------- ---------- ---------- ----------
	
	@FXML
    private CheckBox chkCompareSecondaryTargets;
	@FXML
    private CheckBox chkDoAlignment;
	@FXML
    private CheckBox chkUseBiojava;
	@FXML
    private ToggleGroup groupAlignment;
	@FXML
    private RadioButton radioPairwiseAlignment;
    @FXML
    private RadioButton radioMultipleAlignment;
    
    // ========== attributes ========== ---------- ---------- ---------- ----------
    
    private boolean compareSecondaryTargets;
	private boolean doAlignment;
	private boolean useBiojava;
	private int alignmentType;
    
    private IParametersConfigurations iParametersConfigurations;
    
// ========== initialization ========== ---------- ---------- ---------- ----------
    
    @FXML
	public void initialize() {
    	loadAttributes();
    	setScreenValues();
    	updateFieldsDisable();
    }
    
    private void loadAttributes() {
    	File config = new File("C:/OligoDesign/AlignmentConfigs.txt");
		if (config.exists()) {
			try {
				List<String> lines = new ArrayList<>();
		    	FileReader fr = new FileReader(config);
		    	BufferedReader br = new BufferedReader(fr);
				String line = br.readLine();
				while (line != null) {
					lines.add(line);
					line = br.readLine();
				}
				fr.close();
				br.close();

				Object[] params = lines.toArray();
				
				doAlignment = Integer.valueOf(((String)params[0]).split(" = ")[1]) == 1;
				useBiojava = Integer.valueOf(((String)params[1]).split(" = ")[1]) == 1;
				alignmentType = Integer.valueOf(((String)params[2]).split(" = ")[1]);
				compareSecondaryTargets = Integer.valueOf(((String)params[3]).split(" = ")[1]) == 1;
				
			} catch (Exception e) {
				doAlignment = true;
				useBiojava = true;
				alignmentType = 0;
				compareSecondaryTargets = false;
			}
		}
    }
    
    private void setScreenValues() {
    	chkCompareSecondaryTargets.setSelected(compareSecondaryTargets);
    	chkDoAlignment.setSelected(doAlignment);
    	chkUseBiojava.setSelected(useBiojava);
    	if (alignmentType == 0)
    		radioPairwiseAlignment.setSelected(true);
    	else
    		radioMultipleAlignment.setSelected(true);
    }
    
    private void updateFieldsDisable() {
    	updateFieldsDisableDoAlignment();
    	updateFieldsDisableUseBiojava();
    }
    
    private void updateFieldsDisableUseBiojava() {
    	if (useBiojava) {
    		radioPairwiseAlignment.setDisable(false);
    	    radioMultipleAlignment.setDisable(false);
    	} else {
    		radioPairwiseAlignment.setDisable(true);
    	    radioMultipleAlignment.setDisable(true);
    	}
    }
    
    private void updateFieldsDisableDoAlignment() {
    	if (doAlignment) {
    		chkUseBiojava.setDisable(false);
    		radioPairwiseAlignment.setDisable(false);
    	    radioMultipleAlignment.setDisable(false);
    	} else {
    		chkUseBiojava.setDisable(true);
    		radioPairwiseAlignment.setDisable(true);
    	    radioMultipleAlignment.setDisable(true);
    	}
    }
    
    // ========== screen checkbox event ========== ---------- ---------- ---------- ----------
    
    @FXML
    void onCheckChange(ActionEvent event) {
    	if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            if ("Considerar sequência(s) alvo secundária(s) para o desenho".equals(chk.getText())) {
            	compareSecondaryTargets = chkCompareSecondaryTargets.isSelected();
            } else if ("Usar Biojava".equals(chk.getText())) {
            	useBiojava = chkUseBiojava.isSelected();
            	updateFieldsDisableUseBiojava();
            } else if ("Fazer Alinhamento".equals(chk.getText())) {
            	doAlignment = chkDoAlignment.isSelected();
            	updateFieldsDisableDoAlignment();
            }
        }
    }
    
 // ========== screen buttons events ========== ---------- ---------- ---------- ----------
	
 	@FXML
     void save(ActionEvent event) {
 		
 		alignmentType = radioMultipleAlignment.isSelected() ? 1 : 0;
 		
 		try {
 			String line1 = "Fazer Alinhamento (0 - Não; 1 - Sim) = " + (doAlignment ? 1 : 0);
			String line2 = "Usar BioJava (0 - Não; 1 - Sim) = " + (useBiojava ? 1 : 0);
			String line3 = "Tipo de Alinhamento (0 - Alinhamento em pares; 1 - Alinhamento Múltiplo) = " + alignmentType;
			String line4 = "Considerar sequência(s) alvo secundária(s) para o desenho (0 - Não; 1 - Sim) = " + (compareSecondaryTargets ? 1 : 0);
			List<String> lines = new ArrayList<>();
			lines.add(line1);
			lines.add(line2);
			lines.add(line3);
			lines.add(line4);
 			
 			File config = new File("C:/OligoDesign/AlignmentConfigs.txt");
 			config.delete();
 			
 			Main.writeFile("C:/OligoDesign", "AlignmentConfigs", "txt", lines);
 		} catch(Exception e) {
 			// do nothing
 		}

 		iParametersConfigurations.loadConfigurations();
     }
     
     @FXML
     void cancel(ActionEvent event) {

     	Stage stage = (Stage)chkDoAlignment.getScene().getWindow();
     	stage.close();
     }
    
    // ========== setter method ========== ---------- ---------- ---------- ----------
	
	public void setIParametersConfigurations(IParametersConfigurations iParametersConfigurations) {
		this.iParametersConfigurations = iParametersConfigurations;
	}
	
	// ========== ========== ---------- ---------- ---------- ----------
}
