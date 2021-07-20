package application.utils;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.stream.Stream;

/**
 * This class uses the Needleman-Wunsch algorithm
 * to align nucleotide sequences.
 */
public class NeedlemanWunschUsingFile {

    // Scoring scheme for match, mismatch, and indel
    private final int MATCH;
    private final int MISMATCH;
    private final int INDEL;

    // Strands to be analyzed
    private String strand1;
    private String strand2;
    
    // Validity of mismatching
    private boolean allowMismatch;

    private String[] alignedStrands;
    
    //private String directory = "C:\\OligoDesignTemp\\";
	private String fileName = "matrix.txt";
	
	LargeMatrix largeMatrixFile;

    /**
     * Constructor taking in two strands.
     * Default values:
     * MATCH = 1
     * MISMATCH = -1
     * INDEL = -1
     * allowMismatch = true
     *
     * @param strand1 the first strand
     * @param strand2 the second strand
     */
    public NeedlemanWunschUsingFile(String strand1, String strand2) {
        this(strand1, strand2, 1, -1, -1, true);
    }

    /**
     * Constructor taking in two strands and whether mismatching
     * is allowed.
     * Default values:
     * MATCH = 1
     * MISMATCH = -1 / -999
     * INDEL = -1
     *
     * @param strand1 the first strand
     * @param strand2 the second strand
     * @param allowMismatch whether mismatching is allowed
     */
    public NeedlemanWunschUsingFile(String strand1, String strand2, boolean allowMismatch) {
        this(strand1, strand2, 1, allowMismatch ? -1 : -999, -1, allowMismatch);
    }

    /**
     * Constructor taking in two strands and the scoring system.
     * Default values:
     * allowedMismatch = true
     *
     * @param strand1 the first strand
     * @param strand2 the second strand
     * @param match the value of a match
     * @param mismatch the value of a mismatch
     * @param indel the value of an indel
     */
    public NeedlemanWunschUsingFile(String strand1, String strand2, int match, int mismatch, int indel) {
        this(strand1, strand2, match, mismatch, indel, true);
    }

    /**
     * Constructor taking in two strands and the scoring system
     * and whether mismatching is allowed.
     * Calculates the solution matrix, the end score, and the
     * aligned strands.
     *
     * @param strand1 the first strand
     * @param strand2 the second strand
     * @param match the value of a match
     * @param mismatch the value of a mismatch
     * @param indel the value of an indel
     * @param allowMismatch whether mismatching is allowed
     */
    public NeedlemanWunschUsingFile(String strand1, String strand2, int match, int mismatch, int indel, boolean allowMismatch) {
        this.strand1 = strand1;
        this.strand2 = strand2;

        this.MATCH = match;
        this.MISMATCH = mismatch;
        this.INDEL = indel;

        this.allowMismatch = allowMismatch;

        // Init the matrix file
        initFile(strand1.length()+1, strand2.length()+1);
        // Calculate solution matrix
        findSolution();
        // Calculate aligned strands
        this.alignedStrands = findPath();
        // Close LargeMatrix
        closeLargeMatrix();
        // Delete the matrix file
        //deleteFile();
    }

