import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ModelGui extends Composite {

	private Main model;

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

	}

}