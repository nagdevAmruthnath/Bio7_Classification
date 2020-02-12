import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.eco.bio7.image.Util;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class ModelGui extends Composite {

	private Main model;
	private Text channelSelectionText;
	private Text optionGaussian;
	private Text optionMedian;
	private Text optionConvolve;
	private Button checkGaussianFilter;
	private Button checkMedian;
	private Button checkConvolve;
	public boolean convolve;
	public boolean gaussian;
	public boolean median;
	public String convolveOption;
	public String medianOption;
	public String channelOption;
	public String gaussianOption;
	private Text optionsMean;
	private Text optionsMaximum;
	private Text optionsMinimum;
	private Button checkMaximum;
	private Button checkMean;
	private Button checkMinimum;
	protected boolean mean;
	protected String meanOption;
	protected boolean maximum;
	protected String maximumOption;
	protected boolean minimum;
	protected String minimumOption;
	private CTabFolder tabFolder;
	private CTabItem tabItemFeatures;
	private Composite composite;
	private CTabItem tbtmMore;
	private Composite composite_1;
	private Button checkEdges;
	protected boolean edges;

	public ModelGui(Composite parent, Main model, int style) {
		super(parent, SWT.NONE);
		this.model = model;
		setLayout(new GridLayout(2, true));

		Button btnNewButton = new Button(this, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.executeSelection(1);
			}
		});
		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_btnNewButton.heightHint = 30;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.setText("Create Stack (1)");

		Button btnNewButton_1 = new Button(this, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.executeSelection(2);
			}
		});
		GridData gd_btnNewButton_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_1.heightHint = 30;
		btnNewButton_1.setLayoutData(gd_btnNewButton_1);
		btnNewButton_1.setText("Create Classes (2)");

		Button btnNewButton_2 = new Button(this, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.executeSelection(3);
			}
		});
		GridData gd_btnNewButton_2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_2.heightHint = 30;
		btnNewButton_2.setLayoutData(gd_btnNewButton_2);
		btnNewButton_2.setText("Train Script (3)");

		Button btnNewButton_3 = new Button(this, SWT.NONE);
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.executeSelection(4);
			}
		});
		GridData gd_btnNewButton_3 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_3.heightHint = 30;
		btnNewButton_3.setLayoutData(gd_btnNewButton_3);
		btnNewButton_3.setText("Classify Script (4)");

		tabFolder = new CTabFolder(this, SWT.BORDER);
		GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_tabFolder.heightHint = 349;
		tabFolder.setLayoutData(gd_tabFolder);
		tabFolder.setSelectionBackground(
				Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

		tabItemFeatures = new CTabItem(tabFolder, SWT.NONE);
		tabItemFeatures.setText("Features");
		tabFolder.setSelection(tabItemFeatures);
		composite = new Composite(tabFolder, SWT.NONE);
		tabItemFeatures.setControl(composite);
		composite.setLayout(new GridLayout(2, true));

		Label lblSelectChannels = new Label(composite, SWT.NONE);
		GridData gd_lblSelectChannels = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
		gd_lblSelectChannels.widthHint = 279;
		lblSelectChannels.setLayoutData(gd_lblSelectChannels);
		lblSelectChannels.setText("Select Channels (1,2,... - Leave blank for all!)\r\n");

		channelSelectionText = new Text(composite, SWT.BORDER);
		channelSelectionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblFilter = new Label(composite, SWT.NONE);
		lblFilter.setText("Filter");

		Label lblOption = new Label(composite, SWT.NONE);
		lblOption.setText("Option");

		checkGaussianFilter = new Button(composite, SWT.CHECK);
		checkGaussianFilter.setText("Gaussian Blur");

		optionGaussian = new Text(composite, SWT.BORDER);
		optionGaussian.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		optionGaussian.setText("radius=2");

		checkMedian = new Button(composite, SWT.CHECK);
		checkMedian.setText("Median");

		optionMedian = new Text(composite, SWT.BORDER);
		optionMedian.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		optionMedian.setText("radius=2");

		checkMean = new Button(composite, SWT.CHECK);
		checkMean.setText("Mean");

		optionsMean = new Text(composite, SWT.BORDER);
		optionsMean.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		optionsMean.setText("radius=2");

		checkMaximum = new Button(composite, SWT.CHECK);
		checkMaximum.setText("Maximum");

		optionsMaximum = new Text(composite, SWT.BORDER);
		optionsMaximum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		optionsMaximum.setText("radius=2");

		checkMinimum = new Button(composite, SWT.CHECK);
		checkMinimum.setText("Minimum");

		optionsMinimum = new Text(composite, SWT.BORDER);
		optionsMinimum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		optionsMinimum.setText("radius=2");

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
				"text1=[\r\n-1 -1 -1 -1 -1\r\n-1 -1 -1 -1 -1\r\n-1 -1 24 -1 -1\r\n-1 -1 -1 -1 -1\r\n-1 -1 -1 -1 -1\r\n] normalize");

		tbtmMore = new CTabItem(tabFolder, SWT.NONE);
		tbtmMore.setText("More");

		composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmMore.setControl(composite_1);
		composite_1.setLayout(new GridLayout(2, true));

	}

	public void getFeatureOptions() {

		Display display = Util.getDisplay();

		display.syncExec(new Runnable() {

			public void run() {

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

}