    /**
     * Generates solution matrix given 2 RNA strands.
     * Uses the Needleman-Wunsch algorithm.
     *
     * @return the solution matrix
     */
    public void findSolution() {
        // Generate solution matrix based on lengths of both strands
        // Let strand1 be the side strand
        // Let strand2 be the top strand
    	// Set the matrix values to value of 0
        //initFile(strand1.length()+1, strand2.length()+1);

        // Fill in the top row. Moving to the right always adds the value of INDEL.
        for (int i = 1; i < strand2.length()+1; i++) {
            int solutionValue = getMatrixValue(0, i-1) + INDEL;
            setMatrixValue(0, i, solutionValue);
        }

        // Fill in the left column. Moving down always adds the value of INDEL.
        for (int i = 1; i < strand1.length()+1; i++) {
            int solutionValue = getMatrixValue(i-1, 0) + INDEL;
            setMatrixValue(i, 0, solutionValue);
        }

        // Fill in the rest of the matrix based on a few rules.
        for (int i = 1; i < strand1.length()+1; i++) {
            for (int j = 1; j < strand2.length()+1; j++) {

                int matchValue;

                // If the characters that correspond to the grid position are equal for both strands
                // Set the matchValue to MATCH, else set the matchValue to MISMATCH
                if (strand1.charAt(i-1) == strand2.charAt(j-1)) matchValue = MATCH;
                else matchValue = MISMATCH;

                // Set the value to the maximum of these three values
                    // Position to the left + INDEL
                    // Position above + INDEL
                    // Position top-left + matchVALUE
                int solutionValue = max(getMatrixValue(i, j-1) + INDEL, getMatrixValue(i-1, j) + INDEL, getMatrixValue(i-1, j-1) + matchValue);
                setMatrixValue(i, j, solutionValue);
            }
        }
    }

    /**
     * Helper method for calculating a maximum of three numbers.
     *
     * @return the maximum of the three given integers
     */
    private int max(int a, int b, int c) {
        return Math.max(Math.max(a, b), c);
    }

     /**
      * Aligns RNA strands based off a solution matrix.
      * Finds one of the 'best' paths in the solution matrix.
      * Uses the 'best' path to generate aligned RNA strands.
      * This method does so non-recursively.
      * 
      * @return the two aligned RNA strands
      */
    public String[] findPath() {
        // Let strand1 be the side strand
        // Let strand2 be the top strand
        String alignedStrand1 = "";
        String alignedStrand2 = "";

        // Start from the bottom right of the solution matrix
        int i = strand1.length();
        int j = strand2.length();

        boolean matchAllowed;
        int matchValue;

        // While you are not at the top/left side of the matrix
        // This prevents an OOB exception
        while (i != 0  && j != 0) {
            // Reset matchAllowed value
            matchAllowed = true;
            // If the characters are different, and mismatching is not allowed
            // Diagonal moves are not legal
            if (strand1.charAt(i-1) != strand2.charAt(j-1) && !allowMismatch) matchAllowed = false;
            // Calculate whether the diagonal move value
            if (strand1.charAt(i-1) == strand2.charAt(j-1)) matchValue = MATCH;
            else matchValue = MISMATCH;
            // Calculate the best path to the current position
            // If the top-left position is a valid traceback
            if (getMatrixValue(i-1, j-1) == getMatrixValue(i, j) - matchValue && matchAllowed) {
                // Add the character corresponding to that position to both strands
                // This is the case for either a match or mismatch
                alignedStrand1 = strand1.charAt(i-1) + alignedStrand1;
                alignedStrand2 = strand2.charAt(j-1) + alignedStrand2;
                // Move to the new position
                i -= 1;
                j -= 1;
            // If the left position is a valid traceback
            } else if (getMatrixValue(i, j-1) == getMatrixValue(i, j) - INDEL) {
                // Add '-' to strand1
                // Add the character correponding to that position to strand2
                // This represents a gap in the side strand
                alignedStrand1 = "-" + alignedStrand1;
                alignedStrand2 = strand2.charAt(j-1) + alignedStrand2;
                // Move to the new position
                j -= 1;
            // If the above position is a valid trackback
            } else {
                // Add '-' to strand2
                // Add the character corresponding to that position to strand1
                // This represents a gap in the top strand
                alignedStrand1 = strand1.charAt(i-1) + alignedStrand1;
                alignedStrand2 = "-" + alignedStrand2;
                // Move to the new position
                i -= 1;
            // If the top-left position is the best
            }
        }

        // If you are at the top of the matrix
        if (i == 0) {
            // Append characters corresponding to those positions to strand1
            // Append "-" for every space you are away from 0,0 to strand2
            // EX: If you are at 0,3 (j = 3), add "---" to strand2
            for (int k = 0; k < j; k++) {
                alignedStrand1 = "-" + alignedStrand1;
                alignedStrand2 = strand2.charAt(j-k) + alignedStrand2;
            }
        // If you are at the left most side of the matrix
        } else {
            // Append "-" for every space you are away from 0,0 to strand1
            // Append characters corresponding to those positions to strand2
            // EX: If you are at 3,0 (i = 3), add "---" to strand1
            for (int k = 0; k < i; k++) {
                alignedStrand1 = strand1.charAt(i-k) + alignedStrand1;
                alignedStrand2 = "-" + alignedStrand2;
            }
        }

        return new String[] {alignedStrand1, alignedStrand2};
    }

