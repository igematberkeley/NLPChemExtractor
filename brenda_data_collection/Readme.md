# Data Collection Pipeline
## Stage 1: Obtaining reactions from BRENDA.
```generate_brenda_reactions.ipynb``` - with the BRENDA API, fetch all EC Numbers for each one, fetch their reference, substrates, and products. Then each reference is matched with a PMID if it exists. This is all outputted into ```brenda_rxns_incomplete.csv```. This csv is the basis of the rest of the pipeline. 

```get_doi.ipynb``` - mainly produces the ```refDict``` subset that contains all the reactions with invalid or absent PMID's. It also has a draft of a function that queries doi's from PMID's.
## Stage 2: Getting metadata for each reaction 
### Non PMID Set: ```get_metadata_nonpmidset.ipynb```:
From ```refDict```, query for its DOI with the limited metadata at hand (title, journal, authors, etc). It then uses the Unpaywall API to fetch each paper's metadata (with DOI if available, or query with title otherwise). The output is ```query_out.json``` {DOI: metadata}.
### PMID Set: ```get_metadata_pmidset.ipynb```:
From ```brenda_rxns_incomplete.csv```, filter to only the refs with PMID's, then get their DOI (with EUtilities), and query Unpaywall to get its metadata. Output is ```unpaywall_metadata_pmids.json``` {PMID: metadata}
## Stage 3: Fetching the papers.

## Misc
- ```pmid_paper_collection.ipynb``` - just looking at the "Open Access Subset" on PubMed. 
- Some locations have changed due since removing Git LFS. Check the google drive if you can't find certain files.