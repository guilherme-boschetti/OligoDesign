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
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
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

public class Main extends Application implements FastaFromWeb.IFastaNames, IParametersConfigurations {
	
	// ========== FXML attributes ========== ---------- ---------- ---------- ----------
	
	@FXML
    private MenuItem menuFastaWeb;
	@FXML
    private MenuItem menuDesignParams;
	@FXML
    private MenuItem menuAlignmentConfigs;
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
	private Label lblConfigs;
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
	@FXML
	private VBox vboxLines;
	@FXML
	private VBox vboxNext;
	
	// ========== attributes ========== ---------- ---------- ---------- ----------
	
	private Stage stage;
	private Stage stageFastWeb;
	private Stage stageDesignParams;
	private Stage stageAlignmentConfigs;
	
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
	private int ampliconSize = 150;
	private int maxConsecutiveNucleotides = 5;
	private int minTM = 55;
	private int maxTM = 62;
	private int minPercentCG = 30;
	private int maxPercentCG = 70;
	
	String configs;
	
	private DecimalFormat decimalFormat;
	
	private String messageErrorDownloadFastaWeb;
	
	private boolean compareSecondaryTargets;
	
	private boolean doAlignment;
	private boolean useBiojava;
	
	private int alignmentType;
	
	private boolean showMessageBigSequence;
	private boolean storeMatrixInFile;
	
	// ========== constants ========== ---------- ---------- ---------- ----------
	
	private static int ALIGNMENT_TYPE_PAIR = 0;
	private static int ALIGNMENT_TYPE_MULTIPLE = 1;
	
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
	    
		loadConfigurations();
		
