import static com.eco.bio7.batch.Bio7Dialog.getCurrentPath;
import static com.eco.bio7.batch.Bio7Dialog.openMultipleFiles;
import static com.eco.bio7.image.ImageMethods.imageFeatureStackToR;
import static com.eco.bio7.image.ImageMethods.imageFromR;
import static com.eco.bio7.rbridge.RServeUtil.evalRScript;
import static com.eco.bio7.rbridge.RServeUtil.listRObjects;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
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
import com.eco.bio7.collection.CustomView;
import com.eco.bio7.collection.Work;
import com.eco.bio7.image.Util;
import com.eco.bio7.rbridge.RServe;
import com.eco.bio7.rbridge.RServeUtil;
import com.eco.bio7.rbridge.RState;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.GaborFilter;
import boofcv.alg.filter.derivative.DerivativeLaplacian;
import boofcv.alg.filter.derivative.DerivativeType;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayF32;
import filter.Kuwahara_Filter;
import filter.Lipschitz_;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.ChannelSplitter;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.RankFilters;
import ij.process.ColorProcessor;
import ij.process.ColorSpaceConverter;
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
				 * Create the GUI and transfer a reference to this class that the GUI can
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

		/*
		 * Important call to get the features and feature settings from the GUI
		 * (syncExec wrapped for SWT)!
		 */
		gui.getFeatureOptions();

		/* Create feature stack! */
		if (choice == 1) {
			String files = Bio7Dialog.openFile();
			if (files != null) {
				ImagePlus imPlus = createStackFeatures(null, files, monitor);
				imPlus.show();
				gui.layout();

			}

		}
		/* Create ROI Classes! */
		else if (choice == 2) {
			Bio7Dialog.message("Add selections (ROI's = specific class) to the ROI Manager.\n\n"
					+ "Rename the ROI's to identify the classes - at the end of the string use an underscore followed by the class"
					+ "number (class_1, class_2 or cell_1, cell_2..., etc.).\n\n"
					+ "Open the 'Pixel RM Stack action'\n\n"
					+ "Select the option 'Create signature from ROI Manager name' and transfer the ROI's!");
			/* Opens the ROI Manager of ImageJ! */
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

			if (gui.useDirectoryDialog) {
				String dirSelection = Bio7Dialog.directory("Select the base directory");
				File dir = new File(dirSelection);
				final String[] ext = { "tif", "tiff", "dcm", "png" };
				List<File> files = (List<File>) FileUtils.listFiles(dir, ext, true);
				for (int i = 0; i < files.size(); i++) {
					File file = files.get(i);

					String currentFile = null;
					try {
						currentFile = file.getCanonicalPath();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ImagePlus imPlus = createStackFeatures(currentFile, null, monitor);
					// System.out.println(choice);

					classify(monitor, imPlus);
				}
			} else {
				String[] files = openMultipleFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {

						ImagePlus imPlus = createStackFeatures(files[i], null, monitor);
						// System.out.println(choice);

						classify(monitor, imPlus);

					}
				}
			}

		}

	}

	private void classify(IProgressMonitor monitor, ImagePlus imPlus) {
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

	private ImagePlus createStackFeatures(String files, String singleFile, IProgressMonitor monitor) {
		ImagePlus imPlus = null;
		ImagePlus image = null;
		ImageStack stack = null;

		/*
		 * If we want to use an import macro, e.g., using the BioFormats library
		 * commands which can be recorded with the ImageJ macro recorder!
		 */
		if (gui.useImportMacro) {
			/* Multiple files selected! */
			if (files != null) {
				/* If we used the directory dialog to open all files! */
				if (gui.useDirectoryDialog) {
					IJ.runMacroFile(gui.getMacroTextOption(), files);
				} else {
					IJ.runMacroFile(gui.getMacroTextOption(), getCurrentPath() + "/" + files);
				}

			} else {
				/* Call ImageJ macro with option (file path)! */
				IJ.runMacroFile(gui.getMacroTextOption(), singleFile);
			}

		} else {
			/* Multiple files selected! */
			if (files != null) {
				/* If we used the directory dialog to open all files! */
				if (gui.useDirectoryDialog) {
					image = IJ.openImage(files);
				} else {
					image = IJ.openImage(getCurrentPath() + "/" + files);
				}

			} else {
				image = IJ.openImage(singleFile);
			}
		}
		/*
		 * We must avoid a null reference when using the BioFormats library with the
		 * macro import option!
		 */
		if (gui.useImportMacro && image == null) {
			image = WindowManager.getCurrentImage();
		}

		/* If we have a RGB! */
		if (image.getProcessor() instanceof ColorProcessor) {
			/* Convert to HSB! */
			if (gui.toHsb) {
				monitor.setTaskName("Convert RGB To HSB Color Space");
				ImageConverter con = new ImageConverter(image);
				con.convertToHSB32();

				String opt = gui.channelOption;
				String[] channelToInclude = opt.split(",");
				ImageStack hsbStack = image.getStack();
				/* Create a feature stack from selected HSB channels! */
				stack = new ImageStack(image.getWidth(), image.getHeight());
				if (opt.isEmpty() == false && channelToInclude.length > 0) {

					for (int j = 0; j < channelToInclude.length; j++) {
						/* Add selected HSB float channels to the stack! */
						int sel = Integer.parseInt(channelToInclude[j]);
						/* Use selected slices! */
						ImageProcessor floatProcessor = hsbStack.getProcessor(sel);
						stack.addSlice("Channel_" + j, floatProcessor);
					}
				} else {
					/* Use all slices. Important to convert to Float! */
					stack = image.getStack();
				}

			}
			/* Convert to LAB! */
			else if (gui.toLab) {
				monitor.setTaskName("Convert RGB To LAB Color Space");
				ColorSpaceConverter converter = new ColorSpaceConverter();
				ImagePlus imp = converter.RGBToLab(image);
				// imp.show();
				image.hide();
				imp.copyAttributes(image);
				image.changes = false;
				image.close();
				String opt = gui.channelOption;
				String[] channelToInclude = opt.split(",");
				ImageStack labStack = imp.getStack();
				/* Create a LAB stack from selected channels! */
				stack = new ImageStack(imp.getWidth(), imp.getHeight());
				if (opt.isEmpty() == false && channelToInclude.length > 0) {
					for (int j = 0; j < channelToInclude.length; j++) {
						/* Add LAB channels to the stack. LAB stack are already float images! */
						int sel = Integer.parseInt(channelToInclude[j]);
						/* Use selected slices! */
						ImageProcessor floatProcessor = labStack.getProcessor(sel);
						stack.addSlice("Channel_" + j, floatProcessor);
					}
				} else {
					/* Use all slices. LAB stack are already float images! */
					stack = imp.getStack();
				}

			}

			else {

				/* Split original to R,G,B channels! */
				ImagePlus[] channels = ChannelSplitter.split(image);

				String opt = gui.channelOption;
				String[] channelToInclude = opt.split(",");

				/* Create a feature stack from all available channels (e.g., R,G,B) images! */
				stack = new ImageStack(image.getWidth(), image.getHeight());
				if (opt.isEmpty() == false && channelToInclude.length > 0) {
					for (int j = 0; j < channelToInclude.length; j++) {
						/* Add selected RGB channels to the new stack! */
						/* Convert original to float to have a float image stack for the filters! */
						int sel = Integer.parseInt(channelToInclude[j]) - 1;// Channels index starts with 0 so we
																			// correct here with -1!
						ImageProcessor floatProcessor = channels[sel].getProcessor().convertToFloat();
						stack.addSlice("Channel_" + j, floatProcessor);
					}

				} else {
					for (int j = 0; j < channels.length; j++) {
						/* Add RGB channels to the stack! */
						/* Convert original to float to have a float image stack for the filters! */
						ImageProcessor floatProcessor = channels[j].getProcessor().convertToFloat();
						stack.addSlice("Channel_" + j, floatProcessor);
					}
				}
			}
		} else {/* Grayscale images (8-bit, 16-bit, 32-bit) */
			/* If we have a grayscale stack! */
			if (image.getStackSize() > 1) {
				String opt = gui.channelOption;
				String[] channelToInclude = opt.split(",");
				/* Check if we only want to include certain slices! */
				if (opt.isEmpty() == false && channelToInclude.length > 0) {
					stack = new ImageStack(image.getWidth(), image.getHeight());
					for (int j = 0; j < channelToInclude.length; j++) {
						/* Add selected slices to a new stack! */
						int sel = Integer.parseInt(channelToInclude[j]);// Stack starts with index 1 no correction
																		// necessary!
						stack.addSlice("Grayscale_Layer_" + j, image.getStack().getProcessor(sel).convertToFloat());
					}
				} else {
					/* Convert original to float to have a float image stack for the filters! */
					stack = image.getStack().convertToFloat();
				}
			} else {

				stack = new ImageStack(image.getWidth(), image.getHeight());
				/* Convert original to float to have a float image stack for the filters! */
				stack.addSlice("Grayscale", image.getProcessor().convertToFloat());
			}
		}

		/*
		 * Duplicate the base stack as basis for the different filters! the filtered
		 * images will be added to the base stack!
		 */

		ImageStack tempStack = stack.duplicate();
		/*
		 * See:
		 * https://imagej.nih.gov/ij/developer/api/ij/plugin/filter/GaussianBlur.html#
		 * blur-ij.process.ImageProcessor-double-
		 */
		if (gui.gaussian) {
			monitor.setTaskName("Apply Gaussian Filter");
			GaussianBlur gaussian = new GaussianBlur();
			/* Split the gaussian option to get all sigmas! */
			String[] gaussianSigma = gui.gaussianOption.split(",");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				for (int j = 0; j < gaussianSigma.length; j++) {
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					double sigma = Double.parseDouble(gaussianSigma[j]);
					gaussian.blurGaussian(ip, 0.4 * sigma, 0.4 * sigma, 0.0002);
					stack.addSlice("Gaussian_" + "Sigma_" + sigma + "Layer_" + i, ip);
				}
			}
		}

		if (gui.diffOfGaussian) {
			monitor.setTaskName("Apply Difference of Gaussian Filters");
			GaussianBlur gaussian = new GaussianBlur();
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {

				/* See if we have several Gaussian Difference filter settings! */
				String[] gaussianDiffOptionSet = gui.diffGaussianOption.split(";");
				for (int j = 0; j < gaussianDiffOptionSet.length; j++) {

					String opGaussianDiff = gaussianDiffOptionSet[j];
					/* Split the gaussian option to get all sigmas! */
					String[] gaussianSigma = opGaussianDiff.split(",");

					/* Here we have two sigmas to calculate the difference! */
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					double sigma1 = Double.parseDouble(gaussianSigma[0]);
					gaussian.blurGaussian(ip, sigma1, sigma1, 0.0002);

					ImageProcessor ip2 = tempStack.getProcessor(i).duplicate();
					double sigma2 = Double.parseDouble(gaussianSigma[1]);
					gaussian.blurGaussian(ip2, sigma2, sigma2, 0.0002);

					ImageCalculator ic = new ImageCalculator();
					ImagePlus finalDiffGaussian = ic.run("Subtract create 32-bit", new ImagePlus("sigma1", ip),
							new ImagePlus("sigma2", ip2));
					stack.addSlice("DiffOfGaussian_Set_" + j + "_Layer" + i, finalDiffGaussian.getProcessor());

				}

			}

		}

		if (gui.median) {
			monitor.setTaskName("Apply Median Filter");
			/* Split the median option to get all radii! */
			String[] medianRadius = gui.medianOption.split(",");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				for (int j = 0; j < medianRadius.length; j++) {
					double radius = Double.parseDouble(medianRadius[j]);
					// IJ.run(plus, "Median...", "radius="+medianSigma[j]);
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					RankFilters ran = new RankFilters();
					extracted(radius, ran, ip, RankFilters.MEDIAN);
					stack.addSlice("Median_" + "Radius_" + radius + "Layer_" + i, ip);
				}
			}
		}

		if (gui.mean) {
			monitor.setTaskName("Apply Mean Filter");
			/* Split the mean option to get all radii! */
			String[] meanRadius = gui.meanOption.split(",");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				for (int j = 0; j < meanRadius.length; j++) {
					double radius = Double.parseDouble(meanRadius[j]);
					// IJ.run(plus, "Mean...", "radius="+meanSigma[j]);
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					RankFilters ran = new RankFilters();
					ran.rank(ip, radius, RankFilters.MEAN);
					stack.addSlice("Mean_" + "Radius_" + radius + "Layer_" + i, ip);
				}
			}
		}

		if (gui.variance) {
			monitor.setTaskName("Apply Variance Filter");
			/* Split the mean option to get all radii! */
			String[] varianceSigma = gui.varianceOption.split(",");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				for (int j = 0; j < varianceSigma.length; j++) {
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					double radius = Double.parseDouble(varianceSigma[j]);
					RankFilters ran = new RankFilters();
					extracted(radius, ran, ip, RankFilters.VARIANCE);
					stack.addSlice("Variance_" + "Radius_" + radius + "Layer_" + i, ip);
				}
			}
		}
		if (gui.maximum) {
			monitor.setTaskName("Apply Maximum Filter");
			/* Split the mean option to get all radii! */
			String[] maximumSigma = gui.maximumOption.split(",");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				for (int j = 0; j < maximumSigma.length; j++) {
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					double radius = Double.parseDouble(maximumSigma[j]);
					RankFilters ran = new RankFilters();
					extracted(radius, ran, ip, RankFilters.MAX);
					stack.addSlice("Maximum_" + "Radius_" + radius + "Layer_" + i, ip);
				}
			}
		}

		if (gui.minimum) {
			monitor.setTaskName("Apply Minimum Filter");
			/* Split the mean option to get all radii! */
			String[] minimumSigma = gui.minimumOption.split(",");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				for (int j = 0; j < minimumSigma.length; j++) {
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					double radius = Double.parseDouble(minimumSigma[j]);
					RankFilters ran = new RankFilters();
					extracted(radius, ran, ip, RankFilters.MIN);
					// ran.rank(ip, Double.parseDouble(minimumSigma[j]), RankFilters.MIN);
					stack.addSlice("Minimum_" + "Radius_" + radius + "Layer_" + i, ip);
				}
			}
		}

		if (gui.gradientHessian) {
			monitor.setTaskName("Apply Gradient, Hessian Derivative");
			/*  */
			GaussianBlur gaussian = new GaussianBlur();
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {

				String[] gradientHessian = gui.gradientHessianOption.split(",");
				/* Apply a Gaussian blur if we have double arguments! */
				if (gradientHessian[0].isEmpty() == false) {
					for (int j = 0; j < gradientHessian.length; j++) {
						double sigma = Double.parseDouble(gradientHessian[j]);

						ImageProcessor ip = tempStack.getProcessor(i).duplicate();

						gaussian.blurGaussian(ip, 0.4 * sigma, 0.4 * sigma, 0.0002);

						gradient(stack, ip);

					}
				} else {
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();

					gradient(stack, ip);
				}
			}
		}

		if (gui.laplacian) {
			monitor.setTaskName("Apply Laplacian Derivative");
			int stackSize = tempStack.getSize();
			GaussianBlur gaussian = new GaussianBlur();
			for (int i = 1; i <= stackSize; i++) {
				String[] laplacianSigma = gui.laplacianOption.split(",");
				/* Apply a Gaussian blur if we have double arguments! */
				if (laplacianSigma[0].isEmpty() == false) {
					for (int j = 0; j < laplacianSigma.length; j++) {
						double sigma = Double.parseDouble(laplacianSigma[j]);
						ImageProcessor ip = tempStack.getProcessor(i).duplicate();
						gaussian.blurGaussian(ip, 0.4 * sigma, 0.4 * sigma, 0.0002);
						int width = ip.getWidth();
						int height = ip.getHeight();
						GrayF32 boofFilterImageInput = new GrayF32(width, height);
						GrayF32 boofFilterImageOutput = new GrayF32(width, height);
						/* Transfer ImageProcessor data in place to boofcv image input! */
						ipToBoofCVGray32(ip, boofFilterImageInput);
						DerivativeLaplacian.process(boofFilterImageInput, boofFilterImageOutput, null);
						FloatProcessor flProcessor = new FloatProcessor(width, height, boofFilterImageOutput.getData());
						stack.addSlice("Laplacian_Derivative_From_Gaussian_" + sigma + "_Layer" + i, flProcessor);
					}
				} else {
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					int width = ip.getWidth();
					int height = ip.getHeight();
					GrayF32 boofFilterImageInput = new GrayF32(width, height);
					GrayF32 boofFilterImageOutput = new GrayF32(width, height);
					/* Transfer ImageProcessor data in place to boofcv image input! */
					ipToBoofCVGray32(ip, boofFilterImageInput);
					DerivativeLaplacian.process(boofFilterImageInput, boofFilterImageOutput, null);
					FloatProcessor flProcessor = new FloatProcessor(width, height, boofFilterImageOutput.getData());
					stack.addSlice("Laplacian Derivative_" + "Layer_" + i, flProcessor);

				}
			}
		}

		if (gui.edges) {
			// see:
			// https://imagejdocu.tudor.lu/faq/technical/what_is_the_algorithm_used_in_find_edges
			monitor.setTaskName("Apply Edges");
			int stackSize = tempStack.getSize();
			GaussianBlur gaussian = new GaussianBlur();
			for (int i = 1; i <= stackSize; i++) {
				String[] edgesSigma = gui.edgesOption.split(",");
				/* Apply a Gaussian blur if we have double arguments! */
				if (edgesSigma[0].isEmpty() == false) {
					for (int j = 0; j < edgesSigma.length; j++) {
						double sigma = Double.parseDouble(edgesSigma[j]);
						ImageProcessor ip = tempStack.getProcessor(i).duplicate();
						gaussian.blurGaussian(ip, 0.4 * sigma, 0.4 * sigma, 0.0002);
						IJ.run(new ImagePlus("Edges_layer" + i + "_temp", ip), "Find Edges", "stack");
						stack.addSlice("Edges_Layer_From_Gaussian_" + j + "_Layer_" + i, ip);

					}

				} else {
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					IJ.run(new ImagePlus("Edges_layer" + i + "_temp", ip), "Find Edges", "stack");
					stack.addSlice("Edges_" + "Layer_" + i, ip);

				}
			}

		}

		if (gui.lipschitz) {

			monitor.setTaskName("Apply Lipschitz Filter");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {

				/* See if we have several Lipschitz filter settings! */
				String[] libschitzOptionsSet = gui.lipschitzOption.split(";");
				for (int j = 0; j < libschitzOptionsSet.length; j++) {
					ImageProcessor ip = tempStack.getProcessor(i).duplicate().convertToByte(true);
					String opLipschitz = libschitzOptionsSet[j];
					/* Split the Lipschitz set for one filter! */
					String[] lipschitzOptions = opLipschitz.split(",");
					Lipschitz_ filter = new Lipschitz_();
					Lipschitz_.setDownHatFilter(Boolean.parseBoolean(lipschitzOptions[0]));
					Lipschitz_.setTopHatFilter(Boolean.parseBoolean(lipschitzOptions[1]));
					Lipschitz_.setSlopeFilter(Double.parseDouble(lipschitzOptions[2]));
					filter.Lipschitz2D(ip);
					stack.addSlice("Lipschitz_Set_" + j + "_Layer_" + i, ip.convertToFloat());
				}
			}
		}

		if (gui.gabor) {

			monitor.setTaskName("Apply Gabor Filter");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				/* See if we have several Gabor filter settings! */
				String[] gaborOptionsSet = gui.gaborOption.split(";");

				for (int j = 0; j < gaborOptionsSet.length; j++) {

					String opGabor = gaborOptionsSet[j];
					/* Split the Gabor set for one filter! */
					String[] gaborOptions = opGabor.split(",");
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					/* Will work with 8-bit only! */
					BufferedImage buff = new ImagePlus("tempGabor", ip).getBufferedImage();
					FastBitmap fb = new FastBitmap(buff);

					GaborFilter gabor = new GaborFilter();
					/* Extract the arguments for one filter for the different layers! */
					gabor.setSize(Integer.parseInt(gaborOptions[0]));
					gabor.setWavelength(Double.parseDouble(gaborOptions[1]));
					gabor.setOrientation(Double.parseDouble(gaborOptions[2]));
					gabor.setPhaseOffset(Double.parseDouble(gaborOptions[3]));
					gabor.setGaussianVar(Double.parseDouble(gaborOptions[4]));
					gabor.setAspectRatio(Double.parseDouble(gaborOptions[5]));

					gabor.applyInPlace(fb);
					float[] imArray = fb.toArrayGrayAsFloat();
					int width = ip.getWidth();
					int height = ip.getHeight();
					stack.addSlice("Gabor_Set_" + j + "_Layer_" + i, new FloatProcessor(width, height, imArray));
					// new Gabor_Filter(ip,stack);

				}
			}
		}

		if (gui.topHat) {
			monitor.setTaskName("Apply Top Hat Filter");
			/* Split the mean option to get all radii! */
			String[] topHatRadius = gui.topHatOption.split(",");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				for (int j = 0; j < topHatRadius.length; j++) {
					double radius = Double.parseDouble(topHatRadius[j]);
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					RankFilters ran = new RankFilters();
					ran.rank(ip, radius, RankFilters.TOP_HAT);
					stack.addSlice("Top_Hat_" + radius + "_Layer_" + i, ip);
				}
			}
		}

		if (gui.kuwahara) {
			monitor.setTaskName("Apply Kuwahara Filter");
			int stackSize = tempStack.getSize();
			for (int i = 1; i <= stackSize; i++) {
				/* Split the mean option to get all radii! */
				String[] kuwaharaOptions = gui.kuwaharaOption.split(",");
				for (int j = 0; j < kuwaharaOptions.length; j++) {
					int radius = Integer.parseInt(kuwaharaOptions[j]);
					ImageProcessor ip = tempStack.getProcessor(i).duplicate();
					/* Will work with 8-bit only! */
					Kuwahara_Filter kuw = new Kuwahara_Filter();
					Kuwahara_Filter.size = radius;
					kuw.filter(ip);
					stack.addSlice("Kuwahara_" + radius + "_Layer_" + i, ip);
				}
			}
		}

		if (gui.convolve) {
			monitor.setTaskName("Apply Convolve");
			String[] matrices = gui.convolveOption.split(";");
			for (int i = 0; i < matrices.length; i++) {
				int stackSize = tempStack.getSize();
				for (int u = 1; u <= stackSize; u++) {
					ImageProcessor ip = tempStack.getProcessor(u).duplicate();
					IJ.run(new ImagePlus("Convolved_" + i + "_layer" + u + "_temp", ip), "Convolve...", matrices[i]);
					stack.addSlice("Convolved_" + i + "_Layer_" + u, ip);
				}
			}

		}

		String name = image.getShortTitle();
		imPlus = new ImagePlus(name, stack);
		image = null;
		stack = null;
		tempStack = null;
		return imPlus;
	}

	private void gradient(ImageStack stack, ImageProcessor ip) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		GrayF32 boofFilterImageInput = new GrayF32(width, height);

		/* Transfer ImageProcessor data in place to boofcv image input! */
		ipToBoofCVGray32(ip, boofFilterImageInput);

		// First order derivative, also known as the gradient
		GrayF32 derivX = new GrayF32(boofFilterImageInput.width, boofFilterImageInput.height);
		GrayF32 derivY = new GrayF32(boofFilterImageInput.width, boofFilterImageInput.height);

		GImageDerivativeOps.gradient(DerivativeType.SOBEL, boofFilterImageInput, derivX, derivY, BorderType.EXTENDED);

		// Second order derivative, also known as the Hessian
		// GrayF32 derivXX = new GrayF32(boofFilterImageInput.width,
		// boofFilterImageInput.height);
		// GrayF32 derivXY = new GrayF32(boofFilterImageInput.width,
		// boofFilterImageInput.height);
		// GrayF32 derivYY = new GrayF32(boofFilterImageInput.width,
		// boofFilterImageInput.height);

		// GImageDerivativeOps.hessian(DerivativeType.SOBEL, derivX, derivY, derivXX,
		// derivXY, derivYY,
		// BorderType.EXTENDED);

		FloatProcessor flxProcessor = new FloatProcessor(width, height, derivX.getData());
		FloatProcessor flyProcessor = new FloatProcessor(width, height, derivY.getData());

		// FloatProcessor flxxProcessor = new FloatProcessor(width, height,
		// derivXX.getData());
		// FloatProcessor flxyProcessor = new FloatProcessor(width, height,
		// derivXY.getData());
		// FloatProcessor flyyProcessor = new FloatProcessor(width, height,
		// derivYY.getData());

		stack.addSlice("Gradient_Sobel X", flxProcessor);
		stack.addSlice("Gradient_Sobel Y", flyProcessor);

		// stack.addSlice("Hessian_Sobel XX", flxxProcessor);
		// stack.addSlice("Hessian_Sobel XY", flxyProcessor);
		// stack.addSlice("Hessian_Sobel YY", flyyProcessor);
	}

	private void extracted(double radius, final RankFilters ran, ImageProcessor ip, int FilterType) {

		ran.rank(ip, radius, FilterType);

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
