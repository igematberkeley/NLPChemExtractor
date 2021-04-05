package org.ucb.act.ROProjection;

import org.ucb.act.ROProjection.Reaction_Generator;

import java.util.HashMap;

/** @author ArjunChandran **/
public class ChemoInformaticsPipeline {
    public void main(String filepath) throws Exception{
        Reaction_Generator reac_gen = new Reaction_Generator();
        ReactionValidation reac_val = new ReactionValidation();
        ProjectionAnalysis proj_anl = new ProjectionAnalysis();
        HashMap data_reactions = reac_gen.main(filepath);


    }
}
