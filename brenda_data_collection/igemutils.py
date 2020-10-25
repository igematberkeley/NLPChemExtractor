# A bunch of functions that are useful.

import xml.etree.ElementTree as ET
import requests
import urllib
import os
import json
import pandas as pd
import numpy as np

''' 
Uses EUtils to ocnvert PMID (str, no decimals) to DOI.
'''
def pmid2doi(pmid: str):
    url = f'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id={pmid}'
    
    try:
        r = requests.get(url)
        root = ET.fromstring(r.text.encode('utf-8'))
    
    
        doi = root.findall('./DocSum/Item[@Name="DOI"]')
        doi = doi[0].text

    except:
        return False
    
    if doi == "":
        return false
    
    return doi

'''
Uses a DOI and fetches its Unpaywall metadata; returns a dictionary representation of this metadata.
'''
def get_metadata(doi: str, email: str):
    url = f'https://api.unpaywall.org/v2/{doi}?email={email}'
    
    out_dict = requests.get(url).json()
    
    if 'error' in out_dict:
        return False
    else:
        return out_dict
    
'''
Handy dandy JSON functions
'''
def save_json(json_file: str, contents: dict):
    with open(json_file, 'w') as fp:
        json.dump(contents, fp)
        fp.close()

def get_json(json_file: str):
    if os.path.isfile(json_file):
        with open(json_file, 'r') as fp:
            out_dict = json.load(fp)
            fp.close()
    else:
        out_dict = {}
        
    return out_dict

'''
Creates a subset of the dict based on a criteria.
 @Metadata_dict: key=pmid value=dict-containing-field
 @Returns a tuple (subset_matched, subset_not_matched). 
    If return_complement=True, then the subset_not_matched will be populated, else it's an empty dict.
'''
def generate_subset(metadata_dict: dict, field: str, values: list, return_complement=False):
    subset_dict = {}
    complement_dict = {}
    count_match = 0
    count_nomatch = 0
    
    for value in values:
        for pmid, meta in metadata_dict.items():
            field_value = meta[field] == value
            if field_value:
                subset_dict[pmid] = meta
                count_match += 1
                continue;
            count_nomatch +=1
            if return_complement:
                complement_dict[pmid] = meta

    print(f"Count of {field} being {values}: {count_match}.")
    if return_complement:
        print(f"Count of {field} not matching {values}: {count_nomatch}.")
    
    return subset_dict, complement_dict

'''
Splits input_dict into (almost) even json files. 
@base_outfile -> filename, excluding the part number and filetype. (E.g. '/path/to/dir/mysubset' --> will create mysubset1.json, mysubset2.json, etc in /path/to/dir/)
'''
def split_dict(input_dict: dict, n: int, base_ouftile: str):
    # array of indices at which it switches to new file
    lenn = len(input_dict)
    min_len_per_file = lenn // n
    ending_indices = np.sort(lenn - np.arange(0 , n) * min_len_per_file)
    ending_indices = ending_indices.tolist()
    
    # generate filenames
    outfiles = [f'{base_ouftile}{i}.json' for i in range(1, n+1)]
    
    temp_outdict = {}
    rep = 0
    for key, value in input_dict.items():
        if rep >= ending_indices[0] or rep == lenn-1:
            save_json(outfiles.pop(0), temp_outdict)
            temp_outdict = {}
            print(f"Saved part {n - len(ending_indices) + 1}.")
            ending_indices.pop(0)

        rep += 1
        temp_outdict[key] = value    
    print("All done!")