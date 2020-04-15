import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.eco.bio7.batch.Bio7Dialog;
import com.eco.bio7.batch.FileRoot;
import com.eco.bio7.image.CanvasView;
import com.eco.bio7.image.Util;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FillLayout;

public class ModelGui extends Composite {
	protected boolean convolve;
	protected boolean gaussian;
	protected boolean median;
	protected boolean mean;
	protected boolean maximum;
	protected boolean minimum;
	protected boolean edges;
	protected String convolveOption = "text1=[\n-1 -1 -1 -1 -1\n-1 -1 -1 -1 -1\n-1 -1 24 -1 -1\n-1 -1 -1 -1 -1\n-1 -1 -1 -1 -1\n] normalize";
	protected String medianOption = "2";
	protected String channelOption = "";
	protected String gaussianOption = "2";
	protected String meanOption = "2";
	protected String maximumOption = "2";
	protected String minimumOption = "2";
	private Main model;
	protected Text channelSelectionText;
	protected Text optionGaussian;
	protected Text optionMedian;
	protected Text optionConvolve;
	protected Text optionsMean;
	protected Text optionsMaximum;
	protected Text optionsMinimum;
	protected Button checkGaussianFilter;
	protected Button checkMedian;
	protected Button checkConvolve;
	protected Button checkMaximum;
	protected Button checkMean;
	protected Button checkMinimum;
	protected Button checkEdges;
	private CTabFolder tabFolder;
	private CTabItem tabItemFeatures;
	private Composite composite;
	private CTabItem tbtmMore;
	private Composite composite_1;
	private Button btnLoadConfiguration;
	private Button btnNewButton_4;
	protected Text txtTrainingRScript;
	private Button btnNewButton_5;
	private Button btnRClassificationScript;
	protected Text txtClassificationRScript;
	protected String pathTrainingScript;
	protected String pathClassificationScript;
	protected Button checkConvertToHsb;
	protected boolean toHsb;

