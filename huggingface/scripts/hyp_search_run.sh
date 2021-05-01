#!/bin/bash
# Job name:
#SBATCH --job-name=hyp_search_4_30
#
# Account:
#SBATCH --account=fc_igemcomp
#
# Partition:
#SBATCH --partition=savio2_gpu
#
# Wall clock limit:
#SBATCH --time=00:30:00

# Number of tasks needed for use case (example):
#SBATCH --ntasks=8
#
# Processors per task:
#SBATCH --cpus-per-task=1
#Number of GPUs, this can be in the format of "gpu:[1-4]", or "gpu:K80:[1-4] with the type included
#SBATCH --gres=gpu:4

## Command(s) to run:
##### MM NOTE: this is the path to MY scratch env. adjust to yours
source activate ~/scratch_mm/scibert_scratch/
python main_hyp_search.py
