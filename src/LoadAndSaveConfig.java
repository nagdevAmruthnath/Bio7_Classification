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

	public void loadScript() {

		String file = Bio7Dialog.openFile(new String[] { "*.txt", "*" });
		if (file != null) {
			File fil = new File(file);

			try {

				FileReader fileReader = new FileReader(fil);
				BufferedReader reader = new BufferedReader(fileReader);

				modelGui.channelSelectionText.setText(reader.readLine());

				modelGui.checkGaussianFilter.setSelection(Boolean.parseBoolean(reader.readLine()));
				modelGui.optionGaussian.setText(reader.readLine());

				modelGui.checkMedian.setSelection(Boolean.parseBoolean(reader.readLine()));
				modelGui.optionMedian.setText(reader.readLine());

				modelGui.checkMean.setSelection(Boolean.parseBoolean(reader.readLine()));
				modelGui.optionsMean.setText(reader.readLine());

				modelGui.checkMaximum.setSelection(Boolean.parseBoolean(reader.readLine()));
				modelGui.optionsMaximum.setText(reader.readLine());

				modelGui.checkMinimum.setSelection(Boolean.parseBoolean(reader.readLine()));
				modelGui.optionsMinimum.setText(reader.readLine());

				modelGui.checkEdges.setSelection(Boolean.parseBoolean(reader.readLine()));

				modelGui.checkConvolve.setSelection(Boolean.parseBoolean(reader.readLine()));
				modelGui.optionConvolve.setText(reader.readLine().replace("\\n", System.lineSeparator()));

				reader.close();

			} catch (IOException ex) {

			}

		}
	}

	public void saveScript() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(modelGui.channelSelectionText.getText());
		buffer.append(System.getProperty("line.separator"));

		buffer.append(modelGui.checkGaussianFilter.getSelection());
		buffer.append(System.getProperty("line.separator"));
		buffer.append(modelGui.optionGaussian.getText());
		buffer.append(System.getProperty("line.separator"));

		buffer.append(modelGui.checkMedian.getSelection());
		buffer.append(System.getProperty("line.separator"));
		buffer.append(modelGui.optionMedian.getText());
		buffer.append(System.getProperty("line.separator"));

		buffer.append(modelGui.checkMean.getSelection());
		buffer.append(System.getProperty("line.separator"));
		buffer.append(modelGui.optionsMean.getText());
		buffer.append(System.getProperty("line.separator"));

		buffer.append(modelGui.checkMaximum.getSelection());
		buffer.append(System.getProperty("line.separator"));
		buffer.append(modelGui.optionsMaximum.getText());
		buffer.append(System.getProperty("line.separator"));

		buffer.append(modelGui.checkMinimum.getSelection());
		buffer.append(System.getProperty("line.separator"));
		buffer.append(modelGui.optionsMinimum.getText());
		buffer.append(System.getProperty("line.separator"));

		buffer.append(modelGui.checkEdges.getSelection());
		buffer.append(System.getProperty("line.separator"));

		buffer.append(modelGui.checkConvolve.getSelection());
		buffer.append(System.getProperty("line.separator"));
		buffer.append(modelGui.optionConvolve.getText().replace(System.lineSeparator(), "\\n"));
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
