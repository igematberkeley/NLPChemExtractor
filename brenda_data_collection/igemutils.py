# A bunch of functions that are useful.

import xml.etree.ElementTree as ET
import requests
import urllib
import os
import json
import pandas as pd

''' Uses EUtils to ocnvert PMID (str, no decimals) to DOI.
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

def get_metadata(doi: str, email: str):
    url = f'https://api.unpaywall.org/v2/{doi}?email={email}'
    
    out_dict = requests.get(url).json()
    
    if 'error' in out_dict:
        return False
    else:
        return out_dict
    
def save_json(json_file, contents):
    with open(json_file, 'w') as fp:
        json.dump(contents, fp)
        fp.close()

def get_json(json_file):
    if os.path.isfile(json_file):
        with open(json_file, 'r') as fp:
            out_dict = json.load(fp)
            fp.close()
    else:
        out_dict = {}
        
    return out_dict