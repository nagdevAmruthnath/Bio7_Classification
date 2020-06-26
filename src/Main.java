import static com.eco.bio7.batch.Bio7Dialog.getCurrentPath;
import static com.eco.bio7.batch.Bio7Dialog.openMultipleFiles;
import static com.eco.bio7.image.ImageMethods.imageFeatureStackToR;
import static com.eco.bio7.image.ImageMethods.imageFromR;
import static com.eco.bio7.rbridge.RServeUtil.*;

import java.awt.image.BufferedImage;

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

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.RankFilters;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
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
				/*
				 * Create the GUI and transfer a reference to this class thus that the GUI can
				 * execute methods from this class!
				 */
				gui = new ModelGui(parent, Main.this, SWT.NONE);

				parent.layout(true);
			}
		});
	}

	/* Called from the GUI class! */
	public void executeSelection(int choice) {

		Job job = new Job("Classification Process") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Transfer Image Data And Convolve ...", IProgressMonitor.UNKNOWN);
				/* We create a feature stack. R connection not necessary! */
				if (choice == 1) {
					action(choice, monitor);
				}

				else {
					if (RServe.isAliveDialog()) {
						if (RState.isBusy() == false) {
							/* Notify that R is busy! */
							RState.setBusy(true);
							action(choice, monitor);
						} else {
							System.out.println("RServer is busy. Can't execute the R script!");
						}

					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}

		};
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK()) {

					RState.setBusy(false);
					/* Update the R-Shell view workspace objects! */
					if (RServe.isAlive()) {
						listRObjects();
					}
				} else {

					RState.setBusy(false);
				}
			}
		});

		job.schedule();

	}

	public void action(int choice, IProgressMonitor monitor) {

		/* Create feature stack! */
		if (choice == 1) {
			String files = Bio7Dialog.openFile();
			if (files != null) {
				// System.out.println(files);
				// for (int i = 0; i < files.length; i++) {
				ImagePlus imPlus = createStackFeatures(null, files, monitor);
				imPlus.show();
				gui.layout();
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

		/* Train Classifier with external script! */
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
			monitor.setTaskName("Apply Training Script");
			String path = gui.getPathTrainingScript();
			/* Execute R script! */
			if (path.endsWith(".R")) {
				evalRScript(path);
			}
		}

		/* Classify selected images with external Script! */
		else if (choice == 4) {

			String[] files = openMultipleFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {

					ImagePlus imPlus = createStackFeatures(files[i], null, monitor);
					// System.out.println(choice);

					/* Correct some image names for R! */
					String name = imPlus.getTitle();
					String nameCorrected = RServeUtil.replaceWrongRWord(name);
					/* Transfer the feature stack to R! */
					imageFeatureStackToR(nameCorrected, 0, imPlus);
					RConnection rcon = RServe.getConnection();
					try {
						rcon.eval("current_feature_stack<-" + nameCorrected + "");
					} catch (RserveException e) {
						e.printStackTrace();
					}
					monitor.setTaskName("Apply Classification Script");
					/* Predict in R (evalRScript is a custom method) with the randomForest model! */
					String path = gui.getPathClassificationScript();
					if (path.endsWith(".R")) {
						evalRScript(path);
						/* Transfer the classification result from R back to ImageJ! */
						imageFromR(3, "imageMatrix", 1);
						WindowManager.getCurrentWindow().getImagePlus().setTitle(name + "_Classified");
						gui.layout();
					}

				}
			}

		}

	}

	private ImagePlus createStackFeatures(String files, String singleFile, IProgressMonitor monitor) {
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
				monitor.setTaskName("Convert RGB To HSB Color Space");
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
		 * image copies with specified sigmas to the feature stack!
		 */

		ImageStack tempStack = stack.duplicate();

		if (gui.gaussian) {
			monitor.setTaskName("Apply Gaussian Filter");

			GaussianBlur gaussian = new GaussianBlur();
			/* Split the gaussian option to get all sigmas! */
			String[] gaussianSigma = gui.gaussianOption.split(",");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {

				for (int j = 0; j < gaussianSigma.length; j++) {
					ImagePlus plus = new ImagePlus("gaussian_sigma_" + gaussianSigma[j],
							tempStack.getProcessor(i).duplicate());
					// IJ.run(plus, "Gaussian Blur...", "radius="+gaussianSigma[j]);
					ImageProcessor ip = plus.getProcessor();
					double sigma = Double.parseDouble(gaussianSigma[j]);
					/*
					 * See:
					 * https://imagej.nih.gov/ij/developer/api/ij/plugin/filter/GaussianBlur.html#
					 * blur-ij.process.ImageProcessor-double-
					 */
					int width = plus.getWidth();
					int height = plus.getHeight();
					GrayF32 boofFilterImageInput = new GrayF32(width, height);
					GrayF32 boofFilterImageOutput = new GrayF32(width, height);
					/* Transfer ImageProcessor data in place to boofcv image input! */
					ipToBoofCVGray32(ip, boofFilterImageInput);
					// GrayF32 boofFilterImageInput =
					// ConvertBufferedImage.convertFromSingle(plus.getBufferedImage(), null,
					// GrayF32.class);
					BlurImageOps.gaussian(boofFilterImageInput, boofFilterImageOutput, sigma, -1, null);
					FloatProcessor flProcessor = new FloatProcessor(width, height, boofFilterImageOutput.getData());
					// BufferedImage buff=plus.getBufferedImage();

					// ImagePlus boofFinal=new ImagePlus(plus.getTitle(),flProcessor);
					stack.addSlice(plus.getTitle(), flProcessor);
					// gaussian.blurGaussian(ip, 0.4*sigma,0.4*sigma,0.0002);
					// stack.addSlice(plus.getTitle(), plus.getProcessor());
				}
			}
			// ImagePlus gaussianFiltered = duplicator.run(image);
			// IJ.run(gaussianFiltered, "Gaussian Blur...", gui.gaussianOption);

		}

		if (gui.median) {
			monitor.setTaskName("Apply Median Filter");
			/* Split the median option to get all sigmas! */
			String[] medianSigma = gui.medianOption.split(",");
			// final RankFilters ran=new RankFilters();
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {

				for (int j = 0; j < medianSigma.length; j++) {
					int sigma = Integer.parseInt(medianSigma[j]);
					ImagePlus plus = new ImagePlus("median_sigma_" + sigma, tempStack.getProcessor(i).duplicate());
					// IJ.run(plus, "Median...", "radius="+medianSigma[j]);
					ImageProcessor ip = plus.getProcessor();
					// ran.rank(ip, Double.parseDouble(medianSigma[j]), 4);
					// stack.addSlice(plus.getTitle(), plus.getProcessor());
					int width = plus.getWidth();
					int height = plus.getHeight();
					GrayF32 boofFilterImageInput = new GrayF32(width, height);
					GrayF32 boofFilterImageOutput = new GrayF32(width, height);
					/* Transfer ImageProcessor data in place to boofcv image input! */
					ipToBoofCVGray32(ip, boofFilterImageInput);
					// GrayF32 boofFilterImageInput =
					// ConvertBufferedImage.convertFromSingle(plus.getBufferedImage(), null,
					// GrayF32.class);
					BlurImageOps.median(boofFilterImageInput, boofFilterImageOutput, sigma);
					FloatProcessor flProcessor = new FloatProcessor(width, height, boofFilterImageOutput.getData());
					stack.addSlice(plus.getTitle(), flProcessor);
				}
			}
		}

		if (gui.mean) {
			monitor.setTaskName("Apply Mean Filter");
			/* Split the mean option to get all sigmas! */
			String[] meanSigma = gui.meanOption.split(",");
			// final RankFilters ran=new RankFilters();
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {

				for (int j = 0; j < meanSigma.length; j++) {
					int sigma = Integer.parseInt(meanSigma[j]);
					ImagePlus plus = new ImagePlus("mean_sigma_" + meanSigma[j], tempStack.getProcessor(i).duplicate());
					// IJ.run(plus, "Mean...", "radius="+meanSigma[j]);
					ImageProcessor ip = plus.getProcessor();
					int width = plus.getWidth();
					int height = plus.getHeight();
					GrayF32 boofFilterImageInput = new GrayF32(width, height);
					GrayF32 boofFilterImageOutput = new GrayF32(width, height);
					/* Transfer ImageProcessor data in place to boofcv image input! */
					ipToBoofCVGray32(ip, boofFilterImageInput);
					// GrayF32 boofFilterImageInput =
					// ConvertBufferedImage.convertFromSingle(plus.getBufferedImage(), null,
					// GrayF32.class);
					BlurImageOps.mean(boofFilterImageInput, boofFilterImageOutput, sigma, null, null);
					FloatProcessor flProcessor = new FloatProcessor(width, height, boofFilterImageOutput.getData());
					stack.addSlice(plus.getTitle(), flProcessor);

					// ran.rank(ip, Double.parseDouble(meanSigma[j]), 0);
					// stack.addSlice(plus.getTitle(), plus.getProcessor());
				}
			}
		}
		if (gui.minimum) {
			monitor.setTaskName("Apply Minimum Filter");
			/* Split the mean option to get all sigmas! */
			String[] minimumSigma = gui.minimumOption.split(",");
			final RankFilters ran = new RankFilters();
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {

				for (int j = 0; j < minimumSigma.length; j++) {

					ImagePlus plus = new ImagePlus("minimum_sigma_" + minimumSigma[j],
							tempStack.getProcessor(i).duplicate());

					// IJ.run(plus, "Minimum...", "radius="+minimumSigma[j]);

					ImageProcessor ip = plus.getProcessor();
					ran.rank(ip, Double.parseDouble(minimumSigma[j]), 1);
					stack.addSlice(plus.getTitle(), plus.getProcessor());
				}
			}
		}

		if (gui.maximum) {
			monitor.setTaskName("Apply Maximum Filter");
			/* Split the mean option to get all sigmas! */

			String[] maximumSigma = gui.maximumOption.split(",");
			final RankFilters ran = new RankFilters();
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {

				for (int j = 0; j < maximumSigma.length; j++) {
					ImagePlus plus = new ImagePlus("maximum_sigma_" + maximumSigma[j],
							tempStack.getProcessor(i).duplicate());
					ImageProcessor ip = plus.getProcessor();
					ran.rank(ip, Double.parseDouble(maximumSigma[j]), 2);

					// IJ.run(plus, "Maximum...", "radius="+maximumSigma[j]);

					stack.addSlice(plus.getTitle(), ip);
				}
			}
		}

		if (gui.edges) {
			monitor.setTaskName("Apply Edges");
			ImagePlus edgesCreated = duplicator.run(image);
			IJ.run(edgesCreated, "Find Edges", "");
			stack.addSlice("edges", edgesCreated.getProcessor());
		}

		if (gui.convolve) {
			monitor.setTaskName("Apply Convolve");
			String[] matrices = gui.convolveOption.split(";");
			for (int i = 0; i < matrices.length; i++) {
				ImagePlus convolvedFiltered = duplicator.run(image);
				// System.out.println(matrices[i]);
				IJ.run(convolvedFiltered, "Convolve...", matrices[i]);
				stack.addSlice("convolved" + i, convolvedFiltered.getProcessor());
			}

		}

		String name = image.getShortTitle();
		imPlus = new ImagePlus(name, stack);

		return imPlus;
	}

	private void ipToBoofCVGray32(ImageProcessor ip, GrayF32 boofFilterImageInput) {
		float[][] fl = ip.getFloatArray();
		for (int y = 0; y < ip.getHeight(); y++) {
			for (int x = 0; x < ip.getWidth(); x++) {
				float value = fl[x][y];
				boofFilterImageInput.set(x, y, value);
			}
		}
	}

	/* Only implemented to avoid a console warning! */
	public static void main(String[] args) {

	}
}
