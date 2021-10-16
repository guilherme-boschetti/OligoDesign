package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class DesignParams {
	
	// ========== FXML attributes ========== ---------- ---------- ---------- ----------
	
	@FXML
	private ComboBox<OligoSize> cmbOligoSize;
	
	@FXML
	private ComboBox<OligoSize> cmbAmpliconSize;
	
	@FXML
	private ComboBox<OligoSize> cmbMaxConsecutiveNucleotides;
	
	@FXML
	private ComboBox<OligoSize> cmbMinTM;
	
	@FXML
	private ComboBox<OligoSize> cmbMaxTM;
	
	@FXML
	private ComboBox<OligoSize> cmbMinPercentCG;
	
	@FXML
	private ComboBox<OligoSize> cmbMaxPercentCG;
    
    // ========== attributes ========== ---------- ---------- ---------- ----------
	
	private int oligoSize = 20;
	private int ampliconSize = 150;
	private int maxConsecutiveNucleotides = 5;
	private int minTM = 55;
	private int maxTM = 62;
	private int minPercentCG = 30;
	private int maxPercentCG = 70;
    
    private IParametersConfigurations iParametersConfigurations;
    
    // ========== initialization ========== ---------- ---------- ---------- ----------
    
    @FXML
	public void initialize() {
    	loadAttributes();
    	loadComboBoxOligoSize();
    	loadComboBoxAmpliconSize();
    	loadComboBoxMaxConsecutiveNucleotides();
    	loadComboBoxMinTM();
    	loadComboBoxMaxTM();
    	loadComboBoxMinPercentCG();
    	loadComboBoxMaxPercentCG();
    }
    
    private void loadAttributes() {
    	File config = new File("C:/OligoDesign/DesignParams.txt");
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
			}
		}
    }
    
    // ========== cmbOligoSize ========== ---------- ---------- ---------- ----------
    
    private void loadComboBoxOligoSize() {
		loadComboBoxOligoSizeOptions();
		setComboboxOligoSizeEventChangeListener();
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
		//int indexOligoSize = 5;
		//cmbOligoSize.getSelectionModel().select(indexOligoSize);
		cmbOligoSize.setValue(new OligoSize(oligoSize, oligoSize + " nucleotídeos"));
	}
	
	private void setComboboxOligoSizeEventChangeListener() {
		cmbOligoSize.valueProperty().addListener(new ChangeListener<OligoSize>() {
			@Override
			public void changed(ObservableValue<? extends OligoSize> observable, OligoSize oldValue, OligoSize newValue) {
				if (newValue != null) {
					oligoSize = newValue.getSize();
				}
			}
		});
	}
	
	// ========== cmbAmpliconSize ========== ---------- ---------- ---------- ----------
	
	private void loadComboBoxAmpliconSize() {
		loadComboBoxAmpliconSizeOptions();
		setComboboxAmpliconSizeEventChangeListener();
		setComboBoxAmpliconSizeDefaultOption();
	}
    
    private void loadComboBoxAmpliconSizeOptions() {
		ObservableList<OligoSize> lstAmpliconSize = FXCollections.observableArrayList();
		for (int i=100; i<=200; i++) {
			lstAmpliconSize.add(new OligoSize(i, i + " nucleotídeos"));
		}
		cmbAmpliconSize.setItems(lstAmpliconSize);
	}
	
	private void setComboBoxAmpliconSizeDefaultOption() {
		//int indexAmpliconSize = 50;
		//cmbAmpliconSize.getSelectionModel().select(indexAmpliconSize);
		cmbAmpliconSize.setValue(new OligoSize(ampliconSize, ampliconSize + " nucleotídeos"));
	}
	
	private void setComboboxAmpliconSizeEventChangeListener() {
		cmbAmpliconSize.valueProperty().addListener(new ChangeListener<OligoSize>() {
			@Override
			public void changed(ObservableValue<? extends OligoSize> observable, OligoSize oldValue, OligoSize newValue) {
				if (newValue != null) {
					ampliconSize = newValue.getSize();
				}
			}
		});
	}
	
	// ========== cmbMaxConsecutiveNucleotides ========== ---------- ---------- ---------- ----------
	
	private void loadComboBoxMaxConsecutiveNucleotides() {
		loadComboBoxMaxConsecutiveNucleotidesOptions();
		setComboboxMaxConsecutiveNucleotidesEventChangeListener();
		setComboBoxMaxConsecutiveNucleotidesDefaultOption();
	}
    
    private void loadComboBoxMaxConsecutiveNucleotidesOptions() {
		ObservableList<OligoSize> lstMaxConsecutiveNucleotides = FXCollections.observableArrayList();
		for (int i=2; i<=8; i++) {
			lstMaxConsecutiveNucleotides.add(new OligoSize(i, i + " nucleotídeos"));
		}
		cmbMaxConsecutiveNucleotides.setItems(lstMaxConsecutiveNucleotides);
	}
	
	private void setComboBoxMaxConsecutiveNucleotidesDefaultOption() {
		//int indexMaxConsecutiveNucleotides = 3;
		//cmbMaxConsecutiveNucleotides.getSelectionModel().select(indexMaxConsecutiveNucleotides);
		cmbMaxConsecutiveNucleotides.setValue(new OligoSize(maxConsecutiveNucleotides, maxConsecutiveNucleotides + " nucleotídeos"));
	}
	
	private void setComboboxMaxConsecutiveNucleotidesEventChangeListener() {
		cmbMaxConsecutiveNucleotides.valueProperty().addListener(new ChangeListener<OligoSize>() {
			@Override
			public void changed(ObservableValue<? extends OligoSize> observable, OligoSize oldValue, OligoSize newValue) {
				if (newValue != null) {
					maxConsecutiveNucleotides = newValue.getSize();
				}
			}
		});
	}
	
	// ========== cmbMinTM ========== ---------- ---------- ---------- ----------
	
	private void loadComboBoxMinTM() {
		loadComboBoxMinTMOptions();
		setComboboxMinTMEventChangeListener();
		setComboBoxMinTMDefaultOption();
	}
    
    private void loadComboBoxMinTMOptions() {
		ObservableList<OligoSize> lstMinTM = FXCollections.observableArrayList();
		for (int i=50; i<=57; i++) {
			lstMinTM.add(new OligoSize(i, i + "º"));
		}
		cmbMinTM.setItems(lstMinTM);
	}
	
	private void setComboBoxMinTMDefaultOption() {
		//int indexMinTM = 5;
		//cmbMinTM.getSelectionModel().select(indexMinTM);
		cmbMinTM.setValue(new OligoSize(minTM, minTM + "º"));
	}
	
	private void setComboboxMinTMEventChangeListener() {
		cmbMinTM.valueProperty().addListener(new ChangeListener<OligoSize>() {
			@Override
			public void changed(ObservableValue<? extends OligoSize> observable, OligoSize oldValue, OligoSize newValue) {
				if (newValue != null) {
					minTM = newValue.getSize();
				}
			}
		});
	}
	
	// ========== cmbMaxTM ========== ---------- ---------- ---------- ----------
	
	private void loadComboBoxMaxTM() {
		loadComboBoxMaxTMOptions();
		setComboboxMaxTMEventChangeListener();
		setComboBoxMaxTMDefaultOption();
	}
    
    private void loadComboBoxMaxTMOptions() {
		ObservableList<OligoSize> lstMaxTM = FXCollections.observableArrayList();
		for (int i=58; i<=65; i++) {
			lstMaxTM.add(new OligoSize(i, i + "º"));
		}
		cmbMaxTM.setItems(lstMaxTM);
	}
	
	private void setComboBoxMaxTMDefaultOption() {
		//int indexMaxTM = 4;
		//cmbMaxTM.getSelectionModel().select(indexMaxTM);
		cmbMaxTM.setValue(new OligoSize(maxTM, maxTM + "º"));
	}
	
	private void setComboboxMaxTMEventChangeListener() {
		cmbMaxTM.valueProperty().addListener(new ChangeListener<OligoSize>() {
			@Override
			public void changed(ObservableValue<? extends OligoSize> observable, OligoSize oldValue, OligoSize newValue) {
				if (newValue != null) {
					maxTM = newValue.getSize();
				}
			}
		});
	}
	
	// ========== cmbMinPercentCG ========== ---------- ---------- ---------- ----------
	
	private void loadComboBoxMinPercentCG() {
		loadComboBoxMinPercentCGOptions();
		setComboboxMinPercentCGEventChangeListener();
		setComboBoxMinPercentCGDefaultOption();
	}
    
    private void loadComboBoxMinPercentCGOptions() {
		ObservableList<OligoSize> lstMinPercentCG = FXCollections.observableArrayList();
		for (int i=5; i<=45; i++) {
			lstMinPercentCG.add(new OligoSize(i, i + "%"));
		}
		cmbMinPercentCG.setItems(lstMinPercentCG);
	}
	
	private void setComboBoxMinPercentCGDefaultOption() {
		//int indexMinPercentCG = 4;
		//cmbMinPercentCG.getSelectionModel().select(indexMinPercentCG);
		cmbMinPercentCG.setValue(new OligoSize(minPercentCG, minPercentCG + "%"));
	}
	
	private void setComboboxMinPercentCGEventChangeListener() {
		cmbMinPercentCG.valueProperty().addListener(new ChangeListener<OligoSize>() {
			@Override
			public void changed(ObservableValue<? extends OligoSize> observable, OligoSize oldValue, OligoSize newValue) {
				if (newValue != null) {
					minPercentCG = newValue.getSize();
				}
			}
		});
	}
	
	// ========== cmbMaxPercentCG ========== ---------- ---------- ---------- ----------
	
		private void loadComboBoxMaxPercentCG() {
			loadComboBoxMaxPercentCGOptions();
			setComboboxMaxPercentCGEventChangeListener();
			setComboBoxMaxPercentCGDefaultOption();
		}
	    
	    private void loadComboBoxMaxPercentCGOptions() {
			ObservableList<OligoSize> lstMaxPercentCG = FXCollections.observableArrayList();
			for (int i=55; i<=95; i++) {
				lstMaxPercentCG.add(new OligoSize(i, i + "%"));
			}
			cmbMaxPercentCG.setItems(lstMaxPercentCG);
		}
		
		private void setComboBoxMaxPercentCGDefaultOption() {
			//int indexMaxPercentCG = 4;
			//cmbMaxPercentCG.getSelectionModel().select(indexMaxPercentCG);
			cmbMaxPercentCG.setValue(new OligoSize(maxPercentCG, maxPercentCG + "%"));
		}
		
		private void setComboboxMaxPercentCGEventChangeListener() {
			cmbMaxPercentCG.valueProperty().addListener(new ChangeListener<OligoSize>() {
				@Override
				public void changed(ObservableValue<? extends OligoSize> observable, OligoSize oldValue, OligoSize newValue) {
					if (newValue != null) {
						maxPercentCG = newValue.getSize();
					}
				}
			});
		}
    
    // ========== screen buttons events ========== ---------- ---------- ---------- ----------
	
	@FXML
    void save(ActionEvent event) {
		
		try {
			String line1 = "Comprimento dos Oligonucleotídeos = " + oligoSize;
			String line2 = "Tamanho máximo do Amplicon = " + ampliconSize;
			String line3 = "Número máximo de nucleotídeos consecutivos = " + maxConsecutiveNucleotides;
			String line4 = "Temperatura de melting mínima = " + minTM;
			String line5 = "Temperatura de melting máxima = " + maxTM;
			String line6 = "Porcentagem de CG mínima = " + minPercentCG;
			String line7 = "Porcentagem de CG máxima = " + maxPercentCG;
			// Proporção entre GC e AT mais ou menos 50%, sonda pode ter mais GC que primer, pois tem que ter temperatura maior.
			// Temperatura de melting da sonda tem que ficar aproximadamente 10 graus a mais que dos primers.
			List<String> lines = new ArrayList<>();
			lines.add(line1);
			lines.add(line2);
			lines.add(line3);
			lines.add(line4);
			lines.add(line5);
			lines.add(line6);
			lines.add(line7);
			
			File config = new File("C:/OligoDesign/DesignParams.txt");
			config.delete();
			
			Main.writeFile("C:/OligoDesign", "DesignParams", "txt", lines);
		} catch(Exception e) {
			// do nothing
		}

		iParametersConfigurations.loadConfigurations();
    }
    
    @FXML
    void cancel(ActionEvent event) {

    	Stage stage = (Stage)cmbOligoSize.getScene().getWindow();
    	stage.close();
    }
    
    // ========== setter method ========== ---------- ---------- ---------- ----------
	
	public void setIParametersConfigurations(IParametersConfigurations iParametersConfigurations) {
		this.iParametersConfigurations = iParametersConfigurations;
	}
	
	// ========== ========== ---------- ---------- ---------- ----------
	
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
}
