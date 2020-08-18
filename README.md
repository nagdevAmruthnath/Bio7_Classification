# Bio7_Classification

### A repository for a Bio7 Graphical User Interface for supervised classification which can be extended easily.

## Installation

With Bio7 3.1 first install the Java CV libraries available as an Eclipse Update Site:

https://bio7.github.io/javacv/

Also install the latest ImageJ update with the following Update Site:

https://bio7.github.io/imagej/

To make the libraries accessible for the dynamic Java compiler add the following libs to the
compiler classpath (you find them find them in the installed Java CV plugin - see MacOSX screenshot below)

![image](libs.png)

In addition you have to recalculate the projects classpath (the path to the libraries on your OS). Select the project folder, open the context menu and execute
the 'Fix Project Classpath' action. This will automatically set the correct classpath of the Bio7 (Eclipse) Java JDT project.

Note: When you import the project from a local file location or *.zip file the classpath is automatically adjusted on import.

## Usage

#### Video:

[![Alt text](https://img.youtube.com/vi/3PUxGm6zJAk/0.jpg)](https://www.youtube.com/watch?v=3PUxGm6zJAk)

#### Documentation:

Open and compile the 'Main.java' class dynamically (use the 'Compile Java' action in the Bio7 main toolbar).

##### Features Tab:

Follow Button 1-4 for a classification workflow which trains and classifies images with R (scripts
are available in the R directory) in a non-blocking job.

RGB images can be converted to a different color space (see option HSB Stack, LAB Stack) and selected channels from multichannel images
or stacks can be extracted individually.

Several features can be enabled in the default tab which will be added to the default image layers. 
A comma separated text argument adds filter images of different radius or applies special settings for edge algorithms like Difference of Gaussian, Lipschitz, Gabor, Convolve. 
For some edge detection methods a ';' separator can be set, too, for different sets of edge settings (Difference of Gaussian, Lipschitz, Gabor, Convolve) resulting
in one image layer each.
 
All settings for classification can be stored or reopened with the 'Load/Save Configuration' actions in a simple text file (simply drag the file on the GUI interface to load it!).

##### Settings Tab:

In the Settings tab the path to the R (training and classification) and the ImageJ import macro scripts can be set if necessary (or easier simply change the default scripts).
It is also possible to enable a directory dialog (see option 'Use Directory Dialog') for the classification (step 4) of images in a folder and it's subfolders (lists the image files recursively) 
instead of using selected images with the file dialog (for multiple files).

In addition the data transfer type to R can be selected if a more memory efficient transfer is needed or possible (this action will set the transfer type of images, too).

##### Supported Images:

Until now Multichannel images (e.g. RGB) and Grayscale images or stacks (8-bit, 16-bit, 32-bit) can be classified. It is also possible
to import images with an ImageJ macro (e.g. Landsat 8 images, see ImageJ macro example!).

##### Note:

For convenience images and stored ROI Manager files can be dropped on the ImageJ-Canvas view to open them. In addition a saved R workspace file can be opened, too, by
dropping it on the main toolbar of Bio7. Finally a stored GUI configuration file can be opened by dropping it directly on the classification GUI.

## RAM Usage Settings for Big Images
 
#### Rserve:

If I want to transfer a huge image to R the following warning message about buffer size occurs: "WARNING: discarding buffer because too big awaiting.."

Solution: Increase the size of the input buffer in the Rserve preferences. Open the Rserve preferences Preferences->Preferences Bio7->Preferences R.
In the Field "Rserve startup arguments" copy the following startup argument:

maxinbuf="4194304"

The max. packet size from the client to Rserve is set to 4GB in this example (maxinbuf argument in kb). 
Use the example size only if necessary and if you have the memory available!

#### Java:

The Java runtime is able to optimize memory on demand and return occupied memory to the OS. After the transfer to R the image data on the Java
side in the classification process (open and transfer to R) by default is closed. If the memory settings of Java in Bio7 (see Bio7.ini file) are adjusted accordingly most
of the memory can be returned to the OS and thus to R and the memory intensive classification process.

Windows and Linux:

For an increased Java heap space open the Bio7.ini file in the install directory of Bio7. In the file you can change the default memory settings e.g. 
the initial heap size -Xms and the maximum heap space -Xmx.

MacOSX:

For an increased Java heap space open the Bio7 package (context menu if you click on the icon) then go to Contents->MacOS and open the Bio7.ini file 
with a texteditor. In the file you can change the default memory settings e.g. the initial heap size -Xms and the maximum heap space -Xmx.

Example:

For instance to be able to return back much memory after image transfer use a low -Xms setting (e.g.: -Xms1024m). The maximum size for the Java Runtime (-Xmx) depends on the image size
and could be adjusted depending on the available RAM (using for instance the half of the available RAM) of the OS.

#### Image Transfer And Classification Data:

In the Settings tab the datatype from Java to R can be adjusted for a less memory consumption if possible (transfer and classification - options are: double, integer, raw).

#### Using the Bioformats library:

Using the Bio-Formats library with the ImageJ macro (enable the option 'Use ImageJ Macro at Import' in the 'Settings' tab) it is possible to crop huge images to parts before the R classification process starts.
The library has to be installed first as an ImageJ plugin.
Bio-Formats GUI import commands can be recorded with the ImageJ macro recorder for use in the available project macro for, e.g., a specific import.

## Customization

The 'ModelGui' graphical view interface can be modified or extended with the Eclipse WindowBuilder plugin (SWT) if installed in Bio7.
The Main.java class can be extended with further filters or default features. To store or load settings of the GUI edit the Settings.java file.

The default R scripts and the ImageJ import macro can be changed with the default Bio7 editors.




