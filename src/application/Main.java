package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import application.utils.AlignUtil;
import application.utils.NeedlemanWunsch;
import application.utils.NeedlemanWunschUsingFile;
import application.utils.diff_match_patch;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.web.WebView;

//import name.fraser.neil.plaintext.diff_match_patch;

//==
//https://www.youtube.com/watch?v=t4ehYIynI34
//==
//https://www.java67.com/2014/04/how-to-make-executable-jar-file-in-Java-Eclipse.html
//==
//https://stackoverflow.com/questions/26037161/runnable-jar-not-finding-libraries
//https://stackoverflow.com/questions/10231544/runnable-jar-not-working-with-referenced-libraries
//==

public class Main extends Application implements FastaFromWeb.IFastaNames {
	
	// ========== FXML attributes ========== ---------- ---------- ---------- ----------
	
	@FXML
    private MenuItem menuFastaWeb;
	@FXML
    private MenuItem menuAbout;
    @FXML
    private MenuItem menuHelp;
	@FXML
	private Button btnTarget;
	@FXML
	private Button btnTargetsSecondary;
	@FXML
	private Button btnQuery;
	@FXML
	private Button btnLoadSequencesAligned;
	@FXML
	private Button btnNextPart;
	@FXML
	private Button btnStartProcess;
	@FXML
	private Button btnClearAndRestart;
	@FXML
	private TextField txtLines;
	@FXML
	private ComboBox<OligoSize> cmbOligoSize;
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
    @FXML
    private Label lblOligosRegions;
	@FXML
	private TabPane tabPane;
	@FXML
	private WebView wvInit;
	@FXML
	private WebView wvSeqNames;
	@FXML
	private WebView wvAlignmentComparation;
	@FXML
	private WebView wvResult;
	@FXML
	private ProgressIndicator progressInit;
	
	// ========== attributes ========== ---------- ---------- ---------- ----------
	
	private Stage stage;
	private Stage stageFastWeb;
	
	private String targetSequence;
	private List<String> listSequences;
	private List<String> listSecondaryTargetSequences;
	
	private List<String> lstTargetSeqAlign;
 	private List<String> lstAnotherSeqsAlign;
 	
 	private List<String> lstTargetSeqAlignSecondary;
 	private List<String> lstAnotherSeqsAlignSecondary;
 	
 	private List<String> lstMultipleSeqsAlign;
	
	private List<String> listDescriptions;
	private List<String> listDescriptionsSecondaryTarget;
	
	private Map<Integer, List<diff_match_patch.Diff>> mapDiffs;
	
	private StringBuilder contentInit;
	
	private String messageToDisplayInLblOligosRegions;
	
	private int oligoSize = 20;
	
	private DecimalFormat decimalFormat;
	
	private String messageErrorDownloadFastaWeb;
	
	private boolean compareSecondaryTargets;
	
	private boolean doAlignment;
	private boolean useBiojava;
	private boolean showMessageBigSequence;
	private boolean storeMatrixInFile;
	
	// ========== constants ========== ---------- ---------- ---------- ----------
	
	private static String HTML_STYLE = "<style> body { font-family: 'Courier New', monospace; white-space: nowrap; } </style>";
	private static String HTML_COLOR_BLUE = "#ccccff";
	private static String HTML_COLOR_GREEN = "#ccffcc";
	private static String HTML_COLOR_RED = "#ff5050";
	//private static String HTML_COLOR_PURPLE = "#ccaaff";
	
