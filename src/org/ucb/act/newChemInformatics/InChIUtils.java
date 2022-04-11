package newChemInformatics;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.jep.function.In;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;

import java.io.IOException;
import java.util.HashMap;

import static java.lang.StrictMath.abs;

/** @author Cassandra Areff **/
/** some code pulled from jca20n and ArjunChandran **/

public class InChIUtils {

    /** gets the mass difference between two inchis **/
    public static Integer get_mass_difference(String substrate, String product) {
        Molecule sub = get_inchi_as_mol(substrate);
        Molecule pro = get_inchi_as_mol(product);
        return get_mass_difference(sub, pro);
    }

    /** gets the mass difference between two molecules **/
    public static Integer get_mass_difference(Molecule substrate, Molecule product) {
        double sub_mass = substrate.getExactMass();
        double pro_mass = product.getExactMass();
        Double difference = abs(sub_mass - pro_mass) * 1000;
        Integer difference_rounded = difference.intValue();
        return difference_rounded;
    }

    /** gets the mass difference between many molecules **/
    public static Integer get_mass_difference(Molecule[] substrates, Molecule[] products) {
        double sub_mass = 0;
        double pro_mass = 0;
        for (Molecule sub:substrates) {
            sub_mass += sub.getExactMass();
        }
        for (Molecule pro:products) {
            sub_mass += pro.getExactMass();
        }
        Double difference = abs(sub_mass - pro_mass) * 1000;
        Integer difference_rounded = difference.intValue();
        return difference_rounded;
    }

    /** gets the inchi as a molecule **/
    public static Molecule get_inchi_as_mol(String inchi) {
        try {
            Molecule mol = MolImporter.importMol(inchi);
            return mol;
        } catch (Exception e) {
            return null;
        }
    }

    /** gets the molecule as an inchi **/
    public static String get_mol_as_inchi(Molecule mol) {
        try {
            return MolExporter.exportToFormat(mol, "inchi:AuxNone,Woff");
        } catch (IOException e) {
            return null;
        }
    }

    /** tests most of the functions **/
    public static void main(String[] args) {
        String inchi1 = "InChI=1S/C10H14O2/c11-9-7-1-6-2-8(9)5-10(12,3-6)4-7/h6-8,12H,1-5H2";
        String inchi2 = "InChI=1S/C6H14N2O2/c7-4-2-1-3-5(8)6(9)10/h5H,1-4,7-8H2,(H,9,10)/t5-/m0/s1";
        Integer mass_diff = get_mass_difference(inchi1, inchi2);
        System.out.print(mass_diff);
    }
}
