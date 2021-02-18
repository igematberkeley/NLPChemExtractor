/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ucb.act.composition.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author J. Christopher Anderson
 */
public class InstantiateGFPReporter {
    public static void main(String[] args) {
        //Instantiate the two Parts
        Part amilGFP = new Part(PartType.Protein, "amilGFP", "MSYSKHGIVQEMKTKYHMEGSVNGHEFTIEGVGTGYPYEGKQMSELVIIKPAGKPLPFSFDILSSVFQYGNRCFTKYPADMPDYFKQAFPDGMSYERSFLFEDGAVATASWNIRLEGNCFIHKSIFHGVNFPADGPVMKKKTIDWDKSFEKMTVSKEVLRGDVTMFLMLEGGGSHRCQFHSTYKTEKPVTLPPNHVVEHQIVRTDLGQSAKGFTVKLEAHAAAHVNPLKVK");
        Part Pbad = new Part(PartType.DNA, "Pbad", "TTATGACAACTTGACGGCTACATCATTCACTTTTTCTTCACAACCGGCACGGAACTCGCTCGGGCTGGCCCCGGTGCATTTTTTAAATACCCGCGAGAAATAGAGTTGATCGTCAAAACCAACATTGCGACCGACGGTGGCGATAGGCATCCGGGTGGTGCTCAAAAGCAGCTTCGCCTGGCTGATACGTTGGTCCTCGCGCCAGCTTAAGACGCTAATCCCTAACTGCTGGCGGAAAAGATGTGACAGACGCGACGGCGACAAGCAAACATGCTGTGCGACGCTGGCGATATCAAAATTGCTGTCTGCCAGGTGATCGCTGATGTACTGACAAGCCTCGCGTACCCGATTATCCATCGGTGGATGGAGCGACTCGTTAATCGCTTCCATGCGCCGCAGTAACAACTGCTCAAGCAGATTTATCGCCAGCAGCTCCGAATAGCGCCCTTCCCCTTGCCCGGCGTTAATGATTTGCCCAAACAGGTCGCTGAAATGCGGCTGGTGCGCTTCATCCGGGCGAAAGAACCCCGTATTGGCAAATATTGACGGCCAGTTAAGCCATTCATGCCAGTAGGCGCGCGGACGAAAGTAAACCCACTGGTGATACCATTCGCGAGCCTCCGGATGACGACCGTAGTGATGAATCTCTCCTGGCGGGAACAGCAAAATATCACCCGGTCGGCAAACAAATTCTCGTCCCTGATTTTTCACCACCCCCTGACCGCGAATGGTGAGATTGAGAATATAACCTTTCATtcccagcggtcggtcgataaaaaaatcgagataaccgttggcctcaatcggcgttaaacccgccaccagatgggcattaaacgagtatcccggcagcaggggatcattttgcgcttcagccatacttttcatactcccgccattcagagaagaaacGaattgtccatattgcatcagacattgccgtcactgcgtcttttactggctcttctcgctaaccaaaccggtaaccccgcttattaaaagcattctgtaacaaagcgggaccaaagccatgacaaaaacgcgtaacaaaagtgtctataatcacggcagaaaagtccacattgattatttgcacggcgtcacactttgctatgccatagcatttttatccataagattagcggattctacctgacgctttttatcgcaactctctactgtttctccatA");
        
        //Create and fill the parts list
        List<Part> parts = new ArrayList<>();
        parts.add(Pbad);
        parts.add(amilGFP);
        
        //Instantiate the Composition
        Composition comp = new Composition("Pbad-amil1", parts);
        
        //Print its contents
        System.out.println(comp.getName());
        for(Part apart : comp.getParts()) {
            System.out.println(apart.getName() + " : " + apart.getSequence());
        }
    }
}

