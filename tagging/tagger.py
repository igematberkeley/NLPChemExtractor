from chemdataextractor.doc import Document
import pandas as pd
import json
from chemdataextractor.nlp.pos import ChemCrfPosTagger
import pubchempy as pcp
import os
import subprocess
import sys

import urllib
from urllib.request import urlopen
import socket

import time
import re
import igemutils as igem

### TAGGER CODE TO CREATE A CSV WITH A ROW FOR EACH SENTENCE. ###
# Input Data: .json with Key (String) doi_pmid/some form of id & Value (String) as full text or abstract
# Output Data: currently creates 1 CSV file "all.csv" with "sentence": sentence_found, "start": starts, 
# 				"end": ends, "indices": indices, "sentence_pos": tagged, "biological_entities": bio_entities, "chemical_entities": chemicals_found


# Install cirpy, Python interface for the Chemical Identifier Resolver (CIR). (https://cirpy.readthedocs.io/en/latest/)
def install(package):
	subprocess.check_call([sys.executable, "-m", "pip", "install", package])

try:
	import cirpy
except:
	install("cirpy")
	import cirpy

# Set up with input path to load in JSON and prep a CSV to write to
input_path = sys.argv[1]
#out = sys.argv[2]

text_files = igem.get_json(input_path)
cpt = ChemCrfPosTagger()

from urllib.request import urlopen

#bio_ner = en_covido.load()

count = 0 
csv_file = 'output_ner/{}.csv'.format("all")
t0 = time.time()
successful_spans = 0

# annotate(doi_pmid, text) function: Appends to csv_file annotations from each sententence of a given literature text. 
# Input: doi_pmid (key of .json), text (value of .json)
# Output: None.
def annotate(doi_pmid, text):
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
	sentence_found = []
	starts = []
	ends = []
	indices = []
	tagged = []
	chemicals_found = []
	bio_entities = []
	sentences = sentences[0]
	tot = time.time()
	times = 0
	span_total = 0
	successful_spans = 0

	for i in range(len(sentences))[:40]:
		s = sentences[i]
		t_s_0 = time.time()
		
		# Enzymes in sentence (using regex)
		bio_doc = [(m.group(0), m.start(0), m.end(0)) for m in re.finditer(r'[a-zA-Z]+ase\b', str(s))]

		# bio_doc = [(ent.label_, ent.text) for ent in doc.ents]
		# make sure to save

		# Part of Speech Tagger (used later for NLP)
		try:
			pos = (s.pos_tagged_tokens)
		except Exception as e:
			pos = cpt.tag(s.split())
		
		spans = s.cems
		spans_list = []
		for r in range(len(spans)):
			span = spans[r]
			c = span.text

			# Tries to get smiles on entire string, then if it doesn't work, deals with the case where c is a conglomerate of chemicals seperated by spaces.
			smiles = [make_you_smile(c)]
			print(smiles)
			if (smiles == [None]):
				if " " in c:
					chem_chunk = c.split(" ")
					print(chem_chunk)
					smiles = [make_you_smile(x) for x in chem_chunk if not x.isnumeric()]
					# also screen to remove lowercase letters
					# if more then threshold are None, set all smiles = None. 

			# Ignore chemical if not found
			if not smiles:
				continue

			span_dict = {"text": c,
						"start": span.start,
						"end": span.end,
						"smiles": smiles
			}

			# Indexing through pos tokens to find chemical entities
			p = 0
			while p < len(pos):
				token = pos[p][0]
				if token == span.text:
					span_dict["pos"] = pos[p][1]
					break
				p += 1
			spans_list.append(span_dict)

		# Leave for loop and add entries for each sentence in a given literature to lists
		sentence_found.append(s.text)
		chemicals_found.append(spans_list)
		# raw_smiles_found.append() - list of raw smiles
		# smiles_chem_maps.append() - map between chemical name and smiles
		starts.append(s.start)
		ends.append(s.end)
		indices.append(i)
		bio_entities.append(bio_doc)
		tagged.append(pos)
		if len(spans) > 0:
			times += time.time() - t_s_0
			span_total += len(spans)
			#print(time.time()-t_s_0)
	
	# Create a dataframe with  annotations from a given literature.
	print("Avg")
	print("Time per each span (one identified chemical entity): " + str(times/(span_total + 0.01)))
	t_an = time.time()
	print("Time for all sentences in text: " + str(t_an - tot))
	print("Successfully classified spans in paper: " + str(successful_spans/(span_total + 0.01)))
	annotations = {"sentence": sentence_found,
					"start": starts,
					"end": ends,
					"indices": indices,
					"sentence_pos": tagged,
					"biological_entities": bio_entities,
					"chemical_entities": chemicals_found}
	annots_csv = pd.DataFrame(annotations)

	annots_csv["lit_id"] = doi_pmid

	# Reorder our dataframe.
	annots_csv = annots_csv[["lit_id", "indices", "start", "end", "sentence", "sentence_pos",
 						"chemical_entities", "biological_entities"]]

	# Add the datagram to our csv_file, appending if it exists and creating a new one if not.
	if os.path.isfile(csv_file):
		annots_csv.to_csv(csv_file, mode='a', header=False, index=False)
	else:
		annots_csv.to_csv(csv_file, index=False)

def make_you_smile(c):
	global successful_spans
	smiles = None  # :(
	
	# Cleaning of chemical string.
	c = c.rstrip()
	c = c.replace(" ", "%20")
	c = c.replace("\u03b1", "alpha")
	c = c.replace("\u03b2", "beta")
	c = c.replace("\u03b3", "gamma")
	c = c.replace("\u2032", "")
	for dumb_quote in ["\u0060", "\u00B4", "\u2018", "\u2019", "\u201C", "\u201D", "\u00f7", "\u2423", "\u2d19", "\ufb02"]: # quotes and divide
		c.replace(dumb_quote, "")
	for dumb_dash in ["\u007E", "\u2010", "\u2011", "\u2012", "\u2013", "\u2014", "\u2015"]:
		c.replace(dumb_dash, "\u002d")
	
	print(c)
	url_nih = 'http://cactus.nci.nih.gov/chemical/structure/' + c + '/smiles'

	try:	
		print(url_nih)
		req = urlopen(url_nih, timeout = 3)			
	except urllib.error.HTTPError as e:
		print("No entity returned.")
	except socket.timeout as t:
		print("Taking too long, likely an invalid entity.")
		print(c)
	except UnicodeEncodeError as e:
		print("Unicode Encode Error: " + str(e))
	else:
		if req.getcode() == 200:
			print("It worked!")
			smiles = req.read().decode('utf8')
			successful_spans += 1
		else:
			# Try pubchempy is this  doesn't work.
			try: 
				molecule = pcp.get_compounds(c, 'name')
				# Gets different IDs from the same compound name, that is why molecule[0]
				smiles = molecule[0].canonical_smiles
				print("PubChemPy worked!")
				successful_spans += 1
			except:
				print(req.getcode())
				raise
	return smiles  # :) 


# Call annotate on every literature in a given .json and create a csv file. 
for doi_pmid, text in text_files.items():
	annotate(doi_pmid, text)
	count += 1
	if count > 15:
		break
