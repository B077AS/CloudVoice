package settings;

import javafx.scene.control.Slider;
import main.model.MainWindowModel;
import user.UserDB;
import user.UserSettings;

public class VoiceActivationSlider extends Slider{

	private MainWindowModel model;
	private UserSettings settings;
	private static final double DEFAULT_DB=-40.0;
	private static final double MIN_VALUE=-70.0;
	private static final double MAX_VALUE=0.0;
	private UserDB userDB;

	public VoiceActivationSlider(MainWindowModel model) {
		this.model=model;
		this.setMin(MIN_VALUE);
		this.setMax(MAX_VALUE);
		this.setMinWidth(600);	
		this.setMinorTickCount(4); // Imposta il numero di tacche minori a 4 (mostrerà valori ogni 5)
		this.setMajorTickUnit(5.0); // Imposta il passo delle tacche principali a 5
		this.setBlockIncrement(1);
		this.setSnapToTicks(true);
		this.setShowTickMarks(true);
		this.setShowTickLabels(true);

		userDB=new UserDB();
		settings=userDB.fetchSettings(model.getUser());
		if(settings.getDbNum()!=0.0) {
			this.setValue(settings.getDbNum());
		}else {
			this.setValue(DEFAULT_DB);
		}
	}

	public void saveVoiceActivationSliderValue() {
		double selectedValue = this.getValue();
		try {
			model.getAudioManager().getVad().setDb(selectedValue);
			model.getUser().getSettings().setDbNum(selectedValue);
			userDB.updateDbNum(selectedValue, model.getUser());
		}catch(Exception e){
			model.getUser().getSettings().setDbNum(selectedValue);
			userDB.updateDbNum(selectedValue, model.getUser());
		}
	}

}
