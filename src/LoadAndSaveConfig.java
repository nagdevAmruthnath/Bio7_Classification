import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.eco.bio7.batch.Bio7Dialog;

public class LoadAndSaveConfig {
	private ModelGui modelGui;

	public LoadAndSaveConfig(ModelGui modelGui) {
		this.modelGui = modelGui;
	}

	public boolean read(BufferedReader reader) {
		boolean value = false;
		try {
			value = Boolean.parseBoolean(reader.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	public void loadScript() {

		String file = Bio7Dialog.openFile(new String[] { "*.txt", "*" });
		if (file != null) {
			File fil = new File(file);

			try {

				FileReader fileReader = new FileReader(fil);
				BufferedReader reader = new BufferedReader(fileReader);

				modelGui.checkConvertToHsb.setSelection(read(reader));

				modelGui.channelSelectionText.setText(reader.readLine());

				modelGui.checkGaussianFilter.setSelection(read(reader));
				modelGui.optionGaussian.setText(reader.readLine());

				modelGui.checkMedian.setSelection(read(reader));
				modelGui.optionMedian.setText(reader.readLine());

				modelGui.checkMean.setSelection(read(reader));
				modelGui.optionsMean.setText(reader.readLine());

				modelGui.checkMaximum.setSelection(read(reader));
				modelGui.optionsMaximum.setText(reader.readLine());

				modelGui.checkMinimum.setSelection(read(reader));
				modelGui.optionsMinimum.setText(reader.readLine());
				
				modelGui.checkGradientHessian.setSelection(read(reader));
				modelGui.optionGradientHessian.setText(reader.readLine());
				
				modelGui.checkLaplacian.setSelection(read(reader));
				modelGui.optionLaplacian.setText(reader.readLine());

				modelGui.checkEdges.setSelection(read(reader));

				modelGui.checkConvolve.setSelection(read(reader));
				modelGui.optionConvolve.setText(reader.readLine().replace("\\n", System.lineSeparator()));

				modelGui.txtTrainingRScript.setText(reader.readLine());

				modelGui.txtClassificationRScript.setText(reader.readLine());

				reader.close();

			} catch (IOException ex) {

			}

		}
	}

	public void saveScript() {
		StringBuffer buffer = new StringBuffer();
		String sep = System.getProperty("line.separator");
		// buffer.append(sep);
		buffer.append(modelGui.checkConvertToHsb.getSelection());
		buffer.append(sep);

		buffer.append(modelGui.channelSelectionText.getText());
		buffer.append(sep);
		buffer.append(modelGui.checkGaussianFilter.getSelection());
		buffer.append(sep);
		buffer.append(modelGui.optionGaussian.getText());
		buffer.append(sep);

		buffer.append(modelGui.checkMedian.getSelection());
		buffer.append(sep);
		buffer.append(modelGui.optionMedian.getText());
		buffer.append(sep);

		buffer.append(modelGui.checkMean.getSelection());
		buffer.append(sep);
		buffer.append(modelGui.optionsMean.getText());
		buffer.append(sep);

		buffer.append(modelGui.checkMaximum.getSelection());
		buffer.append(sep);
		buffer.append(modelGui.optionsMaximum.getText());
		buffer.append(sep);

		buffer.append(modelGui.checkMinimum.getSelection());
		buffer.append(sep);
		buffer.append(modelGui.optionsMinimum.getText());
		buffer.append(sep);
		
		buffer.append(modelGui.checkGradientHessian.getSelection());
		buffer.append(sep);
		buffer.append(modelGui.optionGradientHessian.getText());
		buffer.append(sep);
		
		buffer.append(modelGui.checkLaplacian.getSelection());
		buffer.append(sep);
		buffer.append(modelGui.optionLaplacian.getText());
		buffer.append(sep);

		buffer.append(modelGui.checkEdges.getSelection());
		buffer.append(sep);

		buffer.append(modelGui.checkConvolve.getSelection());
		buffer.append(sep);
		buffer.append(modelGui.optionConvolve.getText().replace(System.lineSeparator(), "\\n"));
		buffer.append(sep);
		buffer.append(modelGui.getPathTrainingScript());
		buffer.append(sep);
		buffer.append(modelGui.getPathClassificationScript());
		buffer.append(sep);
		// buffer.append(System.getProperty("line.separator"));

		String file = Bio7Dialog.saveFile("*.txt");
		if (file != null) {
			File fil = new File(file);
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(fil);

				BufferedWriter buffWriter = new BufferedWriter(fileWriter);

				String write = buffer.toString();
				buffWriter.write(write, 0, write.length());
				buffWriter.close();
			} catch (IOException ex) {

			} finally {
				try {
					fileWriter.close();
				} catch (IOException ex) {

				}
			}
		}

	}

}
