*  This package is a sidebar project to calculate reaction operators from Reaction

*  The goal of the package is to construct a first-pass set of reaction operators,
   curate them, and make them available as an oracle to other algorithms

***********************
Core Algorithmic Pieces:
***********************

SkeletonMapper and ChangeMapper
*   These are for generating Atom-To-Atom mappings of a reaction.  ChangeMapper just does the
    appropriate ChemAxon AutoMapper
*   SkeletonMapper first maps the carbon backbone, then the
    first shell of heteroatoms, then the rest of the molecule.

SubstructureMatcher
*   This is used by SkeletonMapper

OperatorExtractor
*   Calculates the various types of h*ERO and h*CRO's for a mapped reaction
*   ROProjector Example code for projecting the EROs generated in this project