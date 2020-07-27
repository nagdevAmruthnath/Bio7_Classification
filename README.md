# Bio7_Classification

### A repository for a Bio7 Graphical User Interface for supervised classification which can be extended easily.

#### Please note: To compile the Java project imported from GIT you have to fix the Java classpath (created on a different computer). 
#### Select the project, open the context menu and execute the 'Fix Project Classpath' action. This will automatically fix the classpath.

When you import the project from a local file location or *.zip file the classpath is automatically adjusted on import.

#### Open and compile the 'Main.java' class dynamically ('Compile Java' action main toolbar) with Bio7 >=3.1 to open the interface.

Follow Button 1-4 for a classification workflow which trains and classifies images with R (scripts
are available in the R directory) in a non-blocking job.
 
Several features can be enabled in the default tab which will be added to the default image layers. A comma separated
text argument adds filter images of different radius or applies special settings for edge algorithms like Difference of Gaussian, Lipschitz, Gabor, Convolve. 
For some edge detection methods a ';' separator can be set, too, for different sets of edge settings (Difference of Gaussian, Lipschitz, Gabor, Convolve) resulting
in one image layer each.

All settings for classification can be stored or reopened with the 'Load/Save Configuration' actions in a simple text file.

In the Settings tab the path to the R and ImageJ macro scripts can be set if necessary (or easier simply change the default scripts).

Until now Multichannel images (e.g. RGB) and Grayscale images or stacks (8-bit, 16-bit, 32-bit) can be classified. It is also possible
to import images with an ImageJ macro (e.g. Landsat 8 images, see macro example!)

The 'ModelGui' graphical view interface can be modified or extended with the Eclipse WindowBuilder plugin (SWT) if installed in Bio7.

## Installation

With Bio7 3.1 first install the Java CV libraries available as an Eclipse Update Site:

https://bio7.github.io/javacv/

To make the libraries accessible for the dynamic Java compiler add the following libs to the
compiler classpath (you find them find them in the new installed Java CV plugin - see MacOSX screenshot below)

![image](libs.png)

In addition you have to recalculate the projects classpath (Context Menu->Fix Project Classpath).

