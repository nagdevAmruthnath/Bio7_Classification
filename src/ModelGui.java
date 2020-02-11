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

	public ModelGui(Composite parent, Main model, int style) {
		super(parent, style);
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

		Label lblSelectChannels = new Label(this, SWT.NONE);
		lblSelectChannels.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		lblSelectChannels.setText("Include Channels (1,2,...)\r\n");

		channelSelectionText = new Text(this, SWT.BORDER);
		channelSelectionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		lblNewLabel.setText("Features");

		Label lblFilter = new Label(this, SWT.NONE);
		lblFilter.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblFilter.setText("Filter");

		Label lblOption = new Label(this, SWT.NONE);
		lblOption.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblOption.setText("Option");

		checkGaussianFilter = new Button(this, SWT.CHECK);
		checkGaussianFilter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		checkGaussianFilter.setText("Gaussian Blur");

		optionGaussian = new Text(this, SWT.BORDER);
		optionGaussian.setText("radius=20");
		optionGaussian.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		checkMedian = new Button(this, SWT.CHECK);
		checkMedian.setText("Median");

		optionMedian = new Text(this, SWT.BORDER);
		optionMedian.setText("radius=10");
		optionMedian.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		checkConvolve = new Button(this, SWT.CHECK);
		checkConvolve.setText("Convolve");

		optionConvolve = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		optionConvolve.setText(
				"text1=[\r\n-1 -1 -1 -1 -1\r\n-1 -1 -1 -1 -1\r\n-1 -1 24 -1 -1\r\n-1 -1 -1 -1 -1\r\n-1 -1 -1 -1 -1\r\n] normalize");
		GridData gd_optionConvolve = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_optionConvolve.heightHint = 80;
		optionConvolve.setLayoutData(gd_optionConvolve);

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

				convolve = checkConvolve.getSelection();
				convolveOption = optionConvolve.getText();

			}
		});

	}

}