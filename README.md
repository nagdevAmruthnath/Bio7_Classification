# Bio7_Classification

### A repository for a Bio7 Graphical User Interface for supervised classification which can be extended easily.

#### Please note: To compile the Java project imported from GIT you have to fix the Java classpath (created on a different computer). 
#### Select the project, open the context menu and execute the 'Fix Project Classpath' action. This will automatically fix the classpath.

When you import the project from a local file location or *.zip file the classpath is automatically adjusted on import.

#### Open and compile the 'Main.java' class dynamically ('Compile Java' action main toolbar) with Bio7 >=3.1 to open the interface.

Follow Button 1-4 for a classification workflow which trains and classifies images with R (scripts
are available in the R directory) in a non-blocking job.
 
Enable/disable/add filters in the 'createStackFeatures' method.

Until now Multichannel images (e.g. RGB) and Grayscale images or stacks (8-bit, 16-bit, 32-bit) can be classified. It is also possible
to import images with an ImageJ macro (e.g. Landsat 8 images, see macro example!)

The 'ModelGui' graphical view interface can be modified or extended with the Eclipse WindowBuilder plugin (SWT) if installed in Bio7.

