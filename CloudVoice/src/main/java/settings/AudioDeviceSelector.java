package settings;


import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sound.sampled.*;


public class AudioDeviceSelector{

	Map<String, TargetDataLine> inputDevicesMap = new LinkedHashMap<>();
	private ObservableMap<String, TargetDataLine> inputDevices = FXCollections.observableMap(inputDevicesMap);

	Map<String, SourceDataLine> outputDevicesMap = new LinkedHashMap<>();
	private ObservableMap<String, SourceDataLine> outputDevices = FXCollections.observableMap(outputDevicesMap);


	public AudioDeviceSelector() {

		populateAudioDevicesLists();
	}


	public void populateAudioDevicesLists() {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info info : mixerInfos) {
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info[] lineInfos = mixer.getSourceLineInfo(); // Per ottenere le informazioni sugli speaker

			for (Line.Info lineInfo : lineInfos) {

				try {
					SourceDataLine realSpeaker=(SourceDataLine)mixer.getLine(lineInfo);
					outputDevices.put(info.getName(), realSpeaker);
				} catch (Exception e) {
					//non speaker valido
				}
			}

			lineInfos = mixer.getTargetLineInfo(); // Per ottenere le informazioni sui microfoni
			for (Line.Info lineInfo : lineInfos) {

				try {
					TargetDataLine realMicrophone=(TargetDataLine)mixer.getLine(lineInfo);
					inputDevices.put(info.getName(), realMicrophone);
				} catch (Exception e) {
					//non microfono valido
				}

			}
		}
	}


	public ObservableMap<String, TargetDataLine> getInputDevices() {
		return inputDevices;
	}


	public void setInputDevices(ObservableMap<String, TargetDataLine> inputDevices) {
		this.inputDevices = inputDevices;
	}


	public ObservableMap<String, SourceDataLine> getOutputDevices() {
		return outputDevices;
	}


	public void setOutputDevices(ObservableMap<String, SourceDataLine> outputDevices) {
		this.outputDevices = outputDevices;
	}

}