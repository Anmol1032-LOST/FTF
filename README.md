# FTF

It Can be used to maintain download files or something where you need to place a particular type of file in a particular directory, like .png, .jpg in images/, .obj, .gltf in models/ etc.

# How to use
- Select the source directory where all files are present (e.g., C:\Users\91750\Downloads, or ./testing/from)
- Click `Edit Transfer logic` to define the logic like
    - .png --> C:\Users\91750\Pictures
    - .jpg --> C:\Users\91750\Pictures
    - .obj --> C:\Users\91750\3D Models
    - .gltf --> C:\Users\91750\3D Models
- Click Run 
- The matching files will be copied to respected location

# External Files
### data.txt
Contains locations of file as specified by `Transfer logic Editor`

Syntax:
```text
pattern1=path\to\dir1
pattern2=path\to\dir2
```

### shared.txt
Contains paths of files that have been copied. Delete this file to recopy all files.

Syntax:
```text
path\to\file1
path\to\file2
```

### LoadingImage.gif
Image to be shown while loading.