    /**
     * Method to return the aligned Strands.
     * @return the aligned strands
     */
    public String[] getAlignedStrands() {
        return alignedStrands;
    }
    
    // ==================================================
    
    private void initFile(int lines, int columns) {
		try {
			largeMatrixFile = new LargeMatrix(fileName, lines, columns);
	   		for (int i=0; i<lines; i++) {
	   			for (int j=0; j<columns; j++) {
	   				largeMatrixFile.set(i, j, 0);
	   			}
	   		}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
    
    private void closeLargeMatrix() {
    	try {
			largeMatrixFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private int getMatrixValue(int i, int j) {
    	return largeMatrixFile.get(i, j);
    }
    
    private void setMatrixValue(int i, int j, int value) {
    	largeMatrixFile.set(i, j, value);
    }
    
    // ---------------------------------------------------
    
    /*private void initFile(int lines, int columns) {
		try {
			File directoryFile = new File(directory);
			directoryFile.mkdir();
	   		File file = new File(directory + fileName);
	   		FileWriter fw = new FileWriter(file);
	   		BufferedWriter bw = new BufferedWriter(fw);
	   		for (int i=0; i<lines; i++) {
	   			for (int j=0; j<columns; j++) {
	   				bw.write("0");
	   				if (j < columns-1) {
	   					bw.write(";");
	   				}
	   			}
	   			bw.newLine();
	   		}
	   		bw.close();
	   		fw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
    
    private void deleteFile() {
    	File file = new File(directory + fileName);
    	file.delete();
    	File directoryFile = new File(directory);
		directoryFile.delete();
    }
    
    // https://www.educative.io/edpresso/reading-the-nth-line-from-a-file-in-java
    private int getMatrixValue(int i, int j) {
        try (Stream<String> lines = Files.lines(Paths.get(directory + fileName))) {
        	String line = lines.skip(i).findFirst().get();
        	String[] columns = line.split(";");
        	return Integer.valueOf(columns[j]);
        }catch(IOException e){
        	System.out.println(e);
        }
        return 0;
    }
    
    private void setMatrixValue(int i, int j, int value) {
    	// Pega a linha a alterar
    	try (Stream<String> lines = Files.lines(Paths.get(directory + fileName))) {
        	String line = lines.skip(i).findFirst().get();
        	String[] columns = line.split(";");
	    	// Altera o valor na linha
	    	if (columns != null) {
	    		//StringBuilder sb = new StringBuilder();
	    		String lineToWrite = "";
	    		for (int index=0; index<columns.length; index++) {
	    			if (index == j) {
	    				//sb.append(value);
	    				lineToWrite += value;
	    			} else {
	    				//sb.append(columns[index]);
	    				lineToWrite += columns[index];
	    			}
	    			if (index < columns.length-1) {
	    				//sb.append(";");
	    				lineToWrite += ";";
	   				}
	    		}
	    		// Grava a linha alterada
	        	Path path = Paths.get(directory + fileName);
	            List<String> linesToWrite = Files.readAllLines(path);
	            //linesToWrite.set(i, sb.toString());
	            linesToWrite.set(i, lineToWrite);
	            Files.write(path, linesToWrite);
	    	}
    	} catch (IOException e) {
        	System.out.println(e);
        } catch (Exception ex) {
        	System.out.println(ex);
        }
    }*/
}