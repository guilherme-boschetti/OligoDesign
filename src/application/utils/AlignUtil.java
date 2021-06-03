package application.utils;

import java.util.LinkedList;
import java.util.List;

import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.Profile;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.AmbiguityDNACompoundSet;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;

public class AlignUtil {

	public static String[] pairwiseAlignment (String targetSequence, String anotherSequence) throws CompoundNotFoundException {

		DNASequence target = new DNASequence(targetSequence, AmbiguityDNACompoundSet.getDNACompoundSet());
        DNASequence query = new DNASequence(anotherSequence, AmbiguityDNACompoundSet.getDNACompoundSet());

        SubstitutionMatrix<NucleotideCompound> matrix = SubstitutionMatrixHelper.getNuc4_4();

        SimpleGapPenalty gapP = new SimpleGapPenalty();

        SequencePair<DNASequence, NucleotideCompound> psa = Alignments.getPairwiseAlignment(query, target, Alignments.PairwiseSequenceAlignerType.GLOBAL, gapP, matrix);
        
        System.gc(); // Runs the garbage collector.
        
        String[] ret = new String[2];
        ret[0] = psa.getAlignedSequence(target).getSequenceAsString(); // target sequence
        ret[1] = psa.getAlignedSequence(query).getSequenceAsString(); // another sequence
        
        return ret;
    }
	
	public static List<String> multipleAlignment(List<String> listSequences) throws CompoundNotFoundException {
		
		List<DNASequence> lstDNASequences = new LinkedList<>();
		for (String sequence : listSequences) {
			lstDNASequences.add(new DNASequence(sequence, AmbiguityDNACompoundSet.getDNACompoundSet()));
		}
		
		System.gc(); // Runs the garbage collector.
        
		Profile<DNASequence, NucleotideCompound> msa = Alignments.getMultipleSequenceAlignment(lstDNASequences);
		
		System.gc(); // Runs the garbage collector.
        
        List<String> ret = new LinkedList<>();
        for (String seq : listSequences) {
        	ret.add(msa.getAlignedSequence(new DNASequence(seq, AmbiguityDNACompoundSet.getDNACompoundSet())).getSequenceAsString());
        }
        
        System.gc(); // Runs the garbage collector.
        
        return ret;
	}
	
	/*public static void testReadFastaDNASequence() {
		try {
			URL fastaUrl = new URL(String.format("https://www.ncbi.nlm.nih.gov/search/api/download-sequence/?db=nuccore&id=%s", "AX109998.1"));
			LinkedHashMap<String, DNASequence> fasta = FastaReaderHelper.readFastaDNASequence(fastaUrl.openStream());
			DNASequence seq = fasta.get("AX109998.1");
			System.out.println(seq);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}*/
	
	/*public static void testReadFastaProteinSequence() {
		try {
			URL uniprotFasta = new URL(String.format("http://www.uniprot.org/uniprot/%s.fasta", "Q7WUX0"));
			LinkedHashMap<String, ProteinSequence> fasta = FastaReaderHelper.readFastaProteinSequence(uniprotFasta.openStream());
	        ProteinSequence seq = fasta.get("Q7WUX0");
	        System.out.println(seq);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}*/
	
}
