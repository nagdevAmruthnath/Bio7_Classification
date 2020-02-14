import static com.eco.bio7.batch.Bio7Dialog.getCurrentPath;
import static com.eco.bio7.batch.Bio7Dialog.openMultipleFiles;
import static com.eco.bio7.image.ImageMethods.imageFeatureStackToR;
import static com.eco.bio7.image.ImageMethods.imageFromR;
import static com.eco.bio7.rbridge.RServeUtil.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import com.eco.bio7.batch.Bio7Dialog;
import com.eco.bio7.batch.FileRoot;
import com.eco.bio7.collection.CustomView;
import com.eco.bio7.collection.Work;
import com.eco.bio7.image.CanvasView;
import com.eco.bio7.image.Util;
import com.eco.bio7.rbridge.RServe;
import com.eco.bio7.rbridge.RServeUtil;
import com.eco.bio7.rbridge.RState;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class Main {

	private ModelGui gui;

	public Main() {

		CustomView view = new CustomView();

		Display display = Util.getDisplay();

		display.syncExec(new Runnable() {

			public void run() {
				Composite parent = view.getComposite("Classification");
				gui = new ModelGui(parent, Main.this, SWT.NONE);

				parent.layout(true);
			}
		});
	}

	/* Called from the GUI class! */
	public void executeSelection(int choice) {

		if (RServe.isAliveDialog()) {
			if (RState.isBusy() == false) {
				/* Notify that R is busy! */
				RState.setBusy(true);
				Job job = new Job("Transfer from R") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Transfer Image Data And Convolve ...", IProgressMonitor.UNKNOWN);
						action(choice);
						monitor.done();
						return Status.OK_STATUS;
					}

				};
				job.addJobChangeListener(new JobChangeAdapter() {
					public void done(IJobChangeEvent event) {
						if (event.getResult().isOK()) {

							RState.setBusy(false);
							/* Update the R-Shell view workspace objects! */
							listRObjects();
						} else {

							RState.setBusy(false);
						}
					}
				});

				job.schedule();
			} else {
				System.out.println("RServer is busy. Can't execute the R script!");
			}
		}

	}

	public void action(int choice) {

		/* Create feature stack! */
		if (choice == 1) {
			String files = Bio7Dialog.openFile();
			if (files != null) {
				System.out.println(files);
				// for (int i = 0; i < files.length; i++) {
				ImagePlus imPlus = createStackFeatures(null, files);
				imPlus.show();
				// }
			}

		}
		/* Create ROI Classes! */
		else if (choice == 2) {
			Bio7Dialog.message(
					"Add selections to the ROI Manager and transfer the ROI selections with the 'Pixel RM Stack action'\n\n'"
							+ "Create class names with an underscore (class_1, class_2... etc.) and select the option 'Create signature form ROI Manager name'\n"
							+ "in the 'Pixel RM Stack action'!");
			IJ.run("ROI Manager...", "");

		}

		/* Train Classifier with external R script! */
		else if (choice == 3) {
			/*
			 * We have set the busy variable to false to use the R-Shell selection in this
			 * job!
			 */
			RState.setBusy(false);
			Work.openView("com.eco.bio7.RShell");
			Bio7Dialog.selection("Select training features (classes) in R-Shell!\n\n"
					+ "Select multiple with STRG (CMD)+MouseClick or SHIFT+MouseClick!\n\nPress 'OK' when selected to execute the training R script!");
			/* Set the busy variable again to true because now we call R! */
			RState.setBusy(true);
			evalRScript(gui.getPathTrainingRScript());
		}

		/* Classify selected images with external R Script! */
		else if (choice == 4) {

			String[] files = openMultipleFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {

					ImagePlus imPlus = createStackFeatures(files[i], null);
					// System.out.println(choice);

					/* Correct some image names for R! */
					String name = imPlus.getTitle();
					String nameCorrected = RServeUtil.replaceWrongRWord(name);
					imageFeatureStackToR(nameCorrected, 0, imPlus);
					RConnection rcon = RServe.getConnection();
					try {
						rcon.eval("current_feature_stack<-" + nameCorrected + "");
					} catch (RserveException e) {
						e.printStackTrace();
					}
					/* Predict in R (evalRScript is a custom method) with the randomForest model! */
					evalRScript(gui.getPathClassificationRScript());
					/* Transfer the classification result from R back to ImageJ! */
					imageFromR(3, "imageMatrix", 1);
					WindowManager.getCurrentWindow().getImagePlus().setTitle(name + "_Classified");

				}
			}

		}

	}

	private ImagePlus createStackFeatures(String files, String singleFile) {
		ImagePlus imPlus = null;
		ImagePlus image;

		if (files != null) {
			image = IJ.openImage(getCurrentPath() + "/" + files);// Open image data with the ImageJ without
																	// display!
		} else {
			image = IJ.openImage(singleFile);
		}

		/* Duplicate the image! */
		Duplicator duplicator = new Duplicator();
		/* Duplicate original for the RGB channels! */
		ImagePlus rgb = duplicator.run(image);
		ImageStack stack = null;
		/*
		 * Convert original to float for the filter (and grayscale layer if not color)
		 * images!
		 */
		image.setProcessor(image.getProcessor().convertToFloat());
		/* Important to call to get the features and feature options from the GUI! */
		gui.getFeatureOptions();
		/* If we have a RGB! */
		if (rgb.getProcessor() instanceof ColorProcessor) {

			if (gui.toHsb) {
				ImageConverter con = new ImageConverter(rgb);
				con.convertToHSB();

				String opt = gui.channelOption;
				String[] channelToInclude = opt.split(",");

				// IJ.run(rgb, "HSB Stack", "");
				ImageStack hsbStack = rgb.getStack();
				/* Create a feature stack from all available channels (e.g., R,G,B) images! */
				stack = new ImageStack(image.getWidth(), image.getHeight());
				if (opt.isEmpty() == false && channelToInclude.length > 0) {

					for (int j = 0; j < channelToInclude.length; j++) {
						/* Add RGB channels to the stack! */
						/* Convert original to float to have a float image stack for the filters! */
						int sel = Integer.parseInt(channelToInclude[j]);
						/* Use selected slices. Important to convert to Float! */
						ImageProcessor floatProcessor = hsbStack.getProcessor(sel).convertToFloat();
						stack.addSlice("Channel" + j, floatProcessor);
					}
				} else {
					/* Use all slices. Important to convert to Float! */
					stack = rgb.getStack().convertToFloat();
				}

			} else {

				/* Split original to R,G,B channels! */
				ImagePlus[] channels = ChannelSplitter.split(rgb);

				String opt = gui.channelOption;
				String[] channelToInclude = opt.split(",");

				/* Create a feature stack from all available channels (e.g., R,G,B) images! */
				stack = new ImageStack(image.getWidth(), image.getHeight());
				if (opt.isEmpty() == false && channelToInclude.length > 0) {
					for (int j = 0; j < channelToInclude.length; j++) {
						/* Add RGB channels to the stack! */
						/* Convert original to float to have a float image stack for the filters! */
						int sel = Integer.parseInt(channelToInclude[j]) - 1;
						ImageProcessor floatProcessor = channels[sel].getProcessor().convertToFloat();
						stack.addSlice("Channel" + j, floatProcessor);
					}

				} else {
					for (int j = 0; j < channels.length; j++) {
						/* Add RGB channels to the stack! */
						/* Convert original to float to have a float image stack for the filters! */
						ImageProcessor floatProcessor = channels[j].getProcessor().convertToFloat();
						stack.addSlice("Channel" + j, floatProcessor);
					}
				}
			}
		} else {/* Grayscale images (8-bit, 16-bit, 32-bit) */
			/* If we have a multichannel image! */
			if (image.getStackSize() > 1) {
				/* Convert original to float to have a float image stack for the filters! */
				stack = image.getStack().convertToFloat();
			} else {

				stack = new ImageStack(image.getWidth(), image.getHeight());
				stack.addSlice("grayscale", image.getProcessor());
			}
		}

		/*
		 * Duplicate the filtered images (our additional features!) and add the filtered
		 * image copies to the feature stack!
		 */

		if (gui.gaussian) {
			ImagePlus gaussianFiltered = duplicator.run(image);
			IJ.run(gaussianFiltered, "Gaussian Blur...", gui.gaussianOption);
			stack.addSlice("gaussian", gaussianFiltered.getProcessor());
		}
		if (gui.median) {
			ImagePlus medianFiltered = duplicator.run(image);
			IJ.run(medianFiltered, "Median...", gui.medianOption);
			stack.addSlice("median", medianFiltered.getProcessor());
		}

		if (gui.mean) {
			ImagePlus meanFiltered = duplicator.run(image);
			IJ.run(meanFiltered, "Mean...", gui.meanOption);
			stack.addSlice("mean", meanFiltered.getProcessor());
		}

		if (gui.minimum) {
			ImagePlus minimumedFiltered = duplicator.run(image);
			IJ.run(minimumedFiltered, "Minimum...", gui.minimumOption);
			stack.addSlice("minimum", minimumedFiltered.getProcessor());
		}

		if (gui.maximum) {
			ImagePlus maximumFiltered = duplicator.run(image);
			IJ.run(maximumFiltered, "Maximum...", gui.maximumOption);
			stack.addSlice("maximum", maximumFiltered.getProcessor());
		}

		if (gui.edges) {
			ImagePlus edgesCreated = duplicator.run(image);
			IJ.run(edgesCreated, "Find Edges", "");
			stack.addSlice("edges", edgesCreated.getProcessor());
		}

		if (gui.convolve) {
			ImagePlus convolvedFiltered = duplicator.run(image);
			IJ.run(convolvedFiltered, "Convolve...", gui.convolveOption);
			stack.addSlice("convolved", convolvedFiltered.getProcessor());
		}
		String name = image.getShortTitle();
		imPlus = new ImagePlus(name, stack);

		return imPlus;
	}

	/* Only implemented to avoid a console warning! */
	public static void main(String[] args) {

	}
}
