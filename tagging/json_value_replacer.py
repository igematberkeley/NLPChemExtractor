# elsevier extractions return a dictionary with values as another dictionary. 
# We're only interested in the key 'originalText'

import json
import sys
import igemutils as igem

input_path = sys.argv[1]

def replace_dict_with_text(json_path):
    json_file = igem.get_json(json_path)
    # print(json_path)
    # print(json_file.items())
    for doi, paper_dict in json_file.items():
        originalText = paper_dict['originalText']
        json_file.update({doi : originalText})

    igem.save_json(input_path, json_file)

replace_dict_with_text(input_path)