	public ModelGui(Composite parent, Main model, int style) {
		super(parent, SWT.NONE);
		this.model = model;
		setLayout(new FillLayout(SWT.HORIZONTAL));

		tabFolder = new CTabFolder(this, SWT.BORDER);
		//tabFolder.setSelectionBackground(
				//Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

		tabItemFeatures = new CTabItem(tabFolder, SWT.NONE);
		tabItemFeatures.setText("Features");
		tabFolder.setSelection(tabItemFeatures);
		composite = new Composite(tabFolder, SWT.NONE);
		tabItemFeatures.setControl(composite);
		composite.setLayout(new GridLayout(2, true));
		
				Button btnNewButton = new Button(composite, SWT.NONE);
				btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				btnNewButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						model.executeSelection(1);
					}
				});
				btnNewButton.setText("Create Stack (1)");
		
				Button btnNewButton_1 = new Button(composite, SWT.NONE);
				btnNewButton_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				btnNewButton_1.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						model.executeSelection(2);
					}
				});
				btnNewButton_1.setText("Create Classes (2)");
		
				Button btnNewButton_2 = new Button(composite, SWT.NONE);
				btnNewButton_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				btnNewButton_2.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						model.executeSelection(3);
					}
				});
				btnNewButton_2.setText("Train Script (3)");
		
				Button btnNewButton_3 = new Button(composite, SWT.NONE);
				btnNewButton_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				btnNewButton_3.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						model.executeSelection(4);
					}
				});
				btnNewButton_3.setText("Classify Script (4)");
		
				btnLoadConfiguration = new Button(composite, SWT.NONE);
				btnLoadConfiguration.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				btnLoadConfiguration.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						new LoadAndSaveConfig(ModelGui.this).loadScript();
					}
				});
				btnLoadConfiguration.setText("Load Configuration");
		
				btnNewButton_4 = new Button(composite, SWT.NONE);
				btnNewButton_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
				btnNewButton_4.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						new LoadAndSaveConfig(ModelGui.this).saveScript();
					}
				});
				btnNewButton_4.setText("Save Configuration");

		Label lblSelectChannels = new Label(composite, SWT.NONE);
		GridData gd_lblSelectChannels = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
		gd_lblSelectChannels.widthHint = 279;
		lblSelectChannels.setLayoutData(gd_lblSelectChannels);
		lblSelectChannels.setText("Select Channels (1,2,... - Leave blank for all!)\r\n");

		channelSelectionText = new Text(composite, SWT.BORDER);
		channelSelectionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				
						checkConvertToHsb = new Button(composite, SWT.CHECK);
						checkConvertToHsb.setText("Convert to HSB Color Space");
		new Label(composite, SWT.NONE);

		Label lblFilter = new Label(composite, SWT.NONE);
		lblFilter.setText("Filter");

		Label lblOption = new Label(composite, SWT.NONE);
		lblOption.setText("Sigma");

		checkGaussianFilter = new Button(composite, SWT.CHECK);
		checkGaussianFilter.setText("Gaussian Blur");

		optionGaussian = new Text(composite, SWT.BORDER);
		optionGaussian.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		optionGaussian.setText("2");

		checkMedian = new Button(composite, SWT.CHECK);
		checkMedian.setText("Median");

		optionMedian = new Text(composite, SWT.BORDER);
		optionMedian.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		optionMedian.setText("2");

		checkMean = new Button(composite, SWT.CHECK);
		checkMean.setText("Mean");

		optionsMean = new Text(composite, SWT.BORDER);
		optionsMean.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		optionsMean.setText("2");

		checkMaximum = new Button(composite, SWT.CHECK);
		checkMaximum.setText("Maximum");

		optionsMaximum = new Text(composite, SWT.BORDER);
		optionsMaximum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		optionsMaximum.setText("2");

		checkMinimum = new Button(composite, SWT.CHECK);
		checkMinimum.setText("Minimum");

		optionsMinimum = new Text(composite, SWT.BORDER);
		optionsMinimum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		optionsMinimum.setText("2");

		checkEdges = new Button(composite, SWT.CHECK);
		checkEdges.setText("Edges");
		new Label(composite, SWT.NONE);

		checkConvolve = new Button(composite, SWT.CHECK);
		checkConvolve.setText("Convolve");
		new Label(composite, SWT.NONE);

		optionConvolve = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_optionConvolve = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_optionConvolve.widthHint = 248;
		optionConvolve.setLayoutData(gd_optionConvolve);
		optionConvolve.setText(
				"text1=[\n-1 -1 -1 -1 -1\n-1 -1 -1 -1 -1\n-1 -1 24 -1 -1\n-1 -1 -1 -1 -1\n-1 -1 -1 -1 -1\n] normalize");

		tbtmMore = new CTabItem(tabFolder, SWT.NONE);
		tbtmMore.setText("Settings");

		composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmMore.setControl(composite_1);
		composite_1.setLayout(new GridLayout(2, true));

		btnNewButton_5 = new Button(composite_1, SWT.NONE);
		btnNewButton_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = Bio7Dialog.openFile();
				path = path.replace("\\", "/");
				txtTrainingRScript.setText(path);
			}
		});
		GridData gd_btnNewButton_5 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_5.heightHint = 25;
		btnNewButton_5.setLayoutData(gd_btnNewButton_5);
		btnNewButton_5.setText("R Training Script");

		txtTrainingRScript = new Text(composite_1, SWT.BORDER);
		txtTrainingRScript.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		txtTrainingRScript.setText(FileRoot.getCurrentCompileDir() + "/../R/Train_RandomForest.R");
		btnRClassificationScript = new Button(composite_1, SWT.NONE);
		btnRClassificationScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = Bio7Dialog.openFile();
				path = path.replace("\\", "/");
				txtClassificationRScript.setText(path);
			}
		});
		GridData gd_btnRClassificationScript = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnRClassificationScript.heightHint = 25;
		btnRClassificationScript.setLayoutData(gd_btnRClassificationScript);
		btnRClassificationScript.setText("R Classification Script");

		txtClassificationRScript = new Text(composite_1, SWT.BORDER);
		txtClassificationRScript.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		txtClassificationRScript.setText(FileRoot.getCurrentCompileDir() + "/../R/Classify_RandomForest.R");

	}

	public void getFeatureOptions() {

		Display display = Util.getDisplay();

		display.syncExec(new Runnable() {

			public void run() {

				toHsb = checkConvertToHsb.getSelection();

				channelOption = channelSelectionText.getText();

				gaussian = checkGaussianFilter.getSelection();
				gaussianOption = optionGaussian.getText();

				median = checkMedian.getSelection();
				medianOption = optionMedian.getText();

				mean = checkMean.getSelection();
				meanOption = optionsMean.getText();

				maximum = checkMaximum.getSelection();
				maximumOption = optionsMaximum.getText();

				minimum = checkMinimum.getSelection();
				minimumOption = optionsMinimum.getText();

				edges = checkEdges.getSelection();

				convolve = checkConvolve.getSelection();
				convolveOption = optionConvolve.getText();

			}
		});

	}

	public String getPathTrainingRScript() {

		Display display = Util.getDisplay();

		display.syncExec(new Runnable() {

			public void run() {

				pathTrainingScript = txtTrainingRScript.getText();
			}
		});
		return pathTrainingScript;
	}

	public String getPathClassificationRScript() {
		Display display = Util.getDisplay();

		display.syncExec(new Runnable() {

			public void run() {

				pathClassificationScript = txtClassificationRScript.getText();
			}
		});
		return pathClassificationScript;
	}
	
	public void layout() {
		CanvasView canvasView = CanvasView.getCanvas_view();
		canvasView.updatePlotCanvas();
	}

}