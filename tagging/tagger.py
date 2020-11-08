from chemdataextractor.doc import Document
import pandas as pd
import json
from chemdataextractor.nlp.pos import ChemCrfPosTagger
import os
import subprocess
import sys
#import spacy
#import en_covido
from urllib.request import urlopen
import pubchempy as pcp
import time
import re
import igemutils as igem

def install(package):
	subprocess.check_call([sys.executable, "-m", "pip", "install", package])

try:
	import cirpy
except:
	install("cirpy")
	import cirpy



# input data:
## json of pmid/some form of id, full texts, or pmid/some form of id, abstracts
input_path = sys.argv[1]#
#out = sys.argv[2]


text_files = igem.get_json(input_path) #json.load(input_path)
#text_files = pd.read_csv(input_path)
cpt = ChemCrfPosTagger()

from urllib.request import urlopen

#bio_ner = en_covido.load()

count = 0 
csv_file = 'output_ner/{}.csv'.format("all")
t0 = time.time()


def annotate(pmid, text):
	global count
	global t0

	
	t1 = time.time()
	print("{} out of {} completed".format(count,len(text_files.keys()))) 
	print(t1 - t0)
	t0 = t1
	try:
		sentences = [p.sentences for p in Document.from_string(text.encode())] # this has character-based indices
	except:
		print(text)
		raise
	s_found = []
	starts = []
	ends = []
	indices = []
	tagged = []
	d_found = []
	bio_entities = []
	sentences = sentences[0]
	t_s_0 = time.time()
	for i in range(len(sentences)):
		s = sentences[i]
		
		#doc = bio_ner(str(s))
		# find regex for enzymes: 
		bio_doc = [(m.group(0), m.start(0), m.end(0)) for m in re.finditer(r'[a-zA-Z]+ase\b', str(s))]


		# bio_doc = [(ent.label_, ent.text) for ent in doc.ents]
		# make sure to save
		
		try:
			pos = (s.pos_tagged_tokens)
		except Exception as e:
			pos = cpt.tag(s.split())
		
		p = 0 # for indexing through pos tokens
		
		spans = s.cems
		spans_list = []
		for r in range(len(spans)):
			span = spans[r]
			c = span.text
			t_sp_0 = time.time()
			# cleaning of chemical string: remove newlines -- too hard to remove dashes paired with new-lines..
			
			c = c.rstrip()

			# From SMILES.ipynb
			molecule = None
			print()
			if " " not in c:
				try:
					url_nih = 'http://cactus.nci.nih.gov/chemical/structure/' + c + '/smiles'
					molecule = urlopen(url_nih).read().decode('utf8')
					print(molecule)
				except Exception as e:
					print("oof" + c)
					continue

			#ignore if not found
			if not molecule:
				continue

			smiles = molecule

			span_dict = {"text": c,
						"start": span.start,
						"end": span.end,
						"smiles": smiles 
			}

			while p < len(pos):
				token = pos[p][0]
				if token == span.text:
					span_dict["pos"] = pos[p][1]
					break
				p += 1
			spans_list.append(span_dict)
			print(time.time()-t_sp_0)
			print("sp")

		s_found.append(s.text)
		d_found.append(spans_list)
		starts.append(s.start)
		ends.append(s.end)
		indices.append(i)
		bio_entities.append(bio_doc)
		tagged.append(pos)
		print(time.time()-t_s_0)
		print("len of spans:" + str(len(spans)))
	t_an = time.time()
	annotations = {"sentence": s_found,
					"start": starts,
					"end": ends,
					"indices": indices,
					"sentence_pos": tagged,
					"biological_entities": bio_entities,
					"chemical_entities": d_found}
	annots_csv = pd.DataFrame(annotations)

	annots_csv["lit_id"] = pmid
# reorder
	annots_csv = annots_csv[["lit_id", "indices", "start", "end", "sentence", "sentence_pos",
 						"chemical_entities", "biological_entities"]]
# so that i don't have to store in memory!
	if os.path.isfile(csv_file):
		annots_csv.to_csv(csv_file, mode='a', header=False, index=False)
	else:
		annots_csv.to_csv(csv_file, index=False)
	print(time.time() - t_an)


# as dictionary:

for pmid, paper_dict in text_files.items():
	annotate(pmid, paper_dict)
	count += 1
	break
