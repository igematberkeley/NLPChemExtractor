package org.ucb.act.ROProjection;

import java.util.ArrayList;
import java.util.Arrays;

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.io.formats.name.nameexport.Chem;
import chemaxon.struc.Molecule;
import org.ucb.act.ro.Combination;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ucb.act.ro.ROProjecter;
import org.ucb.act.utils.ChemAxonUtils;

/** @author ArjunChandran **/

public class Molecule_Projector {
    public boolean MoleculeRxn(String RO, ArrayList<Molecule> reactents, ArrayList<Molecule> products) throws MolFormatException {
        Boolean result = false;
        Molecule[] formatted_reactents = new Molecule[reactents.size()];
        int i = 0;
        for (Molecule chemical: reactents){
            //chemical.aromatize();
            formatted_reactents[i] = chemical;
            i = i + 1;
        }
        int f = 0;
        for (Molecule r: formatted_reactents) {
            ChemAxonUtils.savePNGImage(r, "Formatted_reactent" + Integer.toString(f) + ".png");
            System.out.println("Formatted Smile is " +Integer.toString(f) + " " +  ChemAxonUtils.toSmiles(r));
            f = f + 1;
        }
        ChemAxonUtils.savePNGImage(MolImporter.importMol(RO), "RO.png");
        ROProjecter projector = new ROProjecter();
        try {
            Set<String> prds = projector.project(RO, formatted_reactents);
            Set<String> prods = new HashSet<String>();
            for(String t: prds){
                String molecule = ChemAxonUtils.toInchi(MolImporter.importMol(t));
                prods.add(molecule);
            }
            int j = 0;
            for (String molecule: prods){
                Molecule mol = MolImporter.importMol(molecule);
                String name = "Mol_ProjProducts" + Integer.toString(j) + ".png";
                j = j + 1;
                ChemAxonUtils.savePNGImage(mol, name);
            }
            if (prods.isEmpty()){
                result = false;
            } else {
                for (Molecule product: products){
                    String inchi = ChemAxonUtils.toInchi(product);
                    if (prods.contains(inchi)){
                        result = true;
                    }

                    if(!prods.contains(inchi)){
                        result = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.print(e);
            result = false;
        }
        return result;
    }
}