		setMenusIcons();
		enableOnlyFirstTab();
		bindWebViewsScrollBarValues();
		setTextFieldListener();
	}
	
	public void loadConfigurations() {
		configs = "Configura��es do OligoDesign:\n\n";
		
	    loadDesignParams();
	    loadAlignmentConfigs();
	    
	    lblConfigs.setText(configs);
	}
	
	private void loadDesignParams() {
		if (stageDesignParams != null) {
			stageDesignParams.close();
   		}
		File dir = new File("C:/OligoDesign");
		if (!dir.exists()) {
			dir.mkdir();
		}
		File config = new File("C:/OligoDesign/DesignParams.txt");
		if (!config.exists()) {

			String line1 = "Comprimento dos Oligonucleot�deos = 20";
			String line2 = "Tamanho m�ximo do Amplicon = 150";
			String line3 = "N�mero m�ximo de nucleot�deos consecutivos = 5";
			String line4 = "Temperatura de melting m�nima = 55";
			String line5 = "Temperatura de melting m�xima = 62";
			String line6 = "Porcentagem de CG m�nima = 30";
			String line7 = "Porcentagem de CG m�xima = 70";
			// Propor��o entre GC e AT mais ou menos 50%, sonda pode ter mais GC que primer, pois tem que ter temperatura maior.
			// Temperatura de melting da sonda tem que ficar aproximadamente 10 graus a mais que dos primers.
			List<String> lines = new ArrayList<>();
			lines.add(line1);
			lines.add(line2);
			lines.add(line3);
			lines.add(line4);
			lines.add(line5);
			lines.add(line6);
			lines.add(line7);
			
			writeFile("C:/OligoDesign", "DesignParams", "txt", lines);
			
			config = new File("C:/OligoDesign/DesignParams.txt");
		}
		if (!config.exists()) {
			oligoSize = 20;
			ampliconSize = 150;
			maxConsecutiveNucleotides = 5;
			minTM = 55;
			maxTM = 62;
			minPercentCG = 30;
			maxPercentCG = 70;
		} else {
			try {
				List<String> lines = new ArrayList<>();
		    	FileReader fr = new FileReader(config);
		    	BufferedReader br = new BufferedReader(fr);
		    	
		    	StringBuilder sb = new StringBuilder();
		    	
				String line = br.readLine();
				while (line != null) {
					lines.add(line);
					sb.append(line + "\n");
					line = br.readLine();
				}
				fr.close();
				br.close();
				
				configs += sb.toString();

				Object[] params = lines.toArray();
				
				oligoSize = Integer.valueOf(((String)params[0]).split(" = ")[1]);
				ampliconSize = Integer.valueOf(((String)params[1]).split(" = ")[1]);
				maxConsecutiveNucleotides = Integer.valueOf(((String)params[2]).split(" = ")[1]);
				minTM = Integer.valueOf(((String)params[3]).split(" = ")[1]);
				maxTM = Integer.valueOf(((String)params[4]).split(" = ")[1]);
				minPercentCG = Integer.valueOf(((String)params[5]).split(" = ")[1]);
				maxPercentCG = Integer.valueOf(((String)params[6]).split(" = ")[1]);
				
			} catch (Exception e) {
				oligoSize = 20;
				ampliconSize = 150;
				maxConsecutiveNucleotides = 5;
				minTM = 55;
				maxTM = 62;
				minPercentCG = 30;
				maxPercentCG = 70;
			}
		}
	}
	
	private void loadAlignmentConfigs() {
		if (stageAlignmentConfigs != null) {
			stageAlignmentConfigs.close();
   		}
		File dir = new File("C:/OligoDesign");
		if (!dir.exists()) {
			dir.mkdir();
		}
		File config = new File("C:/OligoDesign/AlignmentConfigs.txt");
		if (!config.exists()) {
			
			String line1 = "Fazer Alinhamento (0 - N�o; 1 - Sim) = 1";
			String line2 = "Usar BioJava (0 - N�o; 1 - Sim) = 1";
			String line3 = "Tipo de Alinhamento (0 - Alinhamento em pares; 1 - Alinhamento M�ltiplo) = 0";
			String line4 = "Considerar sequ�ncia(s) alvo secund�ria(s) para o desenho (0 - N�o; 1 - Sim) = 0";
			List<String> lines = new ArrayList<>();
			lines.add(line1);
			lines.add(line2);
			lines.add(line3);
			lines.add(line4);
			
			writeFile("C:/OligoDesign", "AlignmentConfigs", "txt", lines);
			
			config = new File("C:/OligoDesign/AlignmentConfigs.txt");
		}
		if (!config.exists()) {
			doAlignment = true;
			useBiojava = true;
			alignmentType = 0;
			compareSecondaryTargets = false;
		} else {
			try {
				List<String> lines = new ArrayList<>();
		    	FileReader fr = new FileReader(config);
		    	BufferedReader br = new BufferedReader(fr);
		    	
		    	StringBuilder sb = new StringBuilder();
		    	
				String line = br.readLine();
				while (line != null) {
					lines.add(line);
					sb.append(line + "\n");
					line = br.readLine();
				}
				fr.close();
				br.close();
				
				configs += sb.toString();

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
		if (doAlignment) {
			vboxLines.setVisible(false);
			vboxNext.setVisible(false);
    	    btnTarget.setDisable(false);
    		btnTargetsSecondary.setDisable(false);
    	    btnLoadSequencesAligned.setDisable(true);
    	    txtLines.setDisable(true);
    	    txtLines.setEditable(false);
    	    btnNextPart.setDisable(true);
    	} else {
    		vboxLines.setVisible(true);
			vboxNext.setVisible(true);
    	    btnTarget.setDisable(true);
    		btnTargetsSecondary.setDisable(true);
    	    btnLoadSequencesAligned.setDisable(false);
    	    txtLines.setDisable(false);
    	    txtLines.setEditable(true);
    	    btnNextPart.setDisable(true);
    	}
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
    	alert.setTitle("Aten��o!");
    	alert.setHeaderText(null);
    	alert.setContentText("N�o foi poss�vel ler o arquivo.");
    	alert.showAndWait();
    }
	
	private void showAlertAlignError(String exceptionMessage) {
		// https://code.makery.ch/blog/javafx-dialogs-official/
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Aten��o!");
    	alert.setHeaderText("N�o foi poss�vel alinhar as sequ�ncias.");
    	alert.setContentText("OutOfMemoryError: " + exceptionMessage);
    	alert.showAndWait();
    }
	
	private void showAlertCantOpenWindow(String windowName, String exceptionMessage) {
		// https://code.makery.ch/blog/javafx-dialogs-official/
    	Alert alert = new Alert(AlertType.WARNING);
    	alert.setTitle("Aten��o!");
    	alert.setHeaderText("N�o foi poss�vel abrir a janela " + windowName + ".");
    	alert.setContentText("Exception message: " + exceptionMessage);
    	alert.showAndWait();
    }
	
	private void showAlertProcessFinished() {
		// https://code.makery.ch/blog/javafx-dialogs-official/
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("Informa��o!");
    	alert.setHeaderText("Processamento Finalizado.");
    	alert.setContentText("Veja o resultado do alinhamento e da compara��o na aba de \'Alinhamento e Compara��o\'.\nVeja o resultado do desenho dos oligonucleot�deos na aba de \'Resultado\'.");
    	alert.showAndWait();
    }
	
	private void showConfirmationAlignUsingFile() {
		// https://code.makery.ch/blog/javafx-dialogs-official/
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Informa��o!");
		alert.setHeaderText("Sequ�ncias muito grandes");
		alert.setContentText("As sequ�ncias selecionadas s�o muito grandes para fazer o alinhamento em mem�ria.\nDeseja fazer o alinhamento em disco?\n(OBS: O processamento em disco � extremamente demorado.)");

		ButtonType buttonTypeYes = new ButtonType("Sim", ButtonData.YES);
		ButtonType buttonTypeNo = new ButtonType("N�o", ButtonData.NO);
		
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
    void openDesignParams(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("DesignParams.fxml"));
			Parent root = loader.load();
			
			DesignParams designParams = loader.getController();
			if (designParams != null) {
				designParams.setIParametersConfigurations(this);
			}
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stageDesignParams = new Stage();
			stageDesignParams.setScene(scene);
			stageDesignParams.setTitle("Par�metros de desenho OligoDesign");
	        try {
	        	stageDesignParams.getIcons().add(new Image(getClass().getClassLoader().getResource("iconOligoDesign.png").toString()));
			} catch (Exception exept) {
				// do nothing
			}
	        stageDesignParams.show();
		} catch (Exception e) {
			showAlertCantOpenWindow("Par�metros de desenho", e.getMessage());
		}
    }
	
	@FXML
    void openAlignmentConfigs(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AlignmentConfigs.fxml"));
			Parent root = loader.load();
			
			AlignmentConfigs alignmentConfigs = loader.getController();
			if (alignmentConfigs != null) {
				alignmentConfigs.setIParametersConfigurations(this);
			}
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stageAlignmentConfigs = new Stage();
			stageAlignmentConfigs.setScene(scene);
			stageAlignmentConfigs.setTitle("Configura��es de alinhamento OligoDesign");
	        try {
	        	stageAlignmentConfigs.getIcons().add(new Image(getClass().getClassLoader().getResource("iconOligoDesign.png").toString()));
			} catch (Exception exept) {
				// do nothing
			}
	        stageAlignmentConfigs.show();
		} catch (Exception e) {
			showAlertCantOpenWindow("Configura��es de alinhamento", e.getMessage());
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
	
	// ========== screen buttons events ========== ---------- ---------- ---------- ----------
	
    @FXML
	public void loadTargetFile(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione a sequ�ncia alvo (principal)");
        File file = fileChooser.showOpenDialog(stage);
        readTargetFile(file);
	}
	
    @FXML
	public void loadTargetsSecondaryFiles(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione a(s) sequ�ncia(s) alvo secund�ria(s)");
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        readTargetSecondaryFiles(files);
	}

    @FXML
	public void loadQueryFiles(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione outra(s) sequ�ncia(s) similar(es)");
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        readQueryFiles(files);
	}
    
    @FXML
	public void loadSequencesAligned(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o arquivo com todas as sequ�ncias");
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
			if (index < lstPartsSeq.size()) {
				lstMultipleSeqsAlign.add(lstPartsSeq.get(index));
			}
		}
		if (!lstMultipleSeqsAlign.isEmpty()) {
			lstTargetSeqAlign.add(lstMultipleSeqsAlign.get(0));
		}
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
		index = 0;
		
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
			listDescriptionsSecondaryTarget.add(br.readLine()); // A primeira linha do arquivo fasta n�o faz parte da sequ�ncia, � uma descri��o
		} else {
			listDescriptions.add(br.readLine()); // A primeira linha do arquivo fasta n�o faz parte da sequ�ncia, � uma descri��o
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
				listDescriptions.add(line); // A primeira linha do arquivo fasta n�o faz parte da sequ�ncia, � uma descri��o
				if (listDescriptions.size() >= 2) {
					lstPartsSeqs.add(lstPartsSeq);
					if (index < lstPartsSeq.size()) {
						lstMultipleSeqsAlign.add(lstPartsSeq.get(index));
					}
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
			if (count >= lines) {
				count = 0;
				lstPartsSeq.add(stringBuilder.toString());
				stringBuilder = new StringBuilder();
			}
			line = br.readLine(); // read next line
		}
		lstPartsSeqs.add(lstPartsSeq);
		if (index < lstPartsSeq.size()) {
			lstMultipleSeqsAlign.add(lstPartsSeq.get(index));
		}
		if (!lstMultipleSeqsAlign.isEmpty()) {
			lstTargetSeqAlign.add(lstMultipleSeqsAlign.get(0));
		}
		fr.close();
		br.close();
	}
	
	private void readTargetFile(File file) {
		if (file != null) {
			contentInit.append("Carregando sequ�ncia alvo ...");
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
		        	contentInit.append("Sequ�ncia alvo carregada. (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao carregar a sequ�ncia alvo.");
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
			contentInit.append("Carregando a(s) sequ�ncia(s) alvo secund�ria(s) ...");
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
		    		contentInit.append("Sequ�ncia(s) alvo secund�ria(s) carregada(s). (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao carregar a(s) sequ�ncia(s) alvo secund�ria(s).");
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
			contentInit.append("Carregando outra(s) sequ�ncia(s) similar(es) ...");
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
		    		contentInit.append("Outra(s) sequ�ncia(s) similar(es) carregada(s). (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao carregar outra(s) sequ�ncia(s) similar(es).");
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
			contentInit.append("Carregando sequ�ncias ...");
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
		        	contentInit.append("Sequ�ncias carregadas. (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
				}

				@Override
				protected void failed() {
					progressInit.setVisible(false);
		        	contentInit.append("Falha ao carregar as sequ�ncias.");
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
		contentInit.append("Alinhando as sequ�ncias ...");
		contentInit.append("<br/>-<br/>");
		showContentInit();
		progressInit.setVisible(true);
		
		lstTargetSeqAlign = new LinkedList<>();
		lstAnotherSeqsAlign = new LinkedList<>();
		if (alignmentType == ALIGNMENT_TYPE_MULTIPLE) {
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
					if (alignmentType == ALIGNMENT_TYPE_PAIR) {
						
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
					contentInit.append("Sequ�ncias alinhadas. (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					showContentInit();
					compareAlign();
				}
			}

			@Override
			protected void failed() {
				progressInit.setVisible(false);
	        	contentInit.append("Falha ao alinhar as sequ�ncias.");
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
		
    	if (alignmentType == ALIGNMENT_TYPE_PAIR && doAlignment) {
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
						
						// Montar o header com a "r�gua" indicando o tamanho das sequencias alinhadas
						buildHeader(comparationsAlign);
			            
			            // Mostrar a sequ�ncia alvo
			            String firstTargetAligned = lstTargetSeqAlign.get(0);
			            comparationsAlign.append("<span style=\"background:" + HTML_COLOR_BLUE + ";\">").append(firstTargetAligned).append("</span>");
			            comparationsAlign.append("<br/>");
			            
			            StringBuilder comparationsAlignAnother = new StringBuilder();
			            StringBuilder comparationsAlignSecondary = null;
			           
			            diff_match_patch dmp = new diff_match_patch();
			            
			            // Comparar a sequ�ncia alvo com as sequ�ncias n�o alvo e mostrar as sequ�ncias n�o alvo
			 			for (int i=0; i<lstTargetSeqAlign.size(); i++) {
				            LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(lstAnotherSeqsAlign.get(i), lstTargetSeqAlign.get(i)); // LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(lstTargetSeqAlign.get(i), lstAnotherSeqsAlign.get(i));
				            comparationsAlignAnother.append(toHtmlDiff(diffs));
				            comparationsAlignAnother.append("<br/>");
				            // Armazena as diferen�as
				            mapDiffs.put(i, diffs);
			 			}
			 			
			 			if (listSecondaryTargetSequences != null) {
			            	comparationsAlignSecondary = new StringBuilder();
				            // Comparar a sequ�ncia alvo com as sequ�ncias alvo secund�rias e mostrar as sequ�ncias alvo secund�rias
				            for (int i=0; i<lstAnotherSeqsAlignSecondary.size(); i++) {
				            	LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(lstAnotherSeqsAlignSecondary.get(i), lstTargetSeqAlignSecondary.get(i));
				            	comparationsAlignSecondary.append("<span style=\"background:" + HTML_COLOR_GREEN + ";\">").append(toHtmlDiff(diffs)).append("</span>");
				            	comparationsAlignSecondary.append("<br/>");
				            	if (compareSecondaryTargets) {
				            		// Armazena as diferen�as
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
						
						// == Montar o header com a "r�gua" indicando o tamanho das sequencias alinhadas
						buildHeader(comparationsAlign);
						
			    		// Mostrar a sequ�ncia alvo
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
			    		
			    		// Comparar a sequ�ncia alvo com as outras sequ�ncias (alvo secund�rias e n�o alvo)
			    		diff_match_patch dmp = new diff_match_patch();
			    		for (int i=1; i<lstMultipleSeqsAlign.size(); i++) {
			    			String otherSeqAlign = lstMultipleSeqsAlign.get(i);
			    			LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(otherSeqAlign, targetAlign);
			    			if (hasSecondaryTargetFiles && i<=listSecondaryTargetSequences.size()) {
			    				// Mostrar as sequ�ncias alvo secund�rias
			    				comparationsAlignSecondary.append("<span style=\"background:" + HTML_COLOR_GREEN + ";\">").append(toHtmlDiff(diffs)).append("</span>");
			    				comparationsAlignSecondary.append("<br/>");
			    				if (compareSecondaryTargets) {
				            		// Armazena as diferen�as
						            mapDiffsAux.put(listSequences.size() + i - 1, diffs);
						            lstAnotherSeqsAlignSecondary.add(otherSeqAlign);
				            	}
			    			} else {
			    				// Mostrar as sequ�ncias n�o alvo
			    				comparationsAlignAnother.append(toHtmlDiff(diffs));
			    				comparationsAlignAnother.append("<br/>");
			    				// Armazena as diferen�as
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
    		} else {
        		contentInit.append("Fim");
        		contentInit.append("<br/>-<br/>");
        		showContentInit();
        	}
    	}
 	}
    
    private void beforeExecuteTaskCompareAlign() {
    	contentInit.append("Comparando as sequ�ncias alinhadas ...");
		contentInit.append("<br/>-<br/>");
		showContentInit();
		progressInit.setVisible(true);
    }
    
    private void compareAlignSucceeded(String contentAlignmentComparation, String valueTime) {
    	progressInit.setVisible(false);
		contentInit.append("Sequ�ncias alinhadas comparadas. (" + valueTime + "s)");
		contentInit.append("<br/>-<br/>");
		showContentInit();
		wvAlignmentComparation.getEngine().loadContent(contentAlignmentComparation);
		// Mostra as descri��es das sequ�ncias
		showSeqNames();
		// Desenhar os Oligonucleot�deos
		designOligos();
    }
    
    private void compareAlignFailed(Throwable exception) {
    	progressInit.setVisible(false);
    	contentInit.append("Falha ao comparar as sequ�ncias alinhadas.");
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
		//--
		int[] spacesTargetSeqAlign = new int[lstTargetSeqAlign.size()];
		for (int i=0; i<lstTargetSeqAlign.size(); i++) {
			String targetSeqAligned = lstTargetSeqAlign.get(i);
			int countSpaces = 0;
			for (int j=0; j<targetSeqAligned.length(); j++) {
	  			char c = targetSeqAligned.charAt(j);
	  			if (c == '-') {
	  				countSpaces++;
	  			} else {
	  				break;
	  			}
	  		}
			spacesTargetSeqAlign[i] = countSpaces;
		}
		List<Integer> spacesSecondaryTargetSequences = new ArrayList<>();
		if (listSecondaryTargetSequences != null) {
			for (int i=0; i<lstTargetSeqAlignSecondary.size(); i++) {
				String targetSeqAligned = lstTargetSeqAlignSecondary.get(i);
				int countSpaces = 0;
				for (int j=0; j<targetSeqAligned.length(); j++) {
		  			char c = targetSeqAligned.charAt(j);
		  			if (c == '-') {
		  				countSpaces++;
		  			} else {
		  				break;
		  			}
		  		}
				spacesSecondaryTargetSequences.add(countSpaces);
			}
		}
		//--
		int smallest = spacesTargetSeqAlign[0];
		for (int space : spacesTargetSeqAlign) {
			if (space < smallest)
				smallest = space;
		}
		if (listSecondaryTargetSequences != null) {
			for (int space : spacesSecondaryTargetSequences) {
				if (space < smallest)
					smallest = space;
			}
		}
		//--
		for (int i=0; i<lstTargetSeqAlign.size(); i++) {
			String targetSeqAligned = lstTargetSeqAlign.get(i);
			targetSeqAligned = targetSeqAligned.substring(smallest, targetSeqAligned.length());
			lstTargetSeqAlign.remove(i);
			lstTargetSeqAlign.add(i, targetSeqAligned);
			
			String anotherSeqAligned = lstAnotherSeqsAlign.get(i);
			anotherSeqAligned = anotherSeqAligned.substring(smallest, anotherSeqAligned.length());
			lstAnotherSeqsAlign.remove(i);
			lstAnotherSeqsAlign.add(i, anotherSeqAligned);
		}
		if (listSecondaryTargetSequences != null) {
			for (int i=0; i<lstTargetSeqAlignSecondary.size(); i++) {
				String targetSeqAligned = lstTargetSeqAlignSecondary.get(i);
				targetSeqAligned = targetSeqAligned.substring(smallest, targetSeqAligned.length());
				lstTargetSeqAlignSecondary.remove(i);
				lstTargetSeqAlignSecondary.add(i, targetSeqAligned);
					
				String anotherSeqAligned = lstAnotherSeqsAlignSecondary.get(i);
				anotherSeqAligned = anotherSeqAligned.substring(smallest, anotherSeqAligned.length());
				lstAnotherSeqsAlignSecondary.remove(i);
				lstAnotherSeqsAlignSecondary.add(i, anotherSeqAligned);
				
			}
		}
    }
    
    // ========== Monta o header com a "r�gua" indicando o tamanho das sequencias alinhadas ========== ---------- ---------- ---------- ----------
    
    private void buildHeader(StringBuilder comparationsAlign) {
    	int biggestSize = 1;
    	if (alignmentType == ALIGNMENT_TYPE_PAIR && doAlignment) {
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
    
    // ========== Aplicar html nas Diferen�as ========== ---------- ---------- ---------- ----------
	
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
 	
 	// ========== Mostra as descri��es das sequ�ncias ========== ---------- ---------- ---------- ----------
 	
 	private void showSeqNames() {
		StringBuilder descriptions = new StringBuilder();
		descriptions.append(HTML_STYLE);
		descriptions.append("Legenda (cores): ");
		descriptions.append("<span style=\"background:" + HTML_COLOR_BLUE + ";\">").append("azul").append("</span>");
		descriptions.append(" Sequ�ncia alvo (principal); ");
		descriptions.append("<span style=\"background:" + HTML_COLOR_GREEN + ";\">").append("verde").append("</span>");
		descriptions.append(" Sequ�ncia(s) alvo secund�ria(s); ");
		descriptions.append("<span style=\"background:" + HTML_COLOR_RED + ";\">").append("vermelho").append("</span>");
		descriptions.append(" Diferen�as da(s) sequ�ncia(s) n�o alvo e alvo(s) secund�ria(s) em rela��o � sequ�ncia alvo (principal); ");
		descriptions.append("<br/>-<br/>");
		// Mostra a descri��o da sequ�ncia alvo
		descriptions.append("<span style=\"background:" + HTML_COLOR_BLUE + ";\">").append(listDescriptions.get(0)).append("</span>");
		descriptions.append("<br/>");
		// Mostra a descri��o das sequ�ncias alvo secund�rias
		for (String description : listDescriptionsSecondaryTarget) {
			descriptions.append("<span style=\"background:" + HTML_COLOR_GREEN + ";\">").append(description).append("</span>");
			descriptions.append("<br/>");
		}
		// Mostra a descri��o das sequ�ncias n�o alvo
		for (int i=1; i<listDescriptions.size(); i++) {
			String description = listDescriptions.get(i);
			descriptions.append(description);
			descriptions.append("<br/>");
		}
		descriptions.append("<br/>-<br/>");
		// Carrega o conte�do
		wvSeqNames.getEngine().loadContent(descriptions.toString());
 	}
 	
 	// ========== Desenhar os Oligonucleot�deos ========== ---------- ---------- ---------- ----------
 	
  	private void designOligos() {
  		if (mapDiffs != null) {
  			contentInit.append("Desenhando os Oligonucleot�deos ...");
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
					
					//Desenha os oligonocleot�deos
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
		        	contentInit.append("Oligonucleot�deos desenhados. (" + decimalFormat.format(getValue()) + "s)");
					contentInit.append("<br/>-<br/>");
					contentInit.append("Veja o resultado do alinhamento e da compara��o na aba de \'Alinhamento e Compara��o\'.");
					contentInit.append("<br/>-<br/>");
					contentInit.append("Veja o resultado do desenho dos oligonucleot�deos na aba de \'Resultado\'.");
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
		        	contentInit.append("Falha ao desenhar os Oligonucleot�deos.");
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
  			contentInit.append("Falha ao desenhar os Oligonucleot�deos.");
			contentInit.append("<br/>-<br/>");
			showContentInit();
  		}
  	}
  	
  	private void oligosDesign(StringBuilder result) {
  		// == -- == -- Nesta parte monta uma lista com Maps de diferen�as pr�ximas que podem estar em regi�o candidata a molde de Oligo
  		
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
		// == -- == -- Nesta parte apenas mostra quantas diferen�as proximas tem, s� pra ver (debug)
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
			
			// == -- == -- Nesta parte apenas mostra as diferen�as e suas posi��es, s� pra ver (debug)
			/*for (Entry<Integer, DesignOligo> entry1 : mapDiffsPositions.entrySet()) {
				for (Entry<Integer, List<DiffDesignOligo>> entry2 : entry1.getValue().getMapDiffDesignOligo().entrySet()) {
					for (DiffDesignOligo diffDesignOligo : entry2.getValue()) {
						result.append(diffDesignOligo);
						result.append("<br/>");
					}
				}
			}*/
			
			// == -- == -- Nesta parte seleciona as 3 regi�es que podem servir de molde para o desenho dos oligos
			
			// == Primeiro pega da map apenas as diferen�as que interessam, pois s� interessam as diferen�as presentes em uma mesma regi�o que possui diferen�a em todas as sequencias n�o alvo
			Map<Integer, DesignOligo> mapDiffsPositionsValid = new HashMap<>();
			for (Entry<Integer, DesignOligo> entry : mapDiffsPositions.entrySet()) {
				int qtySequences = compareSecondaryTargets && listSecondaryTargetSequences != null ? listSequences.size() + listSecondaryTargetSequences.size() : listSequences != null ? listSequences.size() : lstMultipleSeqsAlign.size()-1;
				if (entry.getValue().getMapDiffDesignOligo().size() == qtySequences-1) {
					mapDiffsPositionsValid.put(entry.getKey(), entry.getValue());
				}
			}
			if (!mapDiffsPositionsValid.isEmpty()) {

				// == Depois ordena por quantidade de diferen�as (decrescente, ou seja, maior primeiro, mais diferen�as primeiro)
				// https://www.baeldung.com/java-hashmap-sort#:~:text=Since%20the%20Java%208%2C%20we,over%20the%20map's%20stream%20pipeline.
				Map<Integer, DesignOligo> mapSorted = 
						mapDiffsPositionsValid.entrySet().stream().sorted(Map.Entry.comparingByValue((m1, m2) -> Integer.valueOf(m2.getTotalDiffsCount()).compareTo(m1.getTotalDiffsCount())))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
				
				// == Depois pega os 3 primeiros itens, se tiver 3, que s�o os que devem servir de molde para o desenho dos oligos
				//result.append("-<br/>Regioes<br/>-<br/>"); // -- == -- s� pra ver (debug)
				int sizeMapSorted = mapSorted.size();
				int qtyOligosToDesign = /*sizeMapSorted >= 3 ? 3 :*/ sizeMapSorted;
				int i = 0;
				List<DesignOligo> lstRegionsToDesignOligo = new ArrayList<>();
				int repeat = sizeMapSorted;
				while(repeat > 0) {
					qtyOligosToDesign = qtyOligosToDesign - lstRegionsToDesignOligo.size();
					for (Entry<Integer, DesignOligo> entry : mapSorted.entrySet()) {
						if (i < qtyOligosToDesign) {
							lstRegionsToDesignOligo.add(entry.getValue());
							
							// -- == -- Nesta parte apenas mostra as regi�es, s� pra ver (debug)
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
					//result.append("-<br/>"); // -- == -- s� pra ver (debug)
					
					// == Depois v� se n�o tem nehum molde de oligo que pode estar "sobreposto" a outro, se tiver, tem que substituir, caso tenha regioes para substituir
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
						repeat = -1; // Parar la�o While
					}
					repeat--;
				}
				
				// == Depois obt�m o molde dos oligonucleot�deos
				if (!lstRegionsToDesignOligo.isEmpty()) {
					
					String forwardPrimerRegion = "";
					String probeRegion = "";
					String reversePrimerRegion = "";
					String forwardPrimerStartPosition = "";
					String probeStartPosition = "";
					String reversePrimerStartPosition = "";
					String forwardPrimerEndPosition = "";
					String probeEndPosition = "";
					String reversePrimerEndPosition = "";
					
					int indexToGetOligo = 0;
					while (indexToGetOligo < lstRegionsToDesignOligo.size()) {
						
						// ordena a lista por firstDiffStartPosition em ordem crescente
						Collections.sort(lstRegionsToDesignOligo, new Comparator<DesignOligo>() {
				            @Override
				            public int compare(DesignOligo o1, DesignOligo o2) {
				            	return Integer.valueOf(o1.getFirstDiffStartPosition()).compareTo(o2.getFirstDiffStartPosition());
				            }
				        });
						String targetSeqAligned = lstTargetSeqAlign.get(0);
						int forwardPrimerStartPos = lstRegionsToDesignOligo.get(indexToGetOligo).getFirstDiffStartPosition() - 1;
						int forwardPrimerEndPos = forwardPrimerStartPos + oligoSize;
						forwardPrimerRegion = targetSeqAligned.substring(forwardPrimerStartPos, forwardPrimerStartPos + oligoSize);
						while (forwardPrimerRegion.contains("-")) {
							int countSpaces = ((int)forwardPrimerRegion.chars().filter(ch -> ch == '-').count());
							forwardPrimerRegion = forwardPrimerRegion.replaceAll("-", "");
							String complement = targetSeqAligned.substring(forwardPrimerStartPos + oligoSize, forwardPrimerStartPos + oligoSize + countSpaces);
							if (!complement.contains("-")) {
								forwardPrimerRegion += complement;
								forwardPrimerEndPos += countSpaces;
							}
						}
						
						// Valida temperatura de melting
						int forwardPrimerTM = calculateTM(forwardPrimerRegion);
						if (forwardPrimerTM > maxTM) {
							while (forwardPrimerTM > maxTM) {
								forwardPrimerEndPos = forwardPrimerEndPos - 1;
								forwardPrimerRegion = forwardPrimerRegion.substring(0, forwardPrimerRegion.length()-2);
								forwardPrimerTM = calculateTM(forwardPrimerRegion);
							}
						} else if (forwardPrimerTM < minTM) {
							while (forwardPrimerTM < minTM) {
								forwardPrimerEndPos = forwardPrimerEndPos + 1;
								forwardPrimerRegion = targetSeqAligned.substring(forwardPrimerStartPos, forwardPrimerEndPos);
								forwardPrimerTM = calculateTM(forwardPrimerRegion);
							}
						}
						
						// Valida maxConsecutiveNucleotides
						if (haveMoreThanMaxConsecutiveNucleotides(forwardPrimerRegion)) {
							indexToGetOligo ++;
							
							forwardPrimerStartPos = 0;
							forwardPrimerEndPos = 0;
							forwardPrimerStartPosition = "";
							forwardPrimerEndPosition = "";
							forwardPrimerRegion = "";
							
							continue;
						}
						
						// Valida percentCG
						if (!validPercentCG(forwardPrimerRegion)) {
							indexToGetOligo ++;
							
							forwardPrimerStartPos = 0;
							forwardPrimerEndPos = 0;
							forwardPrimerStartPosition = "";
							forwardPrimerEndPosition = "";
							forwardPrimerRegion = "";
							
							continue;
						}
						
						forwardPrimerStartPosition = "" + (forwardPrimerStartPos + 1);
						forwardPrimerEndPosition = "" + forwardPrimerEndPos;
						
						indexToGetOligo ++;
	
						while (indexToGetOligo < lstRegionsToDesignOligo.size()) {
							int probeStartPos = lstRegionsToDesignOligo.get(indexToGetOligo).getFirstDiffStartPosition() - 1;
							int probeEndPos = probeStartPos + oligoSize;
							if (probeEndPos - forwardPrimerStartPos <= ampliconSize) { // Valida tamanho amplicon
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
								
								// Valida temperatura de melting
								int probeTM = calculateTM(probeRegion);
								while (probeTM <= forwardPrimerTM) {
									probeEndPos = probeEndPos + 1;
									probeRegion = targetSeqAligned.substring(probeStartPos, probeEndPos);
									probeTM = calculateTM(probeRegion);
								}
								
								// Valida maxConsecutiveNucleotides
								if (haveMoreThanMaxConsecutiveNucleotides(probeRegion)) {
									indexToGetOligo ++;
									
									probeStartPos = 0;
									probeEndPos = 0;
									probeStartPosition = "";
									probeEndPosition = "";
									probeRegion = "";
									
									continue;
								}
								
								// Valida percentCG
								if (!validPercentCG(probeRegion)) {
									indexToGetOligo ++;
									
									probeStartPos = 0;
									probeEndPos = 0;
									probeStartPosition = "";
									probeEndPosition = "";
									probeRegion = "";
									
									continue;
								}
							}
							
							break;
						}
						
						indexToGetOligo ++;
						
						while (indexToGetOligo < lstRegionsToDesignOligo.size()) {
							int reversePrimerStartPos = lstRegionsToDesignOligo.get(indexToGetOligo).getFirstDiffStartPosition() - 1;
							int reversePrimerEndPos = reversePrimerStartPos + oligoSize;
							if (reversePrimerEndPos - forwardPrimerStartPos <= ampliconSize) { // Valida tamanho amplicon
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
								
								// Valida temperatura de melting
								int reversePrimerTM = calculateTM(reversePrimerRegion);
								if (reversePrimerTM > maxTM) {
									while (reversePrimerTM > maxTM) {
										reversePrimerEndPos = reversePrimerEndPos - 1;
										reversePrimerRegion = reversePrimerRegion.substring(0, reversePrimerRegion.length()-2);
										reversePrimerTM = calculateTM(reversePrimerRegion);
									}
								} else if (reversePrimerTM < minTM) {
									while (reversePrimerTM < minTM) {
										reversePrimerEndPos = reversePrimerEndPos + 1;
										reversePrimerRegion = targetSeqAligned.substring(reversePrimerStartPos, reversePrimerEndPos);
										reversePrimerTM = calculateTM(reversePrimerRegion);
									}
								}
								
								// Valida maxConsecutiveNucleotides
								if (haveMoreThanMaxConsecutiveNucleotides(reversePrimerRegion)) {
									indexToGetOligo ++;
									
									reversePrimerStartPos = 0;
									reversePrimerEndPos = 0;
									reversePrimerStartPosition = "";
									reversePrimerEndPosition = "";
									reversePrimerRegion = "";
									
									continue;
								}
								
								// Valida percentCG
								if (!validPercentCG(reversePrimerRegion)) {
									indexToGetOligo ++;
									
									reversePrimerStartPos = 0;
									reversePrimerEndPos = 0;
									reversePrimerStartPosition = "";
									reversePrimerEndPosition = "";
									reversePrimerRegion = "";
									
									continue;
								}
							}
							
							indexToGetOligo ++;
							
							break;
						}
						
						indexToGetOligo ++;
						
						break;
					}
					result.append("-<br/>");
					result.append("- ________________________ " + forwardPrimerStartPosition + " - " + forwardPrimerEndPosition);
					result.append("<br/>");
					result.append(" - Regi�o Primer 'Forward': ");
					result.append(forwardPrimerRegion);
					result.append("<br/>-<br/>");
					result.append("- ________________________ " + probeStartPosition + " - " + probeEndPosition);
					result.append("<br/>");
					result.append(" - __________ Regi�o Sonda: ");
					result.append(probeRegion);
					result.append("<br/>-<br/>");
					result.append("- ________________________ " + reversePrimerStartPosition + " - " + reversePrimerEndPosition);
					result.append("<br/>");
					result.append(" - _ Regi�o Primer Reverso: ");
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
					result.append(reversePrimer); // reversePrimer
					result.append("<br/>-<br/>");
					// --
					
					result.append("<br/>-<br/>");
					result.append(" - Temperaturas de Melting (TM):");
					result.append("<br/>");
					result.append(" - (4xCGs + 2xATs)");
					result.append("<br/>-<br/>");
					result.append(" - TM Primer 'Forward': ");
					result.append(calculateTM(forwardPrimerRegion)); // TM forwardPrimer
					result.append("<br/>-<br/>");
					result.append(" - __________ TM Sonda: ");
					result.append(calculateTM(probeRegion)); // TM probe
					result.append("<br/>-<br/>");
					result.append(" - _ TM Primer Reverso: ");
					result.append(calculateTM(reversePrimer)); // TM reversePrimer
					result.append("<br/>-<br/>");
					// --
					
					float percCGForwardPrimerRegion = calculatePercCG(forwardPrimerRegion);
					float percCGProbeRegion = calculatePercCG(probeRegion);
					float percCGReversePrimerRegion = calculatePercCG(reversePrimer);
					
					result.append("<br/>-<br/>");
					result.append(" - Porcentagens de CG e AT:");
					result.append("<br/>-<br/>");
					result.append(" - % CG Primer 'Forward': "); // % forwardPrimer
					result.append(percCGForwardPrimerRegion);
					result.append(" - % AT Primer 'Forward': ");
					result.append(percCGForwardPrimerRegion > 0 ? 100 - percCGForwardPrimerRegion : 0.0);
					result.append("<br/>-<br/>");
					result.append(" - __________ % CG Sonda: "); // % probe
					result.append(percCGProbeRegion);
					result.append(" - __________ % AT Sonda: ");
					result.append(percCGProbeRegion > 0 ? 100 - percCGProbeRegion : 0.0);
					result.append("<br/>-<br/>");
					result.append(" - _ % CG Primer Reverso: "); // % reversePrimer
					result.append(percCGReversePrimerRegion);
					result.append(" - _ % AT Primer Reverso: ");
					result.append(percCGReversePrimerRegion > 0 ? 100 - percCGReversePrimerRegion : 0.0);
					result.append("<br/>-<br/>");
					// --
					
					StringBuilder messageOligosRegions = new StringBuilder();
					messageOligosRegions.append("Regi�o do Primer 'Forward': " + forwardPrimerRegion + " (" + forwardPrimerStartPosition + " - " + forwardPrimerEndPosition + "); ");
					messageOligosRegions.append("Regi�o da Sonda: " + probeRegion + " (" + probeStartPosition + " - " + probeEndPosition + "); ");
					messageOligosRegions.append("Regi�o do Primer Reverso: " + reversePrimerRegion + " (" + reversePrimerStartPosition + " - " + reversePrimerEndPosition + ").");
					messageToDisplayInLblOligosRegions = messageOligosRegions.toString();
				} else {
					String messageOligosNotDesigned = "N�o foi poss�vel desenhar os Oligonucleot�dos.";
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
  	
  	private int calculateTM(String oligo) {
  		// (4xCGs + 2xATs)
  		int countC = 0;
  		int countG = 0;
  		int countA = 0;
  		int countT = 0;
  		for (int i=0; i<oligo.length(); i++) {
  			char c = oligo.charAt(i);
  			if (c == 'C' || c == 'c') {
  				countC++;
  			} else if (c == 'G' || c == 'g') {
  				countG++;
  			} else if (c == 'A' || c == 'a') {
  				countA++;
  			} else if (c == 'T' || c == 't') {
  				countT++;
  			}
  		}
  		int tm = 4 * (countC + countG) + 2 * (countA + countT);
  		return tm;
  	}
  	
  	private float calculatePercCG(String oligo) {
  		int countC = 0;
  		int countG = 0;
  		for (int i=0; i<oligo.length(); i++) {
  			char c = oligo.charAt(i);
  			if (c == 'C' || c == 'c') {
  				countC++;
  			} else if (c == 'G' || c == 'g') {
  				countG++;
  			}
  		}
  		float percCG = 0;
  		if (oligo.length() > 0)
  			percCG = (countC + countG) * 100 / oligo.length();
  		return percCG;
  	}
  	
  	private boolean haveMoreThanMaxConsecutiveNucleotides(String oligo) {
  		int maxConsecutive = 1;
  		int consecutive = 1;
  		for (int i=1; i<oligo.length(); i++) {
  			char c1 = oligo.charAt(i-1);
  			char c2 = oligo.charAt(i);
  			if (c1 == c2)
  				consecutive ++;
  			else
  				consecutive = 1;
  			if (consecutive > maxConsecutive)
  				maxConsecutive = consecutive;
  		}
  		return maxConsecutive > maxConsecutiveNucleotides;
  	}
  	
  	private boolean validPercentCG(String oligo) {
  		float percentCG = calculatePercCG(oligo);
  		return percentCG <= maxPercentCG && percentCG >= minPercentCG;
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
				// quando tem uma diferen�a com mais de um nucleotideo, 
				// por exemplo com 3 nucleot�deos (startPosition = 75 e endPosition = 77 (75,76 e 77)), 
				// neste caso quero que conte como 3 diferen�as.
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
   		contentInit.append("Buscando sequ�ncias na web ...");
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
	        	contentInit.append("Sequ�ncias carregadas. (Tempo total: " + decimalFormat.format(getValue()) + "s)");
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
	        	contentInit.append("Falha ao carregar as sequ�ncias da web.");
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
   		String messageError = "Falha na busca da(s) sequ�ncia(s): ";
   		List<String> linesToWrite = null;
   		try {
   			URL fastaUrl = new URL(String.format("https://www.ncbi.nlm.nih.gov/search/api/download-sequence/?db=nuccore&id=%s", fastaName));
   			InputStream is = fastaUrl.openStream();
   			InputStreamReader isr = new InputStreamReader(is);
   			BufferedReader br = new BufferedReader(isr);

   			StringBuilder inputSequence = new StringBuilder();
   			String line = "";
   	        line = br.readLine(); // read first line // A primeira linha do arquivo fasta n�o faz parte da sequ�ncia, � uma descri��o
   	        
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
	 			listDescriptionsSecondaryTarget.add(line); // A primeira linha do arquivo fasta n�o faz parte da sequ�ncia, � uma descri��o
	 		} else {
	 			listDescriptions.add(line); // A primeira linha do arquivo fasta n�o faz parte da sequ�ncia, � uma descri��o
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
   	        	writeFile(directoryToSave, fastaName, "fasta", linesToWrite);
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
   	
   	public static void writeFile(String directory, String fileName, String extension, List<String> lines) {
   		FileWriter fw = null;
   		BufferedWriter bw = null;
   		try {
	   		File file = new File(directory + "/" + fileName + "." + extension);
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