	// ========== start ========== ---------- ---------- ---------- ----------
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Oligo Design");
			try {
				primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("iconOligoDesign.png").toString()));
			} catch (Exception exept) {
				// do nothing
			}
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// ========== initialization ========== ---------- ---------- ---------- ----------
	
	@FXML
	public void initialize() {
		listDescriptions = new LinkedList<>();
		listDescriptionsSecondaryTarget = new ArrayList<>();
		
		contentInit = new StringBuilder();
		contentInit.append(HTML_STYLE);
		
		decimalFormat = new DecimalFormat();
		
		doAlignment = true;
		
		chkDoAlignment.setSelected(true);
		useBiojava = true;
	    chkUseBiojava.setSelected(true);
		
		setMenusIcons();
		loadComboBoxOligoSize();
		enableOnlyFirstTab();
		bindWebViewsScrollBarValues();
		setTextFieldListener();
	}
	
	private void setMenusIcons() {
		// http://tutorials.jenkov.com/javafx/menubar.html
		Image imageAbout = new Image(getClass().getClassLoader().getResource("iconAbout.png").toString(), 22, 22, false, false);
		Image imageHelp = new Image(getClass().getClassLoader().getResource("iconHelp.png").toString(), 22, 22, false, false);
		ImageView imageViewAbout = new ImageView(imageAbout);
		ImageView imageViewHelp = new ImageView(imageHelp);
		imageViewAbout.setPreserveRatio(true);
		imageViewHelp.setPreserveRatio(true);
		menuAbout.setGraphic(imageViewAbout);
		menuHelp.setGraphic(imageViewHelp);
	}
	
	private void loadComboBoxOligoSize() {
		loadComboBoxOligoSizeOptions();
		setComboboxEventChangeListener();
		setComboBoxOligoSizeDefaultOption();
	}
	
	private void loadComboBoxOligoSizeOptions() {
		ObservableList<OligoSize> lstOligoSize = FXCollections.observableArrayList();
		for (int i=15; i<=30; i++) {
			lstOligoSize.add(new OligoSize(i, i + " nucleotídeos"));
		}
		cmbOligoSize.setItems(lstOligoSize);
	}
	
	private void setComboBoxOligoSizeDefaultOption() {
		int index = 5;
		cmbOligoSize.getSelectionModel().select(index);
		//cmbOligoSize.setValue(new OligoSize(oligoSize, oligoSize + " nucleotídeos"));
	}
	
	private void setComboboxEventChangeListener() {
		cmbOligoSize.valueProperty().addListener(new ChangeListener<OligoSize>() {
			@Override
			public void changed(ObservableValue<? extends OligoSize> observable, OligoSize oldValue, OligoSize newValue) {
				if (newValue != null) {
					oligoSize = newValue.getSize();
				}
			}
		});
	}
	
	private class OligoSize {
		private int size;
		private String sizeDescription;
		
		public OligoSize(int size, String sizeDescription) {
			this.size = size;
			this.sizeDescription = sizeDescription;
		}
		
		@Override
		public String toString() {
			return sizeDescription;
		}

		public int getSize() {
			return size;
		}
	}
	
	private boolean alignmentComparationScroll = false;
	private boolean seqNamesScroll = false;
	private void bindWebViewsScrollBarValues() {
		wvAlignmentComparation.setOnScroll(new EventHandler<ScrollEvent>() {
			 	@Override
				public void handle(ScrollEvent event) {
			 		if (seqNamesScroll) {
			 			seqNamesScroll = false;
			 		} else {
			 			alignmentComparationScroll = true;
			 			wvSeqNames.fireEvent(event);
			 		}
				}
		 });
		 
		 wvSeqNames.setOnScroll(new EventHandler<ScrollEvent>() {
			 	@Override
				public void handle(ScrollEvent event) {
			 		if (alignmentComparationScroll) {
			 			alignmentComparationScroll = false;
			 		} else {
			 			seqNamesScroll = true;
			 			wvAlignmentComparation.fireEvent(event);
			 		}
				}
		 });
		 
		 // ==
		 
		 /*ScrollBar vScrollBarAlignmentComparation = getVScrollBar(wvAlignmentComparation);
		 ScrollBar vScrollBarSeqNames = getVScrollBar(wvSeqNames);
		 
		 vScrollBarAlignmentComparation.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number  oldValue, Number  newValue) {
				if (seqNamesScroll) {
		 			seqNamesScroll = false;
		 		} else {
		 			alignmentComparationScroll = true;
		 			vScrollBarSeqNames.valueProperty().setValue(newValue);
		 		}
			}
		 });
		 
		 vScrollBarSeqNames.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number  oldValue, Number  newValue) {
				if (alignmentComparationScroll) {
		 			alignmentComparationScroll = false;
		 		} else {
		 			seqNamesScroll = true;
		 			vScrollBarAlignmentComparation.valueProperty().setValue(newValue);
		 		}
			}
		 });*/
	}
	
	/*private ScrollBar getVScrollBar(WebView webView) {
	    try {
	        Set<Node> scrolls = webView.lookupAll(".scroll-bar");
	        for (Node scrollNode : scrolls) {
	            if (ScrollBar.class.isInstance(scrollNode)) {
	                ScrollBar scroll = (ScrollBar) scrollNode;
	                if (scroll.getOrientation() == Orientation.VERTICAL) {
	                    return scroll;
	                }
	            }
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return null;
	}*/
	
	private void setTextFieldListener() {
		// force the field to be numeric only
		txtLines.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	txtLines.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
	}
	
	private void showContentInit() {
		wvInit.getEngine().loadContent(contentInit.toString());
	}
	
	// ========== Enable/Disable tabs ========== ---------- ---------- ---------- ----------
	
	private void enableOnlyFirstTab() {
		// Deixa apenas a primeira tab habilitada
		List<Tab> tabs = tabPane.getTabs();
		for (int i=0; i<tabs.size(); i++) {
			if (i == 0) {
				tabs.get(i).setDisable(false);
			} else {
				tabs.get(i).setDisable(true);
			}
		}
	}
	
	private void enableAllTabs() {
		// Deixa todas as tabs habilitadas
 		List<Tab> tabs = tabPane.getTabs();
 		for (int i=0; i<tabs.size(); i++) {
 			tabs.get(i).setDisable(false);
 		}
	}
	
	// ========== Show Alerts ========== ---------- ---------- ---------- ----------
	
	private void showAlertReadFileError() {
		// https://code.makery.ch/blog/javafx-dialogs-official/
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Atenção!");
    	alert.setHeaderText(null);
    	alert.setContentText("Não foi possível ler o arquivo.");
    	alert.showAndWait();
    }
	
	private void showAlertAlignError(String exceptionMessage) {
		// https://code.makery.ch/blog/javafx-dialogs-official/
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Atenção!");
    	alert.setHeaderText("Não foi possível alinhar as sequências.");
    	alert.setContentText("OutOfMemoryError: " + exceptionMessage);
    	alert.showAndWait();
    }
	
	private void showAlertCantOpenWindow(String windowName, String exceptionMessage) {
		// https://code.makery.ch/blog/javafx-dialogs-official/
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Atenção!");
    	alert.setHeaderText("Não foi possível abrir a janela " + windowName + ".");
    	alert.setContentText("Exception message: " + exceptionMessage);
    	alert.showAndWait();
    }
	
	private void showAlertProcessFinished() {
		// https://code.makery.ch/blog/javafx-dialogs-official/
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("Informação!");
    	alert.setHeaderText("Processamento Finalizado.");
    	alert.setContentText("Veja o resultado do alinhamento e da comparação na aba de \'Alinhamento e Comparação\'.\nVeja o resultado do desenho dos oligonucleotídeos na aba de \'Resultado\'.");
    	alert.showAndWait();
    }
	
	private void showConfirmationAlignUsingFile() {
		// https://code.makery.ch/blog/javafx-dialogs-official/
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Informação!");
		alert.setHeaderText("Sequências muito grandes");
		alert.setContentText("As sequências selecionadas são muito grandes para fazer o alinhamento em memória.\nDeseja fazer o alinhamento em disco?\n(OBS: O processamento em disco é extremamente demorado.)");

		ButtonType buttonTypeYes = new ButtonType("Sim", ButtonData.YES);
		ButtonType buttonTypeNo = new ButtonType("Não", ButtonData.NO);
		
		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonTypeYes){
			storeMatrixInFile = true;
			align();
		} else { // if (result.get() == buttonTypeNo){
			clearAndRestart(null);
		}
	}
	
	// ========== screen menus events ========== ---------- ---------- ---------- ----------
	
	@FXML
    void loadFastaFromWeb(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("FastaFromWeb.fxml"));
			Parent root = loader.load();
			
			FastaFromWeb fastaFromWeb = loader.getController();
			if (fastaFromWeb != null) {
				fastaFromWeb.setIFastaNames(this);
			}
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stageFastWeb = new Stage();
			stageFastWeb.setScene(scene);
			stageFastWeb.setTitle("Buscar Arquivos Fasta no NCBI Web");
	        try {
	        	stageFastWeb.getIcons().add(new Image(getClass().getClassLoader().getResource("iconOligoDesign.png").toString()));
			} catch (Exception exept) {
				// do nothing
			}
	        stageFastWeb.show();
		} catch (Exception e) {
			showAlertCantOpenWindow("FASTA NCBI WEB", e.getMessage());
		}
    }
	
	@FXML
    void openAbout(ActionEvent event) {
		try {
			//Parent root = FXMLLoader.load(getClass().getResource("About.fxml"));
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("About.fxml"));
			Parent root = loader.load();
			
			About about = loader.getController();
			if (about != null) {
				about.setImageOligoDesign();
			}
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage stage = new Stage();
	        stage.setScene(scene);
	        stage.setTitle("Sobre OligoDesign");
	        try {
	        	stage.getIcons().add(new Image(getClass().getClassLoader().getResource("iconOligoDesign.png").toString()));
			} catch (Exception exept) {
				// do nothing
			}
	        stage.show();
		} catch (Exception e) {
			showAlertCantOpenWindow("Sobre", e.getMessage());
		}
    }
	
    @FXML
    void openHelp(ActionEvent event) {
    	try {
			//Parent root = FXMLLoader.load(getClass().getResource("Help.fxml"));
    		
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("Help.fxml"));
			Parent root = loader.load();
			
			Help help = loader.getController();
			if (help != null) {
				help.setImageOligoDesign();
			}
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage stage = new Stage();
	        stage.setScene(scene);
	        stage.setTitle("OligoDesign Ajuda");
	        try {
	        	stage.getIcons().add(new Image(getClass().getClassLoader().getResource("iconOligoDesign.png").toString()));
			} catch (Exception exept) {
				// do nothing
			}
	        stage.show();
		} catch (Exception e) {
			showAlertCantOpenWindow("Ajuda", e.getMessage());
		}
    }
    
    // ========== screen checkbox event ========== ---------- ---------- ---------- ----------
    
    @FXML
    void onCheckChange(ActionEvent event) {
    	if (event.getSource() instanceof CheckBox) {
            CheckBox chk = (CheckBox) event.getSource();
            if ("Considerar sequência(s) alvo secundária(s)".equals(chk.getText())) {
            	compareSecondaryTargets = chkCompareSecondaryTargets.isSelected();
            } else if ("Usar Biojava".equals(chk.getText())) {
            	useBiojava = chkUseBiojava.isSelected();
            	if (useBiojava) {
            		radioPairwiseAlignment.setDisable(false);
            	    radioMultipleAlignment.setDisable(false);
            	} else {
            		radioPairwiseAlignment.setDisable(true);
            	    radioMultipleAlignment.setDisable(true);
            	}
            } else if ("Fazer Alinhamento".equals(chk.getText())) {
            	doAlignment = chkDoAlignment.isSelected();
            	if (doAlignment) {
            		chkUseBiojava.setDisable(false);
            		radioPairwiseAlignment.setDisable(false);
            	    radioMultipleAlignment.setDisable(false);
            	    btnTarget.setDisable(false);
            		btnTargetsSecondary.setDisable(false);
            	    btnLoadSequencesAligned.setDisable(true);
            	    txtLines.setDisable(true);
            	    txtLines.setEditable(false);
            	    btnNextPart.setDisable(true);
            	} else {
            		chkUseBiojava.setDisable(true);
            		radioPairwiseAlignment.setDisable(true);
            	    radioMultipleAlignment.setDisable(true);
            	    btnTarget.setDisable(true);
            		btnTargetsSecondary.setDisable(true);
            	    btnLoadSequencesAligned.setDisable(false);
            	    txtLines.setDisable(false);
            	    txtLines.setEditable(true);
            	    btnNextPart.setDisable(true);
            	}
            }
        }
    }
    
    /*chkCompareSecondaryTargets.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        	System.out.println("Novo valor: " + newValue);
        }
    });*/
	
	// ========== screen buttons events ========== ---------- ---------- ---------- ----------
	
    @FXML
	public void loadTargetFile(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione a sequência alvo (principal)");
        File file = fileChooser.showOpenDialog(stage);
        readTargetFile(file);
	}
	
    @FXML
	public void loadTargetsSecondaryFiles(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione a(s) sequência(s) alvo secundária(s)");
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        readTargetSecondaryFiles(files);
	}

    @FXML
	public void loadQueryFiles(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione outra(s) sequência(s) similar(es)");
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        readQueryFiles(files);
	}
    
    @FXML
	public void loadSequencesAligned(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o arquivo com todas as sequências");
        File file = fileChooser.showOpenDialog(stage);
        readAllSequencesFiles(file);
    }
    
    @FXML
	public void nextPart(ActionEvent event) {
    	lstMultipleSeqsAlign = new LinkedList<>();
        lstTargetSeqAlign = new LinkedList<>();
        lstAnotherSeqsAlign = new LinkedList<>();
		index++;
		for (List<String> lstPartsSeq : lstPartsSeqs) {
			lstMultipleSeqsAlign.add(lstPartsSeq.get(index));
		}
		lstTargetSeqAlign.add(lstMultipleSeqsAlign.get(0));
		btnStartProcess.setDisable(false);
		
		contentInit = new StringBuilder();
		wvInit.getEngine().loadContent("");
		wvSeqNames.getEngine().loadContent("");
		wvAlignmentComparation.getEngine().loadContent("");
		wvResult.getEngine().loadContent("");
    }
	
    @FXML
	public void startProcess(ActionEvent event) {
    	if (doAlignment) {
    		align();
    	} else {
    		compareAlign();
    	}
		btnTarget.setDisable(true);
		btnTargetsSecondary.setDisable(true);
		btnQuery.setDisable(true);
		btnLoadSequencesAligned.setDisable(true);
		txtLines.setDisable(true);
		txtLines.setEditable(false);
		btnNextPart.setDisable(true);
		btnStartProcess.setDisable(true);
		btnClearAndRestart.setDisable(true);
		cmbOligoSize.setDisable(true);
		radioPairwiseAlignment.setDisable(true);
	    radioMultipleAlignment.setDisable(true);
	    chkCompareSecondaryTargets.setDisable(true);
	    chkUseBiojava.setDisable(true);
	    chkDoAlignment.setDisable(true);
	}
	
    @FXML
	public void clearAndRestart(ActionEvent event) {
		targetSequence = null;
		listSequences = null;
		listSecondaryTargetSequences = null;
		lstTargetSeqAlign = null;
	 	lstAnotherSeqsAlign = null;
	 	lstTargetSeqAlignSecondary = null;
	 	lstAnotherSeqsAlignSecondary = null;
	 	lstMultipleSeqsAlign = null;
		listDescriptions = null;
		listDescriptionsSecondaryTarget = null;
		mapDiffs = null;
		contentInit = null;
		messageToDisplayInLblOligosRegions = null;
		compareSecondaryTargets = false;
		doAlignment = true;
		useBiojava = true;
		showMessageBigSequence = false;
		storeMatrixInFile = false;
		oligoSize = 20;
		
		btnTarget.setDisable(false);
		btnTargetsSecondary.setDisable(false);
		btnQuery.setDisable(true);
		btnLoadSequencesAligned.setDisable(true);
		txtLines.setDisable(true);
		txtLines.setEditable(false);
		txtLines.setText("100");
		btnNextPart.setDisable(true);
		btnStartProcess.setDisable(true);
		btnClearAndRestart.setDisable(true);
		cmbOligoSize.setDisable(false);
		radioPairwiseAlignment.setDisable(false);
	    radioMultipleAlignment.setDisable(false);
	    chkCompareSecondaryTargets.setDisable(false);
	    chkUseBiojava.setDisable(false);
	    chkDoAlignment.setDisable(false);
	    
	    chkDoAlignment.setSelected(true);
	    radioPairwiseAlignment.setSelected(true);
	    chkCompareSecondaryTargets.setSelected(false);
	    chkUseBiojava.setSelected(true);
	    lblOligosRegions.setText("");
		
		wvInit.getEngine().loadContent("");
		wvSeqNames.getEngine().loadContent("");
		wvAlignmentComparation.getEngine().loadContent("");
		wvResult.getEngine().loadContent("");
		
		// Seleciona a primeira Tab
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(0); //select by index starting with 0
		
		System.gc(); // Runs the garbage collector.
		
		initialize();
	}
	
	// ========== read file(s) methods ========== ---------- ---------- ---------- ----------
	
	private String readFile(File file) throws IOException {
		return readFile(file, false);
	}
	
	private String readFile(File file, boolean secondaryTarget) throws IOException {
    	StringBuilder stringBuilder = new StringBuilder();
    	FileReader fr = new FileReader(file);
    	BufferedReader br = new BufferedReader(fr);
		String line = "";
		if (secondaryTarget) {
			listDescriptionsSecondaryTarget.add(br.readLine()); // A primeira linha do arquivo fasta não faz parte da sequência, é uma descrição
		} else {
			listDescriptions.add(br.readLine()); // A primeira linha do arquivo fasta não faz parte da sequência, é uma descrição
		}
        line = br.readLine(); // read next line
		while (line != null) {
			stringBuilder.append(line);
			line = br.readLine(); // read next line
		}
		fr.close();
		br.close();
		return stringBuilder.toString();
    }
	
	private List<List<String>> lstPartsSeqs = new LinkedList<>();
	private int index = 0;
	
	private void readSequencesFile(File file) throws IOException {
		StringBuilder stringBuilder = null;
    	FileReader fr = new FileReader(file);
    	BufferedReader br = new BufferedReader(fr);
        String line = br.readLine(); // read first line
        lstMultipleSeqsAlign = new LinkedList<>();
        lstTargetSeqAlign = new LinkedList<>();
        lstAnotherSeqsAlign = new LinkedList<>();
        int count = 0;
        List<String> lstPartsSeq = new LinkedList<>();
		while (line != null) {
			if (line.startsWith(">")) {
				count = 0;
				listDescriptions.add(line); // A primeira linha do arquivo fasta não faz parte da sequência, é uma descrição
				if (listDescriptions.size() >= 2) {
					lstPartsSeqs.add(lstPartsSeq);
					lstMultipleSeqsAlign.add(lstPartsSeq.get(index));
					lstPartsSeq = new LinkedList<>();
				}
				stringBuilder = new StringBuilder();
			} else {
				count++;
				stringBuilder.append(line);
			}
			String linesStr = txtLines.getText();
			int lines = 100;
			try {
				lines = Integer.parseInt(linesStr);
			} catch (Exception e) {
				// do nothing
			}
			if (count > lines) {
				count = 0;
				lstPartsSeq.add(stringBuilder.toString());
				stringBuilder = new StringBuilder();
			}
			line = br.readLine(); // read next line
		}
		lstPartsSeqs.add(lstPartsSeq);
		lstMultipleSeqsAlign.add(lstPartsSeq.get(index));
		lstTargetSeqAlign.add(lstMultipleSeqsAlign.get(0));
		fr.close();
		br.close();
	}
	
	private void readTargetFile(File file) {
		if (file != null) {
			contentInit.append("Carregando sequência alvo ...");
			contentInit.append("<br/>-<br/>");
			showContentInit();
			progressInit.setVisible(true);
			
			// https://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
			// https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm
			Task<Double> task = new Task<Double>() {
				@Override
				protected Double call() throws Exception {
					// Inicia a marcar o tempo
					long startTime = System.currentTimeMillis();
					
					targetSequence = readFile(file);
					
					// Finaliza de marcar o tempo e calcula o tempo decorrido
					long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					double elapsedTimeInSeconds = elapsedTime / 1000.0;
					
					return elapsedTimeInSeconds;
				}

				@Override
				protected void succeeded() {
					btnQuery.setDisable(false);
		        	progressInit.setVisible(false);
		        	contentInit.append("Sequência alvo carregada. (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao carregar a sequência alvo.");
		        	if (getException() != null && getException().getMessage() != null) {
		        		contentInit.append("<br/>");
		        		contentInit.append(getException().getMessage());
		        	}
		        	getException().printStackTrace();
					contentInit.append("<br/>-<br/>");
					showContentInit();
					if (getException() != null && getException() instanceof IOException) {
						showAlertReadFileError();
				    }
				}
			};
			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
        }
    }
	
	private void readTargetSecondaryFiles(List<File> files) {
		if (files != null) {
			contentInit.append("Carregando a(s) sequência(s) alvo secundária(s) ...");
			contentInit.append("<br/>-<br/>");
			showContentInit();
			progressInit.setVisible(true);
			
			Task<Double> task = new Task<Double>() {
				@Override
				protected Double call() throws Exception {
					// Inicia a marcar o tempo
					long startTime = System.currentTimeMillis();
					
					listSecondaryTargetSequences = new LinkedList<>();
		            for (File file : files) {
		            	listSecondaryTargetSequences.add(readFile(file, true));
		            }

		            // Finaliza de marcar o tempo e calcula o tempo decorrido
		            long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					double elapsedTimeInSeconds = elapsedTime / 1000.0;
					
					return elapsedTimeInSeconds;
				}

				@Override
				protected void succeeded() {
		    		progressInit.setVisible(false);
		    		contentInit.append("Sequência(s) alvo secundária(s) carregada(s). (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao carregar a(s) sequência(s) alvo secundária(s).");
		        	if (getException() != null && getException().getMessage() != null) {
		        		contentInit.append("<br/>");
		        		contentInit.append(getException().getMessage());
		        	}
		        	getException().printStackTrace();
					contentInit.append("<br/>-<br/>");
					showContentInit();
					if (getException() != null && getException() instanceof IOException) {
						showAlertReadFileError();
				    }
				}
			};
			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
        }
    }
	
	private void readQueryFiles(List<File> files) {
		if (files != null) {
			contentInit.append("Carregando outra(s) sequência(s) similar(es) ...");
			contentInit.append("<br/>-<br/>");
			showContentInit();
			progressInit.setVisible(true);
			
			Task<Double> task = new Task<Double>() {
				@Override
				protected Double call() throws Exception {
					// Inicia a marcar o tempo
					long startTime = System.currentTimeMillis();
					
					listSequences = new LinkedList<>();
		            for (File file : files) {
		            	listSequences.add(readFile(file));
		            }

		            // Finaliza de marcar o tempo e calcula o tempo decorrido
		            long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					double elapsedTimeInSeconds = elapsedTime / 1000.0;
					
					return elapsedTimeInSeconds;
				}

				@Override
				protected void succeeded() {
					btnStartProcess.setDisable(false);
		    		progressInit.setVisible(false);
		    		contentInit.append("Outra(s) sequência(s) similar(es) carregada(s). (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao carregar outra(s) sequência(s) similar(es).");
		        	if (getException() != null && getException().getMessage() != null) {
		        		contentInit.append("<br/>");
		        		contentInit.append(getException().getMessage());
		        	}
		        	getException().printStackTrace();
					contentInit.append("<br/>-<br/>");
					showContentInit();
					if (getException() != null && getException() instanceof IOException) {
						showAlertReadFileError();
				    }
				}
			};
			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
        }
    }
	
	private void readAllSequencesFiles(File file) {
		if (file != null) {
			contentInit.append("Carregando sequências ...");
			contentInit.append("<br/>-<br/>");
			showContentInit();
			progressInit.setVisible(true);
			
			// https://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
			// https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm
			Task<Double> task = new Task<Double>() {
				@Override
				protected Double call() throws Exception {
					// Inicia a marcar o tempo
					long startTime = System.currentTimeMillis();
					
					readSequencesFile(file);
					
					// Finaliza de marcar o tempo e calcula o tempo decorrido
					long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					double elapsedTimeInSeconds = elapsedTime / 1000.0;
					
					return elapsedTimeInSeconds;
				}

				@Override
				protected void succeeded() {
					btnStartProcess.setDisable(false);
		        	progressInit.setVisible(false);
		        	contentInit.append("Sequências carregadas. (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao carregar as sequências.");
		        	if (getException() != null && getException().getMessage() != null) {
		        		contentInit.append("<br/>");
		        		contentInit.append(getException().getMessage());
		        	}
		        	getException().printStackTrace();
					contentInit.append("<br/>-<br/>");
					showContentInit();
					if (getException() != null && getException() instanceof IOException) {
						showAlertReadFileError();
				    }
				}
			};
			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
        }
	}
	
	// ========== Alinhar Sequencias ========== ---------- ---------- ---------- ----------
	
	private void align() {
		contentInit.append("Alinhando as sequências ...");
		contentInit.append("<br/>-<br/>");
		showContentInit();
		progressInit.setVisible(true);
		
		lstTargetSeqAlign = new LinkedList<>();
		lstAnotherSeqsAlign = new LinkedList<>();
		if (radioMultipleAlignment.isSelected()) {
			lstMultipleSeqsAlign = new LinkedList<>();
		}
		
		if (listSecondaryTargetSequences != null) {
			lstTargetSeqAlignSecondary = new LinkedList<>();
			lstAnotherSeqsAlignSecondary = new LinkedList<>();
		}
		
		Task<Double> task = new Task<Double>() {
			@Override
			protected Double call() throws Exception {
				// Inicia a marcar o tempo
				long startTime = System.currentTimeMillis();
				
				if (useBiojava) {
					if (radioPairwiseAlignment.isSelected()) {
						
						for (String seq : listSequences) {
							String[] alignResult = AlignUtil.pairwiseAlignment(targetSequence, seq);
							String targetSeq = alignResult[0];
							String anotherSeq = alignResult[1];
							lstTargetSeqAlign.add(targetSeq);
							lstAnotherSeqsAlign.add(anotherSeq);
				        }
						
						if (listSecondaryTargetSequences != null) {
							for (String seq : listSecondaryTargetSequences) {
								String[] alignResult = AlignUtil.pairwiseAlignment(targetSequence, seq);
								String targetSeq = alignResult[0];
								String anotherSeq = alignResult[1];
								lstTargetSeqAlignSecondary.add(targetSeq);
								lstAnotherSeqsAlignSecondary.add(anotherSeq);
					        }
						}
						
					} else {
						
						List<String> listToAlign = new LinkedList<>();
						listToAlign.add(targetSequence);
						if (listSecondaryTargetSequences != null) {
							listToAlign.addAll(listSecondaryTargetSequences);
						}
						listToAlign.addAll(listSequences);
						
						List<String> seqsAligned = AlignUtil.multipleAlignment(listToAlign);
						lstTargetSeqAlign.add(seqsAligned.get(0));
						for (String seq : seqsAligned) {
							lstMultipleSeqsAlign.add(seq);
						}
						
					}
				} else {
					
					for (String seq : listSequences) {
						String[] alignResult = null;
						try {
							System.gc(); // Runs the garbage collector.
							NeedlemanWunsch alignNW = new NeedlemanWunsch(targetSequence, seq);
							alignResult = alignNW.getAlignedStrands();
						} catch (OutOfMemoryError error) {
							System.gc(); // Runs the garbage collector.
							if (storeMatrixInFile) {
								NeedlemanWunschUsingFile alignNWfile = new NeedlemanWunschUsingFile(targetSequence, seq);
								alignResult = alignNWfile.getAlignedStrands();
							} else {
								showMessageBigSequence = true;
							}
						}
						if (alignResult != null) {
							String targetSeq = alignResult[0];
							String anotherSeq = alignResult[1];
							lstTargetSeqAlign.add(targetSeq);
							lstAnotherSeqsAlign.add(anotherSeq);
						}
			        }
					
					if (listSecondaryTargetSequences != null && !showMessageBigSequence) {
						for (String seq : listSecondaryTargetSequences) {
							String[] alignResult = null;
							try {
								System.gc(); // Runs the garbage collector.
								NeedlemanWunsch alignNW = new NeedlemanWunsch(targetSequence, seq);
								alignResult = alignNW.getAlignedStrands();
							} catch (OutOfMemoryError error) {
								System.gc(); // Runs the garbage collector.
								if (storeMatrixInFile) {
									NeedlemanWunschUsingFile alignNWfile = new NeedlemanWunschUsingFile(targetSequence, seq);
									alignResult = alignNWfile.getAlignedStrands();
								} else {
									showMessageBigSequence = true;
								}
							}
							if (alignResult != null) {
								String targetSeq = alignResult[0];
								String anotherSeq = alignResult[1];
								lstTargetSeqAlignSecondary.add(targetSeq);
								lstAnotherSeqsAlignSecondary.add(anotherSeq);
							}
				        }
					}
				}
				
				// Finaliza de marcar o tempo e calcula o tempo decorrido
				long endTime = System.currentTimeMillis();
				long elapsedTime = endTime - startTime;
				double elapsedTimeInSeconds = elapsedTime / 1000.0;
				
				return elapsedTimeInSeconds;
			}

			@Override
			protected void succeeded() {
				progressInit.setVisible(false);
				if (showMessageBigSequence) {
					showConfirmationAlignUsingFile();
					showMessageBigSequence = false;
				} else {
					contentInit.append("Sequências alinhadas. (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
					compareAlign();
				}
			}

			@Override
			protected void failed() {
				progressInit.setVisible(false);
	        	contentInit.append("Falha ao alinhar as sequências.");
	        	if (getException() != null && getException().getMessage() != null) {
	        		contentInit.append("<br/>");
	        		contentInit.append(getException().getMessage());
	        	}
	        	getException().printStackTrace();
				contentInit.append("<br/>-<br/>");
				showContentInit();
				btnClearAndRestart.setDisable(false);
				if (getException() != null && getException() instanceof OutOfMemoryError) {
			    	showAlertAlignError(getException().getMessage());
			    }
			}
		};
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
	}
    
    // ========== Comparar Alinhamento ========== ---------- ---------- ---------- ----------
    
    private void compareAlign() {
    	mapDiffs = new HashMap<>();
    	
    	StringBuilder comparationsAlign = new StringBuilder();
		comparationsAlign.append(HTML_STYLE);
		
    	if (radioPairwiseAlignment.isSelected() && doAlignment) {
	 		if (lstTargetSeqAlign != null && !lstTargetSeqAlign.isEmpty() 
	 				&& lstAnotherSeqsAlign != null && !lstAnotherSeqsAlign.isEmpty()) {
	 			
	 			beforeExecuteTaskCompareAlign();
	 			
	 			Task<Double> task = new Task<Double>() {
					@Override
					protected Double call() throws Exception {
						// Inicia a marcar o tempo
						long startTime = System.currentTimeMillis();
						
						// Alinhar o inicio das sequencias, deixando as sequencias sempre alinhadas
						alignInitOfSequencesPairwiseAligned();
						
						// Montar o header com a "régua" indicando o tamanho das sequencias alinhadas
						buildHeader(comparationsAlign);
			            
			            // Mostrar a sequência alvo
			            String firstTargetAligned = lstTargetSeqAlign.get(0);
			            comparationsAlign.append("<span style=\"background:" + HTML_COLOR_BLUE + ";\">").append(firstTargetAligned).append("</span>");
			            comparationsAlign.append("<br/>");
			            
			            StringBuilder comparationsAlignAnother = new StringBuilder();
			            StringBuilder comparationsAlignSecondary = null;
			           
			            diff_match_patch dmp = new diff_match_patch();
			            
			            // Comparar a sequência alvo com as sequências não alvo e mostrar as sequências não alvo
			 			for (int i=0; i<lstTargetSeqAlign.size(); i++) {
				            LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(lstAnotherSeqsAlign.get(i), lstTargetSeqAlign.get(i)); // LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(lstTargetSeqAlign.get(i), lstAnotherSeqsAlign.get(i));
				            comparationsAlignAnother.append(toHtmlDiff(diffs));
				            comparationsAlignAnother.append("<br/>");
				            // Armazena as diferenças
				            mapDiffs.put(i, diffs);
			 			}
			 			
			 			if (listSecondaryTargetSequences != null) {
			            	comparationsAlignSecondary = new StringBuilder();
				            // Comparar a sequência alvo com as sequências alvo secundárias e mostrar as sequências alvo secundárias
				            for (int i=0; i<lstAnotherSeqsAlignSecondary.size(); i++) {
				            	LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(lstAnotherSeqsAlignSecondary.get(i), lstTargetSeqAlignSecondary.get(i));
				            	comparationsAlignSecondary.append("<span style=\"background:" + HTML_COLOR_GREEN + ";\">").append(toHtmlDiff(diffs)).append("</span>");
				            	comparationsAlignSecondary.append("<br/>");
				            	if (compareSecondaryTargets) {
				            		// Armazena as diferenças
						            mapDiffs.put(lstTargetSeqAlign.size() + i, diffs);
				            	}
				 			}
			            }
			 			
			 			if (compareSecondaryTargets) {
			 				lstTargetSeqAlign.addAll(lstTargetSeqAlignSecondary);
			 				lstAnotherSeqsAlign.addAll(lstAnotherSeqsAlignSecondary);
		            	}
			 			
			 			if (comparationsAlignSecondary != null) {
			 				comparationsAlign.append(comparationsAlignSecondary.toString());
			 			}
			 			comparationsAlign.append(comparationsAlignAnother.toString());
			 			comparationsAlign.append("<br/>-<br/>");
			 			
			 			// Finaliza de marcar o tempo e calcula o tempo decorrido
			 			long endTime = System.currentTimeMillis();
						long elapsedTime = endTime - startTime;
						double elapsedTimeInSeconds = elapsedTime / 1000.0;
						
						return elapsedTimeInSeconds;
					}
	
					@Override
					protected void succeeded() {
						compareAlignSucceeded(comparationsAlign.toString(), decimalFormat.format(getValue()));
					}
	
					@Override
					protected void failed() {
						compareAlignFailed(getException());
					}
				};
				Thread t = new Thread(task);
				t.setDaemon(true);
				t.start();
	 		} else {
	 			btnClearAndRestart.setDisable(false);
	 		}
    	} else {
    		if (lstMultipleSeqsAlign != null && !lstMultipleSeqsAlign.isEmpty()) {
	 			
    			beforeExecuteTaskCompareAlign();
	 			
	 			Task<Double> task = new Task<Double>() {
					@Override
					protected Double call() throws Exception {
						// Inicia a marcar o tempo
						long startTime = System.currentTimeMillis();
						
						// == Montar o header com a "régua" indicando o tamanho das sequencias alinhadas
						buildHeader(comparationsAlign);
						
			    		// Mostrar a sequência alvo
			    		String targetAlign = lstMultipleSeqsAlign.get(0);
			    		comparationsAlign.append("<span style=\"background:" + HTML_COLOR_BLUE + ";\">").append(targetAlign).append("</span>");
			    		comparationsAlign.append("<br/>");
			    		
			    		StringBuilder comparationsAlignAnother = new StringBuilder();
			            StringBuilder comparationsAlignSecondary = null;
			            
			            Map<Integer, List<diff_match_patch.Diff>> mapDiffsAux = null;
			            
			            boolean hasSecondaryTargetFiles = listSecondaryTargetSequences != null && !listSecondaryTargetSequences.isEmpty();
			            if (hasSecondaryTargetFiles) {
			            	comparationsAlignSecondary = new StringBuilder();
			            	if (compareSecondaryTargets) {
			            		mapDiffsAux = new HashMap<>();
			            	}
			            }
			    		
			    		// Comparar a sequência alvo com as outras sequências (alvo secundárias e não alvo)
			    		diff_match_patch dmp = new diff_match_patch();
			    		for (int i=1; i<lstMultipleSeqsAlign.size(); i++) {
			    			String otherSeqAlign = lstMultipleSeqsAlign.get(i);
			    			LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(otherSeqAlign, targetAlign);
			    			if (hasSecondaryTargetFiles && i<=listSecondaryTargetSequences.size()) {
			    				// Mostrar as sequências alvo secundárias
			    				comparationsAlignSecondary.append("<span style=\"background:" + HTML_COLOR_GREEN + ";\">").append(toHtmlDiff(diffs)).append("</span>");
			    				comparationsAlignSecondary.append("<br/>");
			    				if (compareSecondaryTargets) {
				            		// Armazena as diferenças
						            mapDiffsAux.put(listSequences.size() + i - 1, diffs);
						            lstAnotherSeqsAlignSecondary.add(otherSeqAlign);
				            	}
			    			} else {
			    				// Mostrar as sequências não alvo
			    				comparationsAlignAnother.append(toHtmlDiff(diffs));
			    				comparationsAlignAnother.append("<br/>");
			    				// Armazena as diferenças
					            mapDiffs.put(lstAnotherSeqsAlign.size(), diffs);
					            lstAnotherSeqsAlign.add(otherSeqAlign);
			    			}
			 			}
			    		
			    		if (mapDiffsAux != null) {
				    		for (Entry<Integer, List<diff_match_patch.Diff>> entry : mapDiffsAux.entrySet()) {
				    			mapDiffs.put(entry.getKey(), entry.getValue());
				    		}
				    		if (compareSecondaryTargets) {
				 				lstAnotherSeqsAlign.addAll(lstAnotherSeqsAlignSecondary);
			            	}
			    		}
			    		
			    		if (comparationsAlignSecondary != null) {
			    			comparationsAlign.append(comparationsAlignSecondary.toString());
			 			}
			    		comparationsAlign.append(comparationsAlignAnother.toString());
			    		comparationsAlign.append("<br/>-<br/>");
			    		
			    		// Finaliza de marcar o tempo e calcula o tempo decorrido
			 			long endTime = System.currentTimeMillis();
						long elapsedTime = endTime - startTime;
						double elapsedTimeInSeconds = elapsedTime / 1000.0;
						
						return elapsedTimeInSeconds;
					}
			
					@Override
					protected void succeeded() {
						compareAlignSucceeded(comparationsAlign.toString(), decimalFormat.format(getValue()));
					}
			
					@Override
					protected void failed() {
						compareAlignFailed(getException());
					}
				};
				Thread t = new Thread(task);
				t.setDaemon(true);
				t.start();
    		}
    	}
 	}
    
    private void beforeExecuteTaskCompareAlign() {
    	contentInit.append("Comparando as sequências alinhadas ...");
		contentInit.append("<br/>-<br/>");
		showContentInit();
		progressInit.setVisible(true);
    }
    
    private void compareAlignSucceeded(String contentAlignmentComparation, String valueTime) {
    	progressInit.setVisible(false);
		contentInit.append("Sequências alinhadas comparadas. (" + valueTime + "s)");
		contentInit.append("<br/>-<br/>");
		showContentInit();
		wvAlignmentComparation.getEngine().loadContent(contentAlignmentComparation);
		// Mostra as descrições das sequências
		showSeqNames();
		// Desenhar os Oligonucleotídeos
		designOligos();
    }
    
    private void compareAlignFailed(Throwable exception) {
    	progressInit.setVisible(false);
    	contentInit.append("Falha ao comparar as sequências alinhadas.");
    	if (exception != null && exception.getMessage() != null) {
    		contentInit.append("<br/>");
    		contentInit.append(exception.getMessage());
    	}
    	exception.printStackTrace();
		contentInit.append("<br/>-<br/>");
		showContentInit();
		btnClearAndRestart.setDisable(false);
		
    }
    
    // ========== Alinhar o inicio das sequencias alinhadas em pares ========== ---------- ---------- ---------- ----------
    
    private int sizeSubstring(int totalSize) {
    	return ((oligoSize * 3) < (totalSize / 2)) ? (oligoSize * 3) : oligoSize;
    }
    
    private void alignInitOfSequencesPairwiseAligned() {
    	int biggestSizeSeq = 0;
		int smallestSizeSeq = lstTargetSeqAlign.get(0).length();
		int[] arrayAux = new int[lstTargetSeqAlign.size()];
		for (int i=0; i<lstTargetSeqAlign.size(); i++) {
			String targetSeqAligned = lstTargetSeqAlign.get(i);
			if (targetSeqAligned.length() > biggestSizeSeq) {
				biggestSizeSeq = targetSeqAligned.length();
			}
			if (targetSeqAligned.length() < smallestSizeSeq) {
				smallestSizeSeq = targetSeqAligned.length();
			}
			arrayAux[i] = ((int)targetSeqAligned.substring(0, sizeSubstring(targetSeqAligned.length())).chars().filter(ch -> ch == '-').count());
		}
		int[] arrayAuxSecondary = new int[lstTargetSeqAlignSecondary != null ? lstTargetSeqAlignSecondary.size() : 0];
		if (listSecondaryTargetSequences != null) {
			for (int i=0; i<lstTargetSeqAlignSecondary.size(); i++) {
				String targetSeqAligned = lstTargetSeqAlignSecondary.get(i);
				if (targetSeqAligned.length() > biggestSizeSeq) {
					biggestSizeSeq = targetSeqAligned.length();
				}
				if (targetSeqAligned.length() < smallestSizeSeq) {
					smallestSizeSeq = targetSeqAligned.length();
				}
				arrayAuxSecondary[i] = ((int)targetSeqAligned.substring(0, sizeSubstring(targetSeqAligned.length())).chars().filter(ch -> ch == '-').count());
			}
		}
		int maxSpaces = (biggestSizeSeq - smallestSizeSeq);
		for (int i=0; i<lstTargetSeqAlign.size(); i++) {
			String targetSeqAligned = lstTargetSeqAlign.get(i);
			StringBuilder aux = new StringBuilder();
			int countAux = arrayAux[i];
			int spacesToAdd = maxSpaces - countAux;
			if (spacesToAdd > 0) {
				for (int j=0; j<spacesToAdd; j++) {
					aux.append("-");
				}
				targetSeqAligned = aux.toString() + targetSeqAligned;
				lstTargetSeqAlign.remove(i);
				lstTargetSeqAlign.add(i, targetSeqAligned);
				
				String anotherSeqAligned = lstAnotherSeqsAlign.get(i);
				anotherSeqAligned = aux.toString() + anotherSeqAligned;
				lstAnotherSeqsAlign.remove(i);
				lstAnotherSeqsAlign.add(i, anotherSeqAligned);
			}
		}
		if (listSecondaryTargetSequences != null) {
			for (int i=0; i<lstTargetSeqAlignSecondary.size(); i++) {
				String targetSeqAligned = lstTargetSeqAlignSecondary.get(i);
				StringBuilder aux = new StringBuilder();
				int countAux = arrayAuxSecondary[i];
				int spacesToAdd = maxSpaces - countAux;
				if (spacesToAdd > 0) {
					for (int j=0; j<spacesToAdd; j++) {
						aux.append("-");
					}
					targetSeqAligned = aux.toString() + targetSeqAligned;
					lstTargetSeqAlignSecondary.remove(i);
					lstTargetSeqAlignSecondary.add(i, targetSeqAligned);
					
					String anotherSeqAligned = lstAnotherSeqsAlignSecondary.get(i);
					anotherSeqAligned = aux.toString() + anotherSeqAligned;
					lstAnotherSeqsAlignSecondary.remove(i);
					lstAnotherSeqsAlignSecondary.add(i, anotherSeqAligned);
				}
			}
		}
    }
    
    // ========== Monta o header com a "régua" indicando o tamanho das sequencias alinhadas ========== ---------- ---------- ---------- ----------
    
    private void buildHeader(StringBuilder comparationsAlign) {
    	int biggestSize = 1;
    	if (radioPairwiseAlignment.isSelected()) {
    		if (lstTargetSeqAlign != null && !lstTargetSeqAlign.isEmpty() 
	 				&& lstAnotherSeqsAlign != null && !lstAnotherSeqsAlign.isEmpty()) {
    			
    			for (int i=0; i<lstTargetSeqAlign.size(); i++) {
    				int targetSeqAlignLength = lstTargetSeqAlign.get(i).length();
    				int anotherSeqAlignLength = lstAnotherSeqsAlign.get(i).length();
    				if (targetSeqAlignLength > biggestSize) {
    					biggestSize = targetSeqAlignLength;
    				}
    				if (anotherSeqAlignLength > biggestSize) {
    					biggestSize = anotherSeqAlignLength;
    				}
    			}
    		}
    	} else {
    		if (lstMultipleSeqsAlign != null && !lstMultipleSeqsAlign.isEmpty()) {
    			
    			for (int i=0; i<lstMultipleSeqsAlign.size(); i++) {
    				int seqAlignLength = lstMultipleSeqsAlign.get(i).length();
    				if (seqAlignLength > biggestSize) {
    					biggestSize = seqAlignLength;
    				}
    			}
    		}
    	}
    	
    	StringBuilder header = new StringBuilder();
		int numLength = 1;
		for (int i=1; i<=biggestSize; i++) {
			if (i%10 == 0) {
				String num = String.valueOf(i);
				header.append(num);
				numLength = num.length();
			} else {
				if (numLength == 1) {
					header.append("-");
				} else {
					numLength--;
				}
			}
		}
		header.append(" - ");
		header.append("<br/>");
		for (int i=1; i<=biggestSize; i++) {
			if (i%10 == 0) {
				header.append("|");
			} else {
				header.append("-");
			}
		}
		header.append(" - ");
		comparationsAlign.append(header.toString());
        comparationsAlign.append("<br/>");
    }
    
    // ========== Aplicar html nas Diferenças ========== ---------- ---------- ---------- ----------
	
 	private String toHtmlDiff(List<diff_match_patch.Diff> diffs) {
         StringBuilder html = new StringBuilder();
         StringBuilder aux = new StringBuilder();
         for (diff_match_patch.Diff aDiff : diffs) {
             String text = aDiff.text.replace("&", "&amp;").replace("<", "&lt;")
                     .replace(">", "&gt;").replace("\n", "<br/>"); // replace("\n", "&para;<br>")

             if (aDiff.operation.equals(diff_match_patch.Operation.DELETE)) {
                 html.append("<span style=\"background:" + HTML_COLOR_RED + ";\">").append(text).append("</span>");
                 aDiff.diffStartPosition = aux.toString().length() + 1;
                 aux.append(text);
                 aDiff.diffEndPosition = aux.toString().length();
             } else if (!aDiff.operation.equals(diff_match_patch.Operation.INSERT)) {
                 html.append("<span>").append(text).append("</span>");
                 aux.append(text);
             }

             /*switch (aDiff.operation) {
                 case INSERT:
                     html.append("<ins style=\"background:" + HTML_COLOR_GREEN + ";\">").append(text).append("</ins>");
                     break;
                 case DELETE:
                     html.append("<del style=\"background:" + HTML_COLOR_RED + ";\">").append(text).append("</del>");
                     break;
                 case EQUAL:
                     html.append("<span>").append(text).append("</span>");
                     break;
             }*/
         }
         return html.toString();
    }
 	
 	// ========== Mostra as descrições das sequências ========== ---------- ---------- ---------- ----------
 	
 	private void showSeqNames() {
		StringBuilder descriptions = new StringBuilder();
		descriptions.append(HTML_STYLE);
		descriptions.append("Legenda (cores): ");
		descriptions.append("<span style=\"background:" + HTML_COLOR_BLUE + ";\">").append("azul").append("</span>");
		descriptions.append(" Sequência alvo (principal); ");
		descriptions.append("<span style=\"background:" + HTML_COLOR_GREEN + ";\">").append("verde").append("</span>");
		descriptions.append(" Sequência(s) alvo secundária(s); ");
		descriptions.append("<span style=\"background:" + HTML_COLOR_RED + ";\">").append("vermelho").append("</span>");
		descriptions.append(" Diferenças da(s) sequência(s) não alvo e alvo(s) secundária(s) em relação à sequência alvo (principal); ");
		descriptions.append("<br/>-<br/>");
		// Mostra a descrição da sequência alvo
		descriptions.append("<span style=\"background:" + HTML_COLOR_BLUE + ";\">").append(listDescriptions.get(0)).append("</span>");
		descriptions.append("<br/>");
		// Mostra a descrição das sequências alvo secundárias
		for (String description : listDescriptionsSecondaryTarget) {
			descriptions.append("<span style=\"background:" + HTML_COLOR_GREEN + ";\">").append(description).append("</span>");
			descriptions.append("<br/>");
		}
		// Mostra a descrição das sequências não alvo
		for (int i=1; i<listDescriptions.size(); i++) {
			String description = listDescriptions.get(i);
			descriptions.append(description);
			descriptions.append("<br/>");
		}
		descriptions.append("<br/>-<br/>");
		// Carrega o conteúdo
		wvSeqNames.getEngine().loadContent(descriptions.toString());
 	}
 	
 	// ========== Desenhar os Oligonucleotídeos ========== ---------- ---------- ---------- ----------
 	
  	private void designOligos() {
  		if (mapDiffs != null) {
  			contentInit.append("Desenhando os Oligonucleotídeos ...");
			contentInit.append("<br/>-<br/>");
			showContentInit();
			progressInit.setVisible(true);
			
			StringBuilder result = new StringBuilder();
  			result.append(HTML_STYLE);
			
			Task<Double> task = new Task<Double>() {
				@Override
				protected Double call() throws Exception {
					// Inicia a marcar o tempo
					long startTime = System.currentTimeMillis();
					
					//Desenha os oligonocleotídeos
					oligosDesign(result);
					
					// Finaliza de marcar o tempo e calcula o tempo decorrido
					long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					double elapsedTimeInSeconds = elapsedTime / 1000.0;
					
					return elapsedTimeInSeconds;
				}

				@Override
				protected void succeeded() {
		        	progressInit.setVisible(false);
		        	contentInit.append("Oligonucleotídeos desenhados. (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					contentInit.append("Veja o resultado do alinhamento e da comparação na aba de \'Alinhamento e Comparação\'.");
					contentInit.append("<br/>-<br/>");
					contentInit.append("Veja o resultado do desenho dos oligonucleotídeos na aba de \'Resultado\'.");
					contentInit.append("<br/>-<br/>");
					showContentInit();
					wvResult.getEngine().loadContent(result.toString());
					lblOligosRegions.setText(messageToDisplayInLblOligosRegions);
					// Finaliza o processamento
					finishProcess();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao desenhar os Oligonucleotídeos.");
		        	if (getException() != null && getException().getMessage() != null) {
		        		contentInit.append("<br/>");
		        		contentInit.append(getException().getMessage());
		        	}
		        	getException().printStackTrace();
					contentInit.append("<br/>-<br/>");
					showContentInit();
					btnClearAndRestart.setDisable(false);
				}
			};
			Thread t = new Thread(task);
			t.setDaemon(true);
			t.start();
  		} else {
  			contentInit.append("Falha ao desenhar os Oligonucleotídeos.");
			contentInit.append("<br/>-<br/>");
			showContentInit();
  		}
  	}
  	
  	private void oligosDesign(StringBuilder result) {
  		// == -- == -- Nesta parte monta uma lista com Maps de diferenças próximas que podem estar em região candidata a molde de Oligo
  		
        List<Map<Integer, List<diff_match_patch.Diff>>> listMapsDiffsPossibleOligos = new ArrayList<>();
        
		for (Entry<Integer, List<diff_match_patch.Diff>> entry : mapDiffs.entrySet()) {
			//System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
			
			int i = entry.getKey();
			List<diff_match_patch.Diff> diffs = entry.getValue();
			
	        Map<Integer, List<diff_match_patch.Diff>> mapDiffsPossibleOligos = new HashMap<>();
	        
	        for (int j=0; j<diffs.size(); j++) {
	        	diff_match_patch.Diff aDiff = diffs.get(j);
	        	if (aDiff.operation.equals(diff_match_patch.Operation.DELETE)) {
	        		// == ignorar inicio e fim
	        		if (aDiff.diffEndPosition <= oligoSize) {
	        			continue;
	        		}
	        		if (aDiff.diffStartPosition >= (lstAnotherSeqsAlign.get(i).length() - oligoSize)) {
	        			continue;
	        		}
	        		// ==
	        		List<diff_match_patch.Diff> listDiffsOligo = new ArrayList<>();
	        		listDiffsOligo.add(aDiff);
	        		for (int k=j+1; k<diffs.size(); k++) {
	        			diff_match_patch.Diff bDiff = diffs.get(k);
	        			if (bDiff.operation.equals(diff_match_patch.Operation.DELETE)) {
	        				// == ignorar inicio e fim
		            		if (bDiff.diffEndPosition <= oligoSize) {
		            			continue;
		            		}
		            		if (bDiff.diffStartPosition >= (lstAnotherSeqsAlign.get(i).length() - oligoSize)) {
		            			continue;
		            		}
		            		// ==
	            			if (bDiff.diffStartPosition - aDiff.diffEndPosition < oligoSize) {
	            				listDiffsOligo.add(bDiff);
	            			} else {
	            				break;
	            			}
	        			}
	        		}
	        		mapDiffsPossibleOligos.put(mapDiffsPossibleOligos.size()+1, listDiffsOligo);
	        	}
	        }
	        listMapsDiffsPossibleOligos.add(mapDiffsPossibleOligos);
	    }
		// == -- == -- 
		// == -- == -- Nesta parte apenas mostra quantas diferenças proximas tem, só pra ver (debug)
		/*for (Map<Integer, List<diff_match_patch.Diff>> map : listMapsDiffsPossibleOligos) {
			// == Ordena a map por valor, ordena por tamanho de lista decrescente
			// https://www.baeldung.com/java-hashmap-sort#:~:text=Since%20the%20Java%208%2C%20we,over%20the%20map's%20stream%20pipeline.
			//Map<Integer, List<diff_match_patch.Diff>> mapSorted = 
			//		map.entrySet().stream().sorted(Map.Entry.comparingByValue((l1, l2) -> Integer.valueOf(l2.size()).compareTo(l1.size())))
			//		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
	        // ==
			for (Entry<Integer, List<diff_match_patch.Diff>> entry : map.entrySet()) {
	        	//System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
				result.append(entry.getValue().size());
				result.append(" - ");
 		    }
			result.append("<br/>-<br/>");
	    }*/
		// == -- == -- 
		mapDiffsPositions = new HashMap<>();
		if (listMapsDiffsPossibleOligos.size() > 1) {
			Map<Integer, List<diff_match_patch.Diff>> mapFirst = listMapsDiffsPossibleOligos.get(0);
			for (Entry<Integer, List<diff_match_patch.Diff>> entryFirst : mapFirst.entrySet()) {
				for (int i=1; i<listMapsDiffsPossibleOligos.size(); i++) {
	 				Map<Integer, List<diff_match_patch.Diff>> map = listMapsDiffsPossibleOligos.get(i);
	 				for (Entry<Integer, List<diff_match_patch.Diff>> entry : map.entrySet()) {
	 					outer: for (int j=0; j<entryFirst.getValue().size(); j++) {
	 						diff_match_patch.Diff firstDiff = entryFirst.getValue().get(j);
	 						for (int k=0; k<entry.getValue().size(); k++) {
	 							diff_match_patch.Diff otherDiff = entry.getValue().get(k);
	 							
	 							if ((otherDiff.diffStartPosition >= firstDiff.diffStartPosition && otherDiff.diffStartPosition - firstDiff.diffStartPosition <= oligoSize) ||
	 									(otherDiff.diffStartPosition < firstDiff.diffStartPosition && otherDiff.diffEndPosition > firstDiff.diffEndPosition) ||
	 									(firstDiff.diffStartPosition < otherDiff.diffEndPosition && firstDiff.diffStartPosition > otherDiff.diffStartPosition)) {
	 		 		        		
	 								//String diffFirst = "{ (First Seq) Seq Number 0,  Diff: (" + firstDiff.diffStartPosition + ", " + firstDiff.diffEndPosition + ") }; ";
	 		 		        		//String diffOther = "{ (Other Seq) Seq Number " + i + ",  Diff: (" + otherDiff.diffStartPosition + ", " + otherDiff.diffEndPosition + ") }.";
	 		 		        		//String diffsPositions = "{ " + diffFirst + diffOther + " }";
	 		 		        		//result.append(diffsPositions);
	 		 		        		//result.append("<br/>");
	 								
	 		 		        		DiffDesignOligo diffDesignOligo = new DiffDesignOligo(i, firstDiff, otherDiff);
	 		 		        		
	 		 		        		if (!mapDiffsPositions.containsKey(firstDiff.diffStartPosition)) {
	 		 		        			Map<Integer, List<DiffDesignOligo>> submapDiffsPositions = new HashMap<>();
	 		 		        			List<DiffDesignOligo> listDiffDesignOligo = new ArrayList<>();
 		 		 		        		listDiffDesignOligo.add(diffDesignOligo);
 		 		 		        		submapDiffsPositions.put(i, listDiffDesignOligo);
 		 		 		        		mapDiffsPositions.put(firstDiff.diffStartPosition, new DesignOligo(firstDiff.diffStartPosition, submapDiffsPositions));
	 		 		        		} else {
	 		 		        			Map<Integer, List<DiffDesignOligo>> map2 = mapDiffsPositions.get(firstDiff.diffStartPosition).getMapDiffDesignOligo();
	 		 		        			if (!map2.containsKey(i)) {
	 		 		        				List<DiffDesignOligo> listDiffDesignOligo = new ArrayList<>();
	 		 		 		        		listDiffDesignOligo.add(diffDesignOligo);
	 		 		        				map2.put(i, listDiffDesignOligo);
		 		 		        		} else {
		 		 		        			List<DiffDesignOligo> listDiffDesignOligo = map2.get(i);
		 		 		        			listDiffDesignOligo.add(diffDesignOligo);
		 		 		        		}
	 		 		        		}
	 		 		        		
	 		 		        		break outer;
	 		 		        	}
	 						}
	 					}
	 	 		    }
	 		    }
 		    }
			
			// == -- == -- Nesta parte apenas mostra as diferenças e suas posições, só pra ver (debug)
			/*for (Entry<Integer, DesignOligo> entry1 : mapDiffsPositions.entrySet()) {
				for (Entry<Integer, List<DiffDesignOligo>> entry2 : entry1.getValue().getMapDiffDesignOligo().entrySet()) {
					for (DiffDesignOligo diffDesignOligo : entry2.getValue()) {
						result.append(diffDesignOligo);
						result.append("<br/>");
					}
				}
			}*/
			
			// == -- == -- Nesta parte seleciona as 3 regiões que podem servir de molde para o desenho dos oligos
			
			// == Primeiro pega da map apenas as diferenças que interessam, pois só interessam as diferenças presentes em uma mesma região que possui diferença em todas as sequencias não alvo
			Map<Integer, DesignOligo> mapDiffsPositionsValid = new HashMap<>();
			for (Entry<Integer, DesignOligo> entry : mapDiffsPositions.entrySet()) {
				int qtySequences = compareSecondaryTargets && listSecondaryTargetSequences != null ? listSequences.size() + listSecondaryTargetSequences.size() : listSequences != null ? listSequences.size() : lstMultipleSeqsAlign.size();
				if (entry.getValue().getMapDiffDesignOligo().size() == qtySequences-1) {
					mapDiffsPositionsValid.put(entry.getKey(), entry.getValue());
				}
			}
			if (!mapDiffsPositionsValid.isEmpty()) {

				// == Depois ordena por quantidade de diferenças (decrescente, ou seja, maior primeiro, mais diferenças primeiro)
				// https://www.baeldung.com/java-hashmap-sort#:~:text=Since%20the%20Java%208%2C%20we,over%20the%20map's%20stream%20pipeline.
				Map<Integer, DesignOligo> mapSorted = 
						mapDiffsPositionsValid.entrySet().stream().sorted(Map.Entry.comparingByValue((m1, m2) -> Integer.valueOf(m2.getTotalDiffsCount()).compareTo(m1.getTotalDiffsCount())))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
				
				// == Depois pega os 3 primeiros itens, se tiver 3, que são os que devem servir de molde para o desenho dos oligos
				//result.append("-<br/>Regioes<br/>-<br/>"); // -- == -- só pra ver (debug)
				int sizeMapSorted = mapSorted.size();
				int qtyOligosToDesign = sizeMapSorted >= 3 ? 3 : sizeMapSorted;
				int i = 0;
				List<DesignOligo> lstRegionsToDesignOligo = new ArrayList<>();
				int repeat = sizeMapSorted;
				while(repeat > 0) {
					qtyOligosToDesign = qtyOligosToDesign - lstRegionsToDesignOligo.size();
					for (Entry<Integer, DesignOligo> entry : mapSorted.entrySet()) {
						if (i < qtyOligosToDesign) {
							lstRegionsToDesignOligo.add(entry.getValue());
							
							// -- == -- Nesta parte apenas mostra as regiões, só pra ver (debug)
							/*for (Entry<Integer, List<DiffDesignOligo>> entry2 : entry.getValue().getMapDiffDesignOligo().entrySet()) {
								for (DiffDesignOligo diffDesignOligo : entry2.getValue()) {
									result.append(diffDesignOligo);
									result.append("<br/>");
								}
							}*/
		 		        	// -- == -- 
						} else {
							break;
						}
						i++;
					}
					//result.append("-<br/>"); // -- == -- só pra ver (debug)
					
					// == Depois vê se não tem nehum molde de oligo que pode estar "sobreposto" a outro, se tiver, tem que substituir, caso tenha regioes para substituir
					Set<DesignOligo> setOfRegionsToReplaceInDesignOligo = new HashSet<>();
					int lstRegionsToDesignOligoSize = lstRegionsToDesignOligo.size();
					if (lstRegionsToDesignOligoSize > 1) {
						for (int j=0; j<lstRegionsToDesignOligoSize-1; j++) {
							for (int k=j+1; k<lstRegionsToDesignOligoSize; k++) {
								if (Math.abs(lstRegionsToDesignOligo.get(j).getFirstDiffStartPosition() - lstRegionsToDesignOligo.get(k).getFirstDiffStartPosition()) < oligoSize) {
									setOfRegionsToReplaceInDesignOligo.add(lstRegionsToDesignOligo.get(k));
								}
							}
						}
					}
					if (!setOfRegionsToReplaceInDesignOligo.isEmpty()) {
						for (DesignOligo designOligo : lstRegionsToDesignOligo) {
							mapSorted.remove(designOligo.getFirstDiffStartPosition());
						}
						lstRegionsToDesignOligo.removeAll(setOfRegionsToReplaceInDesignOligo);
						i = 0;
					} else {
						repeat = -1; // Parar laço While
					}
					repeat--;
				}
				
				// == Depois obtém o molde dos oligonucleotídeos
				if (!lstRegionsToDesignOligo.isEmpty()) {
					// ordena a lista por firstDiffStartPosition em ordem crescente
					Collections.sort(lstRegionsToDesignOligo, new Comparator<DesignOligo>() {
			            @Override
			            public int compare(DesignOligo o1, DesignOligo o2) {
			            	return Integer.valueOf(o1.getFirstDiffStartPosition()).compareTo(o2.getFirstDiffStartPosition());
			            }
			        });
					String targetSeqAligned = lstTargetSeqAlign.get(0);
					int forwardPrimerStartPos = lstRegionsToDesignOligo.get(0).getFirstDiffStartPosition() - 1;
					int forwardPrimerEndPos = forwardPrimerStartPos + oligoSize;
					String forwardPrimerRegion = targetSeqAligned.substring(forwardPrimerStartPos, forwardPrimerStartPos + oligoSize);
					while (forwardPrimerRegion.contains("-")) {
						int countSpaces = ((int)forwardPrimerRegion.chars().filter(ch -> ch == '-').count());
						forwardPrimerRegion = forwardPrimerRegion.replaceAll("-", "");
						String complement = targetSeqAligned.substring(forwardPrimerStartPos + oligoSize, forwardPrimerStartPos + oligoSize + countSpaces);
						if (!complement.contains("-")) {
							forwardPrimerRegion += complement;
							forwardPrimerEndPos += countSpaces;
						}
					}
					String probeRegion = "";
					String reversePrimerRegion = "";
					String forwardPrimerStartPosition = "" + (forwardPrimerStartPos + 1);
					String probeStartPosition = "";
					String reversePrimerStartPosition = "";
					String forwardPrimerEndPosition = "" + forwardPrimerEndPos;
					String probeEndPosition = "";
					String reversePrimerEndPosition = "";
					int lstRegionsToDesignOligoSize = lstRegionsToDesignOligo.size();
					if (lstRegionsToDesignOligoSize >= 2) {
						int probeStartPos = lstRegionsToDesignOligo.get(1).getFirstDiffStartPosition() - 1;
						int probeEndPos = probeStartPos + oligoSize;
						probeStartPosition += probeStartPos + 1;
						probeRegion = targetSeqAligned.substring(probeStartPos, probeStartPos + oligoSize);
						while (probeRegion.contains("-")) {
							int countSpaces = ((int)probeRegion.chars().filter(ch -> ch == '-').count());
							probeRegion = probeRegion.replaceAll("-", "");
							String complement = targetSeqAligned.substring(probeStartPos + oligoSize, probeStartPos + oligoSize + countSpaces);
							if (!complement.contains("-")) {
								probeRegion += complement;
								probeEndPos += countSpaces;
							}
						}
						probeEndPosition += probeEndPos;
					}
					if (lstRegionsToDesignOligoSize >= 3) {
						int reversePrimerStartPos = lstRegionsToDesignOligo.get(2).getFirstDiffStartPosition() - 1;
						int reversePrimerEndPos = reversePrimerStartPos + oligoSize;
						reversePrimerStartPosition += reversePrimerStartPos + 1;
						reversePrimerRegion = targetSeqAligned.substring(reversePrimerStartPos, reversePrimerStartPos + oligoSize);
						while (reversePrimerRegion.contains("-")) {
							int countSpaces = ((int)reversePrimerRegion.chars().filter(ch -> ch == '-').count());
							reversePrimerRegion = reversePrimerRegion.replaceAll("-", "");
							String complement = targetSeqAligned.substring(reversePrimerStartPos + oligoSize, reversePrimerStartPos + oligoSize + countSpaces);
							if (!complement.contains("-")) {
								reversePrimerRegion += complement;
								reversePrimerEndPos += countSpaces;
							}
						}
						reversePrimerEndPosition += reversePrimerEndPos;
					}
					result.append("-<br/>");
					result.append("- ________________________ " + forwardPrimerStartPosition + " - " + forwardPrimerEndPosition);
					result.append("<br/>");
					result.append(" - Região Primer 'Forward': ");
					result.append(forwardPrimerRegion);
					result.append("<br/>-<br/>");
					result.append("- ________________________ " + probeStartPosition + " - " + probeEndPosition);
					result.append("<br/>");
					result.append(" - __________ Região Sonda: ");
					result.append(probeRegion);
					result.append("<br/>-<br/>");
					result.append("- ________________________ " + reversePrimerStartPosition + " - " + reversePrimerEndPosition);
					result.append("<br/>");
					result.append(" - _ Região Primer Reverso: ");
					result.append(reversePrimerRegion);
					result.append("<br/>-<br/>");
					
					// --
					//String forwardPrimer = "";
					//String probe = "";
					String reversePrimer = "";
					/*for (char c : forwardPrimerRegion.toCharArray()) {
						forwardPrimer += buildOligo(c);
					}
					for (char c : probeRegion.toCharArray()) {
						probe += buildOligo(c);
					}*/
					for (char c : reversePrimerRegion.toCharArray()) {
						reversePrimer += buildOligo(c);
					}
					if (!reversePrimer.isEmpty()) {
						StringBuilder sb = new StringBuilder(reversePrimer);
						reversePrimer = sb.reverse().toString();
					}
					
					result.append("<br/>-<br/>");
					result.append(" - Primer 'Forward': ");
					result.append(forwardPrimerRegion); // forwardPrimer
					result.append("<br/>-<br/>");
					result.append(" - __________ Sonda: ");
					result.append(probeRegion); // probe
					result.append("<br/>-<br/>");
					result.append(" - _ Primer Reverso: ");
					result.append(reversePrimer);
					result.append("<br/>-<br/>");
					// --
					
					StringBuilder messageOligosRegions = new StringBuilder();
					messageOligosRegions.append("Região do Primer 'Forward': " + forwardPrimerRegion + " (" + forwardPrimerStartPosition + " - " + forwardPrimerEndPosition + "); ");
					messageOligosRegions.append("Região da Sonda: " + probeRegion + " (" + probeStartPosition + " - " + probeEndPosition + "); ");
					messageOligosRegions.append("Região do Primer Reverso: " + reversePrimerRegion + " (" + reversePrimerStartPosition + " - " + reversePrimerEndPosition + ").");
					messageToDisplayInLblOligosRegions = messageOligosRegions.toString();
				} else {
					String messageOligosNotDesigned = "Não foi possível desenhar os Oligonucleotídos.";
					result.append("-<br/>");
					result.append(messageOligosNotDesigned);
					result.append("-<br/>");
					messageToDisplayInLblOligosRegions = messageOligosNotDesigned;
				}
			}
			// == -- == -- 
		}
		// == -- == -- 
  	}
  	
  	private String buildOligo(char c) {
  		String ret;
  		switch (c) {
			case 'A':
				ret = "T";
				break;
			case 'C':
				ret = "G";
				break;
			case 'T':
				ret = "A";
				break;
			case 'G':
				ret = "C";
				break;
			case 'a':
				ret = "t";
				break;
			case 'c':
				ret = "g";
				break;
			case 't':
				ret = "a";
				break;
			case 'g':
				ret = "c";
				break;
			default:
				ret = "";
				break;
		}
  		return ret;
  	}
  	
  	private Map<Integer, DesignOligo> mapDiffsPositions;
  	
  	private class DiffDesignOligo {

  		private int seqNumber;
  	    
  	    private diff_match_patch.Diff firstDiff;
  	    private diff_match_patch.Diff otherDiff;
		
		public DiffDesignOligo(int seqNumber, diff_match_patch.Diff firstDiff, diff_match_patch.Diff otherDiff) {
			this.seqNumber = seqNumber;
			this.firstDiff = firstDiff;
			this.otherDiff = otherDiff;
		}
		
		@Override
		public String toString() {
			String diffFirst = "{ (First Seq) Seq Number 0,  Diff: (" + firstDiff.diffStartPosition + ", " + firstDiff.diffEndPosition + ") }; ";
      		String diffOther = "{ (Other Seq) Seq Number " + seqNumber + ",  Diff: (" + otherDiff.diffStartPosition + ", " + otherDiff.diffEndPosition + ") }.";
      		String diffsPositions = "{ " + diffFirst + diffOther + " }";
			return diffsPositions;
		}
		
		public diff_match_patch.Diff getFirstDiff() {
			return firstDiff;
		}
	}
  	
  	private class DesignOligo {
  		
  		private int firstDiffStartPosition = -1;

  		private Map<Integer, List<DiffDesignOligo>> mapDiffDesignOligo;
		
		public DesignOligo(int firstDiffStartPosition, Map<Integer, List<DiffDesignOligo>> mapDiffDesignOligo) {
			this.firstDiffStartPosition = firstDiffStartPosition;
			this.mapDiffDesignOligo = mapDiffDesignOligo;
		}

		public Map<Integer, List<DiffDesignOligo>> getMapDiffDesignOligo() {
			return mapDiffDesignOligo;
		}
		
		public int getFirstDiffStartPosition() {
			return firstDiffStartPosition;
		}
		
		public int getTotalDiffsCount() {
			int count = 0;
			for (Entry<Integer, List<DiffDesignOligo>> entry : mapDiffDesignOligo.entrySet()) {
				//count += entry.getValue().size(); // Simplesmente poderia ser assim, mas
				// quando tem uma diferença com mais de um nucleotideo, 
				// por exemplo com 3 nucleotídeos (startPosition = 75 e endPosition = 77 (75,76 e 77)), 
				// neste caso quero que conte como 3 diferenças.
				for (DiffDesignOligo diffDesignOligo : entry.getValue()) {
					diff_match_patch.Diff firstDiff = diffDesignOligo.getFirstDiff();
					count += ((firstDiff.diffEndPosition - firstDiff.diffStartPosition) + 1);
				}
			}
			return count;
		}
	}
  	
  	// ========== Finalizar o processamento ========== ---------- ---------- ---------- ----------
 	
   	private void finishProcess() {
   		enableAllTabs();
		btnClearAndRestart.setDisable(false);
		if (!doAlignment) {
			btnNextPart.setDisable(false);
		}
		showAlertProcessFinished();
   	}
	
	// ========== Leitura de fasta por url ========== ---------- ---------- ---------- ----------
   	
   	public void loadFastaFromWeb(String targetSeqName, String[] secondaryTargetSeqsNamesArray, String[] otherSeqsNamesArray, String directoryToSave) {
   		
   		clearAndRestart(null);
   		if (stageFastWeb != null) {
   			stageFastWeb.close();
   		}
   		
   		btnTarget.setDisable(true);
   		btnTargetsSecondary.setDisable(true);
   		btnQuery.setDisable(true);
   		btnLoadSequencesAligned.setDisable(true);
   		txtLines.setDisable(true);
   		txtLines.setEditable(false);
   		btnNextPart.setDisable(true);
   		btnStartProcess.setDisable(true);
   		btnClearAndRestart.setDisable(true);
   		contentInit.append("Buscando sequências na web ...");
		contentInit.append("<br/>-<br/>");
		showContentInit();
		progressInit.setVisible(true);
		
		// https://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
		// https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm
		Task<Double> task = new Task<Double>() {
			@Override
			protected Double call() throws Exception {
				// Inicia a marcar o tempo
				long startTime = System.currentTimeMillis();
				
				readFastaFromUrl(targetSeqName, directoryToSave, true, false);
		   		//System.out.println(" -- == -- == ");
		   		if (secondaryTargetSeqsNamesArray != null) {
		   			listSecondaryTargetSequences = new LinkedList<>();
		   			for (String fastaName : secondaryTargetSeqsNamesArray) {
		   				readFastaFromUrl(fastaName, directoryToSave, false, true);
		   			}
		   			//System.out.println(" -- == -- == ");
		   		}
		   		listSequences = new LinkedList<>();
		   		for (String fastaName : otherSeqsNamesArray) {
		   			readFastaFromUrl(fastaName, directoryToSave, false, false);
				}
				
				// Finaliza de marcar o tempo e calcula o tempo decorrido
				long endTime = System.currentTimeMillis();
				long elapsedTime = endTime - startTime;
				double elapsedTimeInSeconds = elapsedTime / 1000.0;
				
				return elapsedTimeInSeconds;
			}

			@Override
			protected void succeeded() {
				btnStartProcess.setDisable(false);
	    		progressInit.setVisible(false);
	        	contentInit.append("Sequências carregadas. (Tempo total: " + decimalFormat.format(getValue()) + "s)");
				if (messageErrorDownloadFastaWeb != null) {
					contentInit.append("<br/>OBS: ");
					contentInit.append(messageErrorDownloadFastaWeb);
					messageErrorDownloadFastaWeb = null;
				}
	        	contentInit.append("<br/>-<br/>");
				showContentInit();
			}

			@Override
			protected void failed() {
				progressInit.setVisible(false);
	        	contentInit.append("Falha ao carregar as sequências da web.");
	        	if (getException() != null && getException().getMessage() != null) {
	        		contentInit.append("<br/>");
	        		contentInit.append(getException().getMessage());
	        	}
	        	getException().printStackTrace();
				contentInit.append("<br/>-<br/>");
				showContentInit();
			}
		};
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
   	}
   	
   	private void readFastaFromUrl(String fastaName, String directoryToSave, boolean target, boolean secondaryTarget) {
   		String messageError = "Falha na busca da(s) sequência(s): ";
   		List<String> linesToWrite = null;
   		try {
   			URL fastaUrl = new URL(String.format("https://www.ncbi.nlm.nih.gov/search/api/download-sequence/?db=nuccore&id=%s", fastaName));
   			InputStream is = fastaUrl.openStream();
   			InputStreamReader isr = new InputStreamReader(is);
   			BufferedReader br = new BufferedReader(isr);

   			StringBuilder inputSequence = new StringBuilder();
   			String line = "";
   	        line = br.readLine(); // read first line // A primeira linha do arquivo fasta não faz parte da sequência, é uma descrição
   	        
   	        if (!line.startsWith(">")) {
   	        	if (messageErrorDownloadFastaWeb == null) {
   	        		messageErrorDownloadFastaWeb = messageError;
   	        	}
   	        	messageErrorDownloadFastaWeb += fastaName + ", ";
   	        	return;
   	        }
   	        
   	        if (directoryToSave != null) {
				linesToWrite = new LinkedList<>();
			}
   	        
   	        //System.out.println(line);
   	        if (linesToWrite != null) {
   	        	linesToWrite.add(line);
   	        }
	   	    if (secondaryTarget) {
	 			listDescriptionsSecondaryTarget.add(line); // A primeira linha do arquivo fasta não faz parte da sequência, é uma descrição
	 		} else {
	 			listDescriptions.add(line); // A primeira linha do arquivo fasta não faz parte da sequência, é uma descrição
	 		}
   	        line = br.readLine(); // read next line
   	        while (line != null) {
   	        	inputSequence.append(line);
   	        	if (linesToWrite != null) {
   	   	        	linesToWrite.add(line);
   	   	        }
   	        	line = br.readLine(); // read next line
   	        }
   	        //System.out.println(inputSequence.toString());
   	        if (target) {
	 			targetSequence = inputSequence.toString();
	 		} else if (secondaryTarget) {
	 			listSecondaryTargetSequences.add(inputSequence.toString());
	 		} else {
	 			listSequences.add(inputSequence.toString());
	 		}
   	        
   	        is.close();
   	        isr.close();
   	        br.close();
   	        
   	        if (linesToWrite != null) {
   	        	writeFile(directoryToSave, fastaName, linesToWrite);
   	        }
   	        
		} catch (MalformedURLException e) {
			e.printStackTrace();
			if (messageErrorDownloadFastaWeb == null) {
        		messageErrorDownloadFastaWeb = messageError;
        	}
        	messageErrorDownloadFastaWeb += fastaName + ", ";
		} catch (Exception ex) {
			ex.printStackTrace();
			if (messageErrorDownloadFastaWeb == null) {
        		messageErrorDownloadFastaWeb = messageError;
        	}
        	messageErrorDownloadFastaWeb += fastaName + ", ";
		}
   	}
   	
   	private void writeFile(String directory, String fileName, List<String> lines) {
   		FileWriter fw = null;
   		BufferedWriter bw = null;
   		try {
	   		File file = new File(directory + "/" + fileName + ".fasta");
	   		fw = new FileWriter(file);
	   		bw = new BufferedWriter(fw);
	   		for (String line : lines) {
	   			bw.write(line);
	   			bw.newLine();
	   		}
	   		bw.close();
	   		fw.close();
   		} catch (IOException ioe) {
   			ioe.printStackTrace();
   		} /*finally {
   			try {
	   			if (bw != null) {
	   				bw.close();
	   			}
	   			if (fw != null) {
	   				fw.close();
	   			}
   			} catch (IOException ioe) {
   	   			// do nothing
   	   		}
   		}*/
   	}
   	
   	// ========== ========== ---------- ---------- ---------- ----------
}
