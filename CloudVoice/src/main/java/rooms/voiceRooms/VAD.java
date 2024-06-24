package rooms.voiceRooms;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;

public class VAD {

    private float sampleRate;
    private int sampleSizeInBits;
    private double db;
    private final double defaultDB=-40.0;

    public VAD(float sampleRate, int sampleSizeInBits, double db) {
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        if(db!=0.0) {
        	this.db=db;
        }else {
        	this.db=defaultDB;
        }
    }


    public boolean start(byte[] audioData) {
        TarsosDSPAudioFormat tdspFormat = new TarsosDSPAudioFormat(sampleRate, sampleSizeInBits, 1, true, false);

        // Calcola la dimensione dell'array di float in base alla dimensione dei frame
        float[] voiceFloatArr = new float[audioData.length / tdspFormat.getFrameSize()];

        // Converte i dati audio in formato float
        TarsosDSPAudioFloatConverter audioFloatConverter = TarsosDSPAudioFloatConverter.getConverter(tdspFormat);
        audioFloatConverter.toFloatArray(audioData, voiceFloatArr);

        SilenceDetector silenceDetector = new SilenceDetector(db, true);
        return silenceDetector.isSilence(voiceFloatArr);
    }

	public double getDb() {
		return db;
	}

	public void setDb(double db) {
		this.db = db;
	}
    
}
