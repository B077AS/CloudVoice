package rooms.voiceRooms;

import javax.sound.sampled.*;
import de.maxhenkel.rnnoise4j.Denoiser;
import javafx.collections.ObservableMap;
import main.model.VPS;
import rooms.ChatRoom;
import settings.AudioDeviceSelector;
import user.UserSettings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AudioManager {

	private final int bufferSize=960;
	private AudioFormat audioFormat;
	private TargetDataLine targetDataLine;
	private SourceDataLine sourceDataLine;
	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private VAD vad;
	private ChatRoom room;
	private volatile boolean sendingLoop=true;
	private volatile boolean receivingLoop=true;
	private Denoiser noiseFilter;
	private UserSettings settings;
	private AudioDeviceSelector audioDeviceSelector;
	private final Object sendingLock = new Object();
	private final Object receivingLock = new Object();

	public AudioManager(ChatRoom room, UserSettings settings) {
		this.settings=settings;
		this.room=room;
		this.audioDeviceSelector=new AudioDeviceSelector();
		ObservableMap<String, TargetDataLine> inputDevices=audioDeviceSelector.getInputDevices();
		ObservableMap<String, SourceDataLine> outputDevices=audioDeviceSelector.getOutputDevices();
		try {
			noiseFilter = new Denoiser();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			vad=new VAD(48000.0f, 16, settings.getDbNum());
			audioFormat = getAudioFormat();

			requestConnect();

			outputStream = this.socket.getOutputStream();
			inputStream = this.socket.getInputStream();

			// microfono
			if(inputDevices.containsKey(settings.getPrefMic())) {
				targetDataLine=inputDevices.get(settings.getPrefMic());
				targetDataLine.open(audioFormat);
			}else {
				DataLine.Info dataLineMic = new DataLine.Info(TargetDataLine.class, audioFormat);
				targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineMic);
				targetDataLine.open(audioFormat);
			}
			// cuffie
			if(outputDevices.containsKey(settings.getPrefSpeaker())) {
				sourceDataLine=outputDevices.get(settings.getPrefSpeaker());
				sourceDataLine.open(audioFormat);
			}else {
				DataLine.Info dataLineHeadPhones = new DataLine.Info(SourceDataLine.class, audioFormat);
				sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineHeadPhones);
				sourceDataLine.open(audioFormat);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void changeMicrophone() {
	    stopSendingTransmission();

	    this.audioDeviceSelector = new AudioDeviceSelector();
	    ObservableMap<String, TargetDataLine> inputDevices = audioDeviceSelector.getInputDevices();

	    // Close the old TargetDataLine
	    if (targetDataLine != null) {
	        targetDataLine.stop();
	        targetDataLine.close();
	    }
	    // microfono
	    try {
	        if (inputDevices.containsKey(settings.getPrefMic())) {
	            targetDataLine = inputDevices.get(settings.getPrefMic());
	            targetDataLine.open(audioFormat);
	        } else {
	            DataLine.Info dataLineMic = new DataLine.Info(TargetDataLine.class, audioFormat);
	            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineMic);
	            targetDataLine.open(audioFormat);
	        }
	        sendingLoop=true;
	    } catch (LineUnavailableException e) {
	        e.printStackTrace();
	    }
	}
	
	public void changeSpeakers() {
	    stopAudioReception();

	    this.audioDeviceSelector = new AudioDeviceSelector();
	    ObservableMap<String, SourceDataLine> outputDevices=audioDeviceSelector.getOutputDevices();

	    if (sourceDataLine != null) {
			sourceDataLine.stop();
			sourceDataLine.close();
	    }
	    // microfono
	    try {
			if(outputDevices.containsKey(settings.getPrefSpeaker())) {
				sourceDataLine=outputDevices.get(settings.getPrefSpeaker());
				sourceDataLine.open(audioFormat);
			}else {
				DataLine.Info dataLineHeadPhones = new DataLine.Info(SourceDataLine.class, audioFormat);
				sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineHeadPhones);
				sourceDataLine.open(audioFormat);
			}
	        receivingLoop=true;
	        startAudioReception();
	    } catch (LineUnavailableException e) {
	        e.printStackTrace();
	    }
	}


	private byte[] removeNoise(byte[] audioData) {
		short[] shortArray = new short[audioData.length / 2];
		ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray);
		short[] denoisedAudio = noiseFilter.denoise(shortArray);
		byte[] denoisedByteArray = new byte[denoisedAudio.length * 2];
		ByteBuffer.wrap(denoisedByteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(denoisedAudio);
		return denoisedByteArray;
	}


	private AudioFormat getAudioFormat() {

		float sampleRate = 48000.0f;
		int sampleSizeInBits = 16;
		int channels = 1; // Cambia il numero di canali a 1 per mono
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	public void startAudioTransmission() {
		startMicrophoneTransmission();//microphone
		startAudioReception();//audio
	}

	public void startMicrophoneTransmission() {
		Thread captureThread = new Thread(this::captureAndSendAudio);
		captureThread.setDaemon(true);
		captureThread.start();
	}

	public void startAudioReception() {
		Thread playbackThread = new Thread(this::receiveAndPlayAudio);
		playbackThread.setDaemon(true);
		playbackThread.start();
	}


	private void captureAndSendAudio() {
		try {
			byte[] audioData = new byte[bufferSize];
			targetDataLine.start();

			while (true) {
				synchronized (sendingLock) {
					while (!sendingLoop) {
						try {
							sendingLock.wait();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							return;
						}
					}
				}

				targetDataLine.read(audioData, 0, audioData.length);

				if (vad.start(audioData) == false) {
					if (settings.isFilter()) {
						byte[] filteredAudioData = removeNoise(audioData);
						outputStream.write(filteredAudioData, 0, filteredAudioData.length);
					} else {
						outputStream.write(audioData, 0, audioData.length);
						//System.out.println("sent");
					}
				} else {
					Arrays.fill(audioData, (byte) 0);
					outputStream.write(audioData, 0, audioData.length);
				}
			}
		} catch (IllegalStateException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void receiveAndPlayAudio() {
		try {
			byte[] audioData = new byte[960];
			sourceDataLine.start();

			while (true) {
				synchronized (receivingLock) {
					while (!receivingLoop) {
						try {
							receivingLock.wait();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							return;
						}
					}
				}

				int bytesRead = inputStream.read(audioData, 0, audioData.length);

				if (bytesRead == -1) {
					break;
				}

				if (bytesRead > 0) {
					sourceDataLine.write(audioData, 0, bytesRead);
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public void requestConnect() {
		try {
			this.socket= new Socket(VPS.SERVER_ADDRESS, room.getPort());
			System.out.println("Request connection for port: "+room.getPort());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopAudioAndSocket() {
		
		stopSendingTransmission();
		stopAudioReception();

		noiseFilter.close();

		try {
			targetDataLine.stop();
			targetDataLine.close();

			sourceDataLine.stop();
			sourceDataLine.close();

			outputStream.close();
			inputStream.close();

			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pauseMicrophoneTransmission() {
		sendingLoop = false;
	}

	public void resumeMicrophoneTransmission() {
		synchronized (sendingLock) {
			sendingLoop = true;
			sendingLock.notify();
		}
	}

	public void pauseAudioReception() {
		receivingLoop = false;
	}

	public void resumeAudioReception() {
		synchronized (receivingLock) {
			receivingLoop = true;
			receivingLock.notify();
		}
	}

	public void stopSendingTransmission() {
		sendingLoop = false;
		synchronized (sendingLock) {
			sendingLock.notify();
		}
	}

	public void stopAudioReception() {
		receivingLoop = false;
		synchronized (receivingLock) {
			receivingLock.notify();
		}
	}

	public VAD getVad() {
		return vad;
	}

	public void setVad(VAD vad) {
		this.vad = vad;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Denoiser getNoiseFilter() {
		return noiseFilter;
	}

	public void setNoiseFilter(Denoiser noiseFilter) {
		this.noiseFilter = noiseFilter;
	}

	public UserSettings getSettings() {
		return settings;
	}

	public void setSettings(UserSettings settings) {
		this.settings = settings;
	}

	public TargetDataLine getTargetDataLine() {
		return targetDataLine;
	}

	public void setTargetDataLine(TargetDataLine targetDataLine) {
		this.targetDataLine = targetDataLine;
	}

	public SourceDataLine getSourceDataLine() {
		return sourceDataLine;
	}

	public void setSourceDataLine(SourceDataLine sourceDataLine) {
		this.sourceDataLine = sourceDataLine;
	}

	public ChatRoom getRoom() {
		return room;
	}

	public void setRoom(ChatRoom room) {
		this.room = room;
	}

	public boolean isSendingLoop() {
		return sendingLoop;
	}

	public void setSendingLoop(boolean sendingLoop) {
		this.sendingLoop = sendingLoop;
	}
}