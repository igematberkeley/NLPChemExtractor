package newChemInformatics;
package org.ucb.act.ro;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;

import java.io.IOException;
import java.util.HashMap;

import static java.lang.StrictMath.abs;

/** @author Cassandra Areff **/
/** some code pulled from jca20n and ArjunChandran **/

public class InChIUtils {

    public static double get_mass_difference(String substrate, String product) {
        Molecule sub = get_inchi_as_mol(substrate);
        Molecule pro = get_inchi_as_mol(product);
        return get_mass_difference(sub, pro);
    }

    public static double get_mass_difference(Molecule substrate, Molecule product) {
        double sub_mass = substrate.getExactMass();
        double pro_mass = product.getExactMass();
        return abs(sub_mass - pro_mass);
    }

    public static Molecule get_inchi_as_mol(String inchi) {
        try {
            Molecule mol = MoleImporter.importMol(inchi);
            return Molecule;
        } catch (Excpection e) {
            return null;
        }
    }

    public static String get_mol_as_inchi(Molecule mol) {
        try {
            return MoleExporter.exportToFormat(mol, "inchi:AuxNone,Woff");
        } catch (IOException e) {
            return null;
        }
    }

//    public static void main(String[] args) {
//        String inchi1 = "InChI=1S/C10H14O2/c11-9-7-1-6-2-8(9)5-10(12,3-6)4-7/h6-8,12H,1-5H2";
//        String inchi2 = "InChI=1S/C6H14N2O2/c7-4-2-1-3-5(8)6(9)10/h5H,1-4,7-8H2,(H,9,10)/t5-/m0/s1";
//        Double mass_diff = get_mass_difference(inchi1, inchi2);
//        System.out.print(mass_diff);
//    }
}
