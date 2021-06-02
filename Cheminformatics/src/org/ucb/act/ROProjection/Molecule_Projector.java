package org.ucb.act.ROProjection;

import java.util.ArrayList;
import java.util.Arrays;

import chemaxon.formats.MolImporter;
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
    public boolean MoleculeRxn(String RO, ArrayList<Molecule> reactents, ArrayList<Molecule> products){
        Boolean result = false;
        Molecule[] formatted_reactents = new Molecule[reactents.size()];
        int i = 0;
        for (Molecule chemical: reactents){
            formatted_reactents[i] = chemical;
            i = i + 1;
        }

        ROProjecter projector = new ROProjecter();
        try {
            Set<String> prds = projector.project(RO, formatted_reactents);
            Set<String> prods = new HashSet<String>();
            for(String t: prds){
                String molecule = ChemAxonUtils.toSMARTS(MolImporter.importMol(t));
                prods.add(molecule);
            }
            if (prods.isEmpty()){
                result = false;
            } else {
                for (Molecule product: products){
                    String smart = ChemAxonUtils.toSMARTS(product);
                    if (prods.contains(smart)){
                        result = true;
                    }

                    if(!prods.contains(smart)){
                        result = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }
}
