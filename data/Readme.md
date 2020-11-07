# Data Directory (Under Construction)
 First of all, this directory will always be empty in Github (and when you first clone). This directory is meant to be synchronised (loosely) to the shared Google Drive [here](https://drive.google.com/drive/folders/1N2VIioBzkeIQqj5qKLi7z9XN1n4_2W3E?usp=sharing).
# Instructions to Sync to Drive:
## Option 1: Download manually from Google Drive
The file structure of the drive and this folder is the same. So 
- GDrive: `igem_nlp_data/` 
    -  `fullpapers/`
    - `metadata/`
    -  some `csv`'s.
- This: `data/` 
    - `fullpapers/` 
    - `metadata/`
    - some `csv`'s.

## Option 2: Rclone Copy 
Set up with [these instructions](https://rclone.org/drive/).
```
rclone config
```

When you wanna copy stuff, do:
```
rclone copy <remote_name>:<path/to/dir/or/file> <path/to/local/>
```
This acts similarly to `cp <source> <target>`