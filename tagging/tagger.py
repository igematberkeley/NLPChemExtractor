

import pandas as pd
import json
import igemutils as igem

import time
import re
import collections

from chemdataextractor.doc import Document # referenced as CDE in some places
from chemdataextractor.nlp.pos import ChemCrfPosTagger


import os
import subprocess
import sys

import urllib
from urllib.request import urlopen
import socket
import pubchempy as pcp




### TAGGER CODE TO CREATE A CSV WITH A ROW FOR EACH SENTENCE. ###
# Input Data: .json with Key (String) doi_pmid/some form of id & Value (String) as full text or abstract
# Output Data: currently creates 1 CSV file "all.csv" with "sentence": sentence_found, "start": starts, 
# 				"end": ends, "indices": indices, "sentence_pos": tagged, "biological_entities": bio_entities, "chemical_entities": chemicals_found


# Set up with input path to load in JSON and prep a CSV to write to
input_path = sys.argv[1]
#out = sys.argv[2]
out_name = input_path.split("/")[-1].split(".")[0]

csv_file = 'output_ner/{}_{}.csv'.format("sentence_annotations", out_name) # can uncomment previous line, change stuff inside format to out to modify name of file

if not os.path.exists('output_ner'):
	os.makedirs('output_ner')

# read in json properly
if os.path.exists(input_path):
	text_files = igem.get_json(input_path)
else:
	raise Exception("supply correct input file")



#if there's a cache, reopen it:
smiles_cache = {} #keys are names, SMILES are values
cache_name = 'smiles_cache.json' # IF RUNNING IN PARALLEL: change to f"smiles_cache_{out_name}.json"

if os.path.exists(cache_name):
	smiles_cache = igem.get_json(cache_name)

#initiate tagger
cpt = ChemCrfPosTagger()

# tracking counts, for monitoring runs
count = 0 
t0 = time.time()
start = time.time()
successful_spans = 0

# annotate(doi_pmid, text) function: Appends to csv_file annotations from each sententence of a given literature text. 
# Input: doi_pmid (key of .json), text (value of .json)
# Output: None.
def annotate(doi_pmid, text):
	global count
	global t0
	
	t1 = time.time()
	if (count % 10 == 0):
		with open("{}.log".format(out_name), "w+") as f:
			f.write("{} out of {} completed".format(count,len(text_files.keys())))
			f.write("elapsed time: " + str(time.time() - start))

		igem.save_json(cache_name, smiles_cache)

	print()
	print("{} out of {} completed".format(count,len(text_files.keys()))) 
	print(t1 - t0)

	t0 = t1
	try:
		sentences = [p.sentences for p in Document.from_string(text.encode()) if hasattr(p, 'sentences')] # this has character-based indices
	except:
		sentences = [[]]
	sentence_found = []
	starts = []
	ends = []
	indices = []
	tagged = []
	chemicals_found = []
	bio_entities = []
	bio_entities_with_pos = []

	names_found = []
	smiles_found = []
	names_and_smiles = []

	sentences = sentences[0] # weird nesting from CDE, do not change
	tot = time.time()
	times = 0
	span_total = 0
	successful_spans = 0

	for i in range(len(sentences)): #TODO: change this to all sentences
		s = sentences[i]
		t_s_0 = time.time()


		# Part of Speech Tagger (used later for NLP)
		try:
			pos = (s.pos_tagged_tokens)
		except Exception as e:
			pos = cpt.tag(s.split())
		
		spans = s.cems # generating here for enzyme finding
		span_names = [c.text for c in spans]


		# Enzymes in sentence (using regex)
		# attempt to get full enzyme names:
		enzyme_names = []
		enzyme_names_locs = []
		for i_w in range(len(pos)):
		    word = pos[i_w][0]
		    for m in re.finditer(r'[a-zA-Z]+ase\b', word):
		        enzyme = m.group(0)
		        i_l = i_w
		        while i_l > 0:
		            prev_word = pos[i_l][0]
		            prev_pos = pos[i_l][1]
		            if prev_word in span_names:
		                enzyme = prev_word + " " + enzyme
		            elif prev_pos not in ":;{}|,./<>?!":
		                break
		            i_l -=1
		        enzyme_names.append(enzyme)
		        enzyme_names_locs.append((enzyme, i_l, i_w))

		spans_sent = []
		smiles_sent = []
		names_sent = []
		names_smiles_sent = []
		for r in range(len(spans)):
			span = spans[r]
			c = span.text

			# Tries to get smiles on entire string, then if it doesn't work, deals with the case where c is a conglomerate of chemicals seperated by spaces.
			name_smiles_tuples = get_smiles(s,c)
			print(name_smiles_tuples)
			print()

			# Ignore chemical if not found
			if not name_smiles_tuples or (len(name_smiles_tuples) == 1 and not name_smiles_tuples[0][0]):
				continue
			successful_spans += len(name_smiles_tuples)

			for name, smiles in name_smiles_tuples:
				span_dict = {"text": name,
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
				spans_sent.append(span_dict)
				names_sent.append(name)
				smiles_sent.append(smiles)
				names_smiles_sent.append((name, smiles))

		# Leave for loop and add entries for each sentence in a given literature to lists
		sentence_found.append(s.text)
		chemicals_found.append(spans_sent)
		names_found.append(",, ".join(names_sent)) # two commas and a space for redundancy, since IUPAC has commas
		smiles_found.append(",, ".join(smiles_sent))
		names_and_smiles.append(names_smiles_sent)

		starts.append(s.start)
		ends.append(s.end)
		indices.append(i)
		bio_entities.append(", ".join(enzyme_names))
		bio_entities_with_pos.append(enzyme_names_locs)
		tagged.append(pos)


		if len(spans) > 0:
			times += time.time() - t_s_0
			span_total += len(spans)
			#print(time.time()-t_s_0)
	
	# Create a dataframe with  annotations from a given literature.
	print()
	print("Average time per each span (one identified chemical entity): " + str(times/(span_total + 0.01)))
	t_an = time.time()
	print("Time for all sentences in text: " + str(t_an - tot))
	print("Successfully classified span percent in paper: " + str(successful_spans/(span_total + 0.01)))

	# put all lists into a dictionary and coerce to dataframe! good riddance
	annotations = {"sentence": sentence_found,
					"start": starts,
					"end": ends,
					"indices": indices,
					"sentence_pos": tagged,
					"enzymes": bio_entities,
					"enzyme_locations": bio_entities_with_pos,
					"chemical_entities_full": chemicals_found,
					"chemical_names": names_found,
					"chemical_smiles": smiles_found,
					"name_smile_tuples": names_and_smiles}
	annots_csv = pd.DataFrame(annotations)

	annots_csv["lit_id"] = doi_pmid

	# Reorder our dataframe.
	annots_csv = annots_csv[["lit_id", "indices", "start", "end", "sentence", "sentence_pos",
 						"enzymes", "enzyme_locations","chemical_entities_full",
 						"chemical_names", "chemical_smiles", "name_smile_tuples"]]

	# Add the datagram to our csv_file, appending if it exists and creating a new one if not.
	if os.path.isfile(csv_file):
		annots_csv.to_csv(csv_file, mode='a', header=False, index=False)
	else:
		annots_csv.to_csv(csv_file, index=False)

def make_you_smile(sent, c):
	global successful_spans
	global out_name
	global smiles_cache
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
	if c.lower() in smiles_cache:
		smiles = smiles_cache[c.lower()]
	else:
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
		except KeyboardInterrupt:
			igem.save_json(cache_name, smiles_cache)
			print()
			print("Restart from this position: " + str(pointer))
			raise
		except Exception as e:
			print("uh oh, some connection error :(")
			with open("output_ner/connection_errors_{}.txt".format(out_name), "w+") as fh:
				fh.write("URLLIB CONNECTION ERROR: " + str(sent))
			pass
		else:
			if req.getcode() == 200:
				print("It worked!")
				smiles = req.read().decode('utf8')
				smiles_cache[c.lower()] = smiles
			else:
				# Try pubchempy is this  doesn't work.
				try: 
					molecule = pcp.get_compounds(c, 'name')
					# Gets different IDs from the same compound name, that is why molecule[0]
					smiles = molecule[0].canonical_smiles
					print("PubChemPy worked!")
					smiles_cache[c.lower()] = smiles
					
				except:
					print(req.getcode())
					raise

	if smiles is None:
		smiles_cache[c.lower()] = None
		return (None, None)
	else:
		return c, smiles # :)

def chunk_filter(substr):
		if substr.isnumeric():
			return False
		elif len(substr) == 1 and substr.islower():
			return False
		elif len(substr) == 1 and substr not in "HBCNOFPSIWU": # a capital letter not in the substring
			return False
		return True

def get_smiles(sent, c):
	data = [make_you_smile(sent, c)]
	print(data)
	if (data == [None]):
		data = []
		if " " in c:
			chem_chunk = c.split(" ")
			print(chem_chunk)
			none_count = 0 
			for ch in chem_chunk:
				if none_count >=5:
					data = None # abort this chunk. 
					break 
				if chunk_filter(ch):
					data.append(make_you_smile(sent, ch))
				else:
					none_count+=1 
	return data

# Call annotate on every literature in a given .json and create a csv file. 

# order the items:
sorted_dictionary_by_pmid = collections.OrderedDict(sorted(text_files.items()))

try: 
	start = 0 ## CHANGE IF RESUMING!
	iter_dict = list(sorted_dictionary_by_pmid.items())
	for pointer in range(len(iter_dict))[start:]:
		entry = iter_dict[pointer]
		doi_pmid = entry[0]
		text = entry[1]
		try: 
			if text and isinstance(text, str):
				annotate(doi_pmid, text)
			count += 1
		except Exception as e:
			print()
			print("here's a paper error!: " + doi_pmid)
			with open("output_ner/connection_errors_{}.txt".format(out_name), "w+") as fh:
				fh.write("General paper error: " + str(doi_pmid))

			continue
except KeyboardInterrupt:
	igem.save_json(cache_name, smiles_cache)
	print("Restart from this position: " + str(pointer))

	pass
