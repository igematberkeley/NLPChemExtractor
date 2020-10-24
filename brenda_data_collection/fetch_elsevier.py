'''
########## Define these urself ##########
'''
email = 'ivalexander13@berkeley.edu' # enter ur email pls
io_dir = './get_fullpapers/elsevier/'
in_base = 'elsevier_subset_split'
out_base = 'elsevier_fullpapers_split'

''' 
######### Imports #########
'''
import urllib
import requests
import os
import math
import json
import pandas as pd
import xml.etree.ElementTree as ET
import numpy as np
import sys

from elsapy.elsclient import ElsClient
from elsapy.elsprofile import ElsAuthor, ElsAffil
from elsapy.elsdoc import FullDoc, AbsDoc
from elsapy.elssearch import ElsSearch

import igemutils as igem

'''
############ Args #############
'''
num_run = sys.argv[1]
in_file = f'{io_dir}{in_base}{num_run}.json'
out_file = f'{io_dir}{out_base}{num_run}.json'


'''
############ Setup ############
'''
# Load configuration
con_file = open("elsevier_api/config.json")
config = json.load(con_file)
con_file.close()

# Initialize client
client = ElsClient(config['apikey'])
client.local_dir = "./get_fullpapers_pmidset/"

# Function: Get paper
def get_paper(doi: str):
    ## ScienceDirect (full-text) document example using DOI
    doi_doc = FullDoc(doi = doi)
    if doi_doc.read(client):
        return doi_doc.data
    else:
        return False

''' 
############## MAIN LOOP ###############
'''
# Output file = doi to full_text dict dictionary (in json)
def loop_elsevier(elsevier_subset, outfile):
    json_file = outfile
    if os.path.isfile(json_file):
        with open(json_file, 'r') as fp:
            elsevier_fulltexts = json.load(fp)
            fp.close()
    else:
        elsevier_fulltexts = {}

    # hyperparam (-1 if max)
    max_calls = -1

    # Stats
    calls = 0
    fails = 0
    not_elsevier = 0
    successes_or_found = 0
    queries = 0

    lenn = len(elsevier_subset)

    try:
        # Looping through quantify_dataset output json.
        for pmid, metadata in elsevier_subset.items():
            doi = metadata['doi']
            doi_url = f"https://doi.org/{doi}"

            # dont go over max calls. (-1 if infinite)
            if calls == max_calls or (queries == -1):
                print("[{num_run}] Query limit reached.")
                break
            else:
                calls += 1

            # checks if paper has been successfully fetched before
            if pmid in elsevier_fulltexts:
                successes_or_found += 1
                print(f"[{num_run}] ## Call {calls} found.")
                continue

            fullpaper = get_paper(doi)
            if (fullpaper):
                elsevier_fulltexts[pmid] = fullpaper
                successes_or_found += 1
                queries += 1
                print(f"[{num_run}] Call {calls} success. {calls / lenn * 100}% done.")
            else:
                fails += 1
                print (f"[{num_run}] #### Call {calls} failed: {fails}. DOI: {doi_url}.")
            
            if calls % 25 == 0:
                print(f'[{num_run}] ############# {successes_or_found} successes, {lenn - calls}left.')

    except KeyboardInterrupt:
        pass

    # save to file
    with open(json_file, 'w') as fp:
        json.dump(elsevier_fulltexts, fp)
        # vary: alter frequency of file save
        fp.close()

    # Print Stats
    print("")
    print("###### STATS ######")
    print(f"Total calls: {calls}")
    print(f"Total number of queries: {queries}")
    print(f"Total number of Elsevier papers: {calls - not_elsevier}")
    print(f"Number of Non-Elsevier papers skipped: {not_elsevier}")
    print(f"Number of fetch failures: {fails}")
    print(f"Papers in storage: {len(elsevier_fulltexts)}")
    print(f"% of success: {successes_or_found / (calls-not_elsevier) * 100}%")

''' RUN '''
loop_elsevier(igem.get_json(in_file), out_file)