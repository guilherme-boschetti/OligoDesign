package application;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class FastaFromWeb {
	
	// ========== FXML attributes ========== ---------- ---------- ---------- ----------
	
	@FXML
    private TextField textTargetSeqName;

    @FXML
    private TextArea textSecondaryTargetSeqsNames;

    @FXML
    private TextArea textOtherSeqsNames;
    
    @FXML
    private CheckBox chkSaveFiles;
    
    @FXML
    private Button btnSelectDirectory;
    
    // ========== attributes ========== ---------- ---------- ---------- ----------
    
    private IFastaNames iFastaNames;
    
    private String directory;
    
    // ========== screen checkbox event ========== ---------- ---------- ---------- ----------
    
    @FXML
    void onCheckChange(ActionEvent event) {
    	if (chkSaveFiles.isSelected()) {
    		btnSelectDirectory.setVisible(true);
    	} else {
    		btnSelectDirectory.setVisible(false);
    		directory = null;
    	}
    	
    	/*if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            if ("checkbox".equals(chk.getText())) {
            	System.out.println("É o checkbox selecionado.");
            }
        }*/
    }
    
    /*chkSaveFiles.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        	System.out.println("Novo valor: " + newValue);
        }
    });*/
    
    // ========== screen buttons events ========== ---------- ---------- ---------- ----------
    
    @FXML
    public void selectDirectory(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Selecione o diretório para salvar os arquivos fasta");
		Stage stage = (Stage)textTargetSeqName.getScene().getWindow();
		File file = directoryChooser.showDialog(stage);
		if (file != null) {
			directory = file.getAbsolutePath();
        } /*else {
        	System.out.println("Nenhum diretório selecionado.");
        }*/
	}

    @FXML
    void loadFastaFilesFromWeb(ActionEvent event) {

    	String targetSeqName = textTargetSeqName.getText();
    	String secondaryTargetSeqsNames = textSecondaryTargetSeqsNames.getText();
    	String otherSeqsNames = textOtherSeqsNames.getText();
    	
    	// == Validações ==
    	if (targetSeqName == null || targetSeqName.isEmpty()) {
    		String messageTarget = "Necessário informar o nome do arquivo FASTA da sequência alvo.";
    		showAlertFillField(messageTarget);
    		return;
    	}
    	if (otherSeqsNames == null || otherSeqsNames.isEmpty()) {
    		String messageOther = "Necessário informar o(s) nome(s) do(s) arquivo(s) FASTA da(s) sequência(s) similar(es) não-alvo.";
    		showAlertFillField(messageOther);
    		return;
    	}
    	if (chkSaveFiles.isSelected() && directory == null) {
    		showAlertSelectDirectory();
    		return;
    	}
    	// == ==
    	
    	String[] secondaryTargetSeqsNamesArray = null;
    	String[] otherSeqsNamesArray = null;
    	
    	if (secondaryTargetSeqsNames != null && !secondaryTargetSeqsNames.isEmpty()) {
    		secondaryTargetSeqsNamesArray = secondaryTargetSeqsNames.split(";");
    	}
    	
    	otherSeqsNamesArray = otherSeqsNames.split(";");
    	
    	//((Stage)textTargetSeqName.getScene().getWindow()).close();
    	
    	iFastaNames.loadFastaFromWeb(targetSeqName, secondaryTargetSeqsNamesArray, otherSeqsNamesArray, directory);
    }
    
    @FXML
    void cancel(ActionEvent event) {

    	Stage stage = (Stage)textTargetSeqName.getScene().getWindow();
    	stage.close();
    }
    
    // ========== Show Alerts ========== ---------- ---------- ---------- ----------
	
 	private void showAlertFillField(String message) {
 		// https://code.makery.ch/blog/javafx-dialogs-official/
     	Alert alert = new Alert(AlertType.WARNING);
     	alert.setTitle("Atenção!");
     	alert.setHeaderText(null);
     	alert.setContentText(message);
     	alert.showAndWait();
    }
 	
 	private void showAlertSelectDirectory() {
 		// https://code.makery.ch/blog/javafx-dialogs-official/
     	Alert alert = new Alert(AlertType.WARNING);
     	alert.setTitle("Atenção!");
     	alert.setHeaderText(null);
     	alert.setContentText("Você não selecionou o diretório para salvar os arquivos fasta da web.");
     	alert.showAndWait();
    }
    
    // ========== setter method ========== ---------- ---------- ---------- ----------
	
	public void setIFastaNames(IFastaNames iFastaNames) {
		this.iFastaNames = iFastaNames;
	}
	
	// ========== internal interface ========== ---------- ---------- ---------- ----------
	
	public interface IFastaNames {
		public void loadFastaFromWeb(String targetSeqName, String[] secondaryTargetSeqsNamesArray, String[] otherSeqsNamesArray, String directoryToSave);
	}
	
	// ========== ========== ---------- ---------- ---------- ----------
}
