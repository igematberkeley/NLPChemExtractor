'''
########## Define these urself ##########
'''
email = 'ivalexander13@berkeley.edu' # enter ur email pls
io_dir = './get_fullpapers/oa/'
in_base = 'oa_subset_split'
out_base = 'oa_fullpapers_split'

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
import subprocess
import PyPDF2
import io

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
# Function: Get paper
def get_paper(pmid: str, metadata: dict) -> str:
    url = metadata['best_oa_location']['url_for_pdf']


    # Save to tmp.pdf
    try:
        # Fetch the paper
        response = requests.get(url)

        outtext = ''
        with io.BytesIO(response.content) as f:
            reader = PyPDF2.PdfFileReader(f)
            for page in reader.pages:
                outtext += page.extractText()
        f.close()
    except Exception:
        return False
    except (requests.exceptions.RequestException,
        ConnectionResetError):
        print("Connection Error")
        return False


    return outtext.replace('\n', ' ')


''' 
############## MAIN LOOP ###############
'''
# Output file = doi to full_text dict dictionary (in json)
def loop(subset, outfile):
    if os.path.isfile(outfile):
        fulltext_dict = igem.get_json(outfile)
    else:
        fulltext_dict = {}

    # hyperparam (-1 if max)
    max_calls = -1

    # Stats
    calls = 0
    fails = 0
    not_oa = 0
    successes_or_found = 0
    queries = 0

    lenn = len(subset)

    try:
        # Looping through quantify_dataset output json.
        for pmid, metadata in subset.items():

            # dont go over max calls. (-1 if infinite)
            if calls == max_calls or (queries == -1):
                print("[{num_run}] Query limit reached.", end='\r')
                break
            else:
                calls += 1

            # checks if paper has been successfully fetched before
            if pmid in fulltext_dict:
                successes_or_found += 1
                print(f"[{num_run}] ## Call {calls} found.", end=' #########\r')
                continue

            # THE FETCH
            fullpaper = get_paper(pmid, metadata)

            if (fullpaper):
                fulltext_dict[pmid] = fullpaper
                successes_or_found += 1
                queries += 1
                print(f"[{num_run}] Call {calls} success. {round(calls / lenn * 100, 2)}% done. {round(successes_or_found / (calls-not_oa) * 100, 2)}% successful.", end=' #########\r')
            else:
                fails += 1
                print(f"[{num_run}] Call {calls} failed. {round(calls / lenn * 100, 2)}% done. {round(successes_or_found / (calls-not_oa) * 100, 2)}% successful.", end=' #########\r')
            
            # if calls % 25 == 0:
            #     print(f'[{num_run}] ############# {successes_or_found} successes, {lenn - calls} left, {round(successes_or_found / (calls-not_oa) * 100, 2)}% successful.', end=' ####\r')
    except KeyboardInterrupt:
        pass


    # save to file
    igem.save_json(outfile, fulltext_dict)

    # Print Stats
    print("")
    print("###### STATS ######")
    print(f"Total calls: {calls}")
    print(f"Total number of queries: {queries}")
    print(f"Total number of Elsevier papers: {calls - not_oa}")
    print(f"Number of Non-Elsevier papers skipped: {not_oa}")
    print(f"Number of fetch failures: {fails}")
    print(f"Papers in storage: {len(fulltext_dict)}")
    print(f"% of success: {successes_or_found / (calls-not_oa) * 100}%")

''' RUN '''
loop(igem.get_json(in_file), out_file)