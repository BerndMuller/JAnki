package de.pdbm.janki.ble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothManager;

/**
 * Class Vehicle represent a Anki Overdrive vehicle.
 * 
 * @author bernd
 *
 */
public class Vehicle {

	private static final Map<Vehicle, Long> vehicles = new ConcurrentHashMap<>();

	private BluetoothDevice bluetoothDevice; // device representing this vehicle

	private BluetoothGattCharacteristic readCharacteristic;

	private BluetoothGattCharacteristic writeCharacteristic;

	private List<NotificationListener> listeners;

	static {
		AnkiBle.sleep(0); // dummy to initialize AnkiBle class
	}


	private Vehicle(BluetoothDevice bluetoothDevice) {
		listeners = new ArrayList<>();
		this.bluetoothDevice = bluetoothDevice;
	}

	/**
	 * Initializes JAnki.
	 * 
	 * You have to call init() before doing something else.
	 * 
	 */
	public static void init() {
		// dummy to run static initializer
	}

	
	/**
	 * Returns a list of all known vehicles.
	 * 
	 * Eventually not all vehicles are connected to the BLE device.
	 * 
	 * @return list of known vehicles
	 */
	public static List<Vehicle> getVehicles() {
		return new ArrayList<>(vehicles.keySet());
	}

	
	/**
	 * Returns the vehicle for this MAC address.
	 * 
	 * @param mac
	 * @return the vehicle
	 */
	public static Vehicle get(String mac) {
		Set<Vehicle> tmp = vehicles.keySet();
		Optional<Vehicle> any = tmp.stream().filter(v -> v.getMacAddress().equals(mac)).findAny();
		return any.get();
	}

	
	/**
	 * Sets the speed of this vehicle.
	 * 
	 * @param speed, usually between 0 and 1000
	 */
	public void setSpeed(int speed) {
		if (bluetoothDevice.getConnected()) {
			writeCharacteristic.writeValue(Message.getSdkMode());
			// TODO check if this is needed
			AnkiBle.sleep(100);
			writeCharacteristic.writeValue(Message.getSpeed((short) speed));
		} else {
			System.out.println("not connected");
		}
	}

	/**
	 * Add a {@link NotificationListener}.
	 * 
	 * @param listener
	 */
	public void addNotificationListener(NotificationListener listener) {
		listeners.add(listener);
	}

	public String getMacAddress() {
		return bluetoothDevice.getAddress();
	}

	
	public static void toggleAllLogs() {
		Arrays.stream(LogType.values()).forEach(t -> toggleLog(t));
	}

	
	
	public static void toggleLog(LogType toggle) {
		AnkiBle.toggleLog(toggle);
	}

	@Override
	public int hashCode() {
		return bluetoothDevice.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vehicle && ((Vehicle) obj).bluetoothDevice.equals(this.bluetoothDevice);
	}

	@Override
	public String toString() {
		return "Vehicle " + bluetoothDevice.getAddress() + ", read " + (readCharacteristic == null ? "-" : "\u2718") 
				+ ", write " + (writeCharacteristic == null ? "-" : "\u2718") + ", listeners =" + listeners;
	}

	/**
	 * Methode wird vom BLE-System für jede Value-Notification aufgerufen.
	 * 
	 * @param bytes The BLE message bytes
	 */
	private void onValueNotification(byte[] bytes) {
		AnkiBle.log(LogType.VALUE_NOTIFICATION, "Value notification: " + Arrays.toString(bytes));
		Notification notification = NotificationParser.parse(this, bytes);
		if (notification instanceof PositionUpdate) {
			PositionUpdate pu = (PositionUpdate) notification;
			for (NotificationListener notificationListener : listeners) {
				if (notificationListener instanceof PositionUpdateListener) {
					((PositionUpdateListener) notificationListener).onPositionUpdate(pu);
				}
			}
		}
	}

	
	
	/**
	 * Class hiding the BLE stuff.
	 * <p>
	 * 
	 * Genuine documentation of the Anki C implementation is
	 * <a href="https://anki.github.io/drive-sdk/docs/programming-guide">Ankis Programming Guide</a>. 
	 * As locale PDF: <a href="./doc-files/anki-programming-guide.pdf">Ankis Programming Guide</a>
	 * <p>
	 * 
	 * TinyB documentation:  
	 * <a href="http://iotdk.intel.com/docs/master/tinyb/java/annotated.html">TinyB Class List</a>
	 * <p>
	 * 
	 * device.getBluetoothType() gibt Fehler:
	 * https://github.com/intel-iot-devkit/tinyb/issues/69
	 * 
	 * 
	 * @author bernd
	 *
	 */
	private static class AnkiBle {

		private static final String ANKI_SERVICE_UUID = "BE15BEEF-6186-407E-8381-0BD89C4D8DF4";
		private static final String ANKI_READ_CHARACTERISTIC_UUID = "BE15BEE0-6186-407E-8381-0BD89C4D8DF4";
		private static final String ANKI_WRITE_CHARACTERISTIC_UUID = "BE15BEE1-6186-407E-8381-0BD89C4D8DF4";

		private static final ScheduledExecutorService scheduler;

		@SuppressWarnings("unused")
		private static final ScheduledFuture<?> discoveryScheduler;

		@SuppressWarnings("unused")
		private static final ScheduledFuture<?> initializationScheduler;

		private static final Map<LogType, Boolean> logToggles = new ConcurrentHashMap<>();

		static {
			System.out.println("JAnki gestartet");
			scheduler = Executors.newScheduledThreadPool(1);
			discoveryScheduler = scheduler.scheduleAtFixedRate(new AnkiBle().new DeviceDiscoverThread(), 0, 10, TimeUnit.SECONDS);
			initializationScheduler = scheduler.scheduleAtFixedRate(new AnkiBle().new DeviceInitializationThread(), 5, 10, TimeUnit.SECONDS);
			logToggles.put(LogType.DEVICE_DISCOVERY, Boolean.FALSE);
			logToggles.put(LogType.DEVICE_INITIALIZATION, Boolean.FALSE);
			logToggles.put(LogType.VALUE_NOTIFICATION, Boolean.FALSE);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {AnkiBle.disconnectAll();}));
		}

		/*
		 * Lock für Tipp aus Tinyb-Dokumentation: Do not attempt to perform multiple connections simultaneously. Instead, serialize all
		 * connection attempts, so that connection, service discovery and characteristic discovery for one peripheral are completed before
		 * attempting to establish another connection.
		 * 
		 */
		private static ReentrantLock lock = new ReentrantLock();

		/**
		 * Liefert die Write-Characteristics eines Autos.
		 * 
		 * @param device device to get write characteristics for
		 * @return the write characteristics of the device
		 */
		static BluetoothGattCharacteristic writeCharacteristicFor(BluetoothDevice device) {
			return characteristicFor(ANKI_WRITE_CHARACTERISTIC_UUID, device);
		}

		/**
		 * Disconnect all Bluetooth devices.
		 * 
		 */
		private static void disconnectAll() {
			System.out.println("Disconnect all devices");
			Set<Entry<Vehicle, Long>> vehicles = Vehicle.vehicles.entrySet();
			for (Entry<Vehicle, Long> entry : vehicles) {
				Vehicle vehicle = entry.getKey();
				vehicle.bluetoothDevice.disconnect();
			}
		}

		/**
		 * Liefert die Read-Characteristics eines Autos.
		 * 
		 * @param device device to get read characteristics for
		 * @return the read characteristics of the device
		 */
		static BluetoothGattCharacteristic readCharacteristicFor(BluetoothDevice device) {
			return characteristicFor(ANKI_READ_CHARACTERISTIC_UUID, device);
		}

		/**
		 * Liefert Read- oder Write-Characteristic eines Autos.
		 * 
		 * @param characteristicUUID UUID of read or write characteristic
		 * @param device the device for which the characteristic is set 
		 * @return the characteristic
		 */
		private static BluetoothGattCharacteristic characteristicFor(String characteristicUUID, BluetoothDevice device) {
			// direktes Lesen funktioniert nicht
			// BluetoothGattService service = device.find(ANKI_SERVICE_UUID);
			// BluetoothGattCharacteristic write = service.find(ANKI_WRITE_UUID);
			// also iterieren

			BluetoothGattCharacteristic readOrWriteCharacteristic = null;
			lock.lock();
			try {
				for (int i = 0; i < 10; i++) {
					boolean connected = device.connect();
					if (connected) {
						List<BluetoothGattService> services = device.getServices();
						for (BluetoothGattService service : services) {
							List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
							for (BluetoothGattCharacteristic characteristic : characteristics) {
								if (characteristic.getUUID().toUpperCase().equals(characteristicUUID)) {
									readOrWriteCharacteristic = characteristic;
								}
							}
						}
						if (readOrWriteCharacteristic != null) {
							return readOrWriteCharacteristic;
						}
					} else {
						System.out.println("connection failed");
					}
					sleep(100);
				}
			} finally {
				lock.unlock();
			}
			return readOrWriteCharacteristic;
		}

		private static void sleep(long millis) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public static void toggleLog(LogType toggle) {
			logToggles.put(toggle, !logToggles.get(toggle));
		}

		private static void log(LogType toggle, String text) {
			if (logToggles.get(toggle)) {
				System.out.println("AnkiBle: " + text);
			}
		}

		class DeviceDiscoverThread extends Thread {

			public void run() {
				BluetoothManager manager = BluetoothManager.getBluetoothManager();
				log(LogType.DEVICE_DISCOVERY, "discovering bluetooth devices ...");
				lock.lock();
				try {
					manager.startDiscovery();
					List<BluetoothDevice> list = manager.getDevices();
					for (BluetoothDevice device : list) {
						if (Arrays.asList(device.getUUIDs()).contains(ANKI_SERVICE_UUID.toLowerCase())) {
							if (Vehicle.getVehicles().stream().map(v -> v.bluetoothDevice.getAddress()).anyMatch(mac -> mac.equals(device.getAddress()))) {
								log(LogType.DEVICE_DISCOVERY, "vehicle " + device.getAddress() + " already known");
								Vehicle.vehicles.replace(Vehicle.get(device.getAddress()), System.nanoTime());
							} else {
								Vehicle.vehicles.put(new Vehicle(device), System.nanoTime());
								log(LogType.DEVICE_DISCOVERY, "vehicle " + device.getAddress() + " added");
							}
						}
					}
					Thread.sleep(1000);
					manager.stopDiscovery();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
				log(LogType.DEVICE_DISCOVERY, "discovering bluetooth devices finished");
			}
		}

		/**
		 * Versucht alle Devices zu initialisieren.
		 * <p>
		 * Dazu gehört:
		 * <ul>
		 * 	<li> Read-Characteristic zu setzen
		 * 	<li> Write-Characteristic zu setzen
		 * 	<li> Value Notification registrieren
		 * </ul>
		 * 
		 * Versucht Read- und Write-Characteristic der Devices zu setzen und Value-Notification zu registrieren.
		 * 
		 */
		class DeviceInitializationThread extends Thread {

			public void run() {
				log(LogType.DEVICE_INITIALIZATION, "initializing bluetooth devices ...");
				Set<Entry<Vehicle, Long>> vehicles = Vehicle.vehicles.entrySet();
				for (Entry<Vehicle, Long> entry : vehicles) {
					Vehicle vehicle = entry.getKey();
					if (vehicle.writeCharacteristic == null) {
						vehicle.writeCharacteristic = writeCharacteristicFor(vehicle.bluetoothDevice);
						log(LogType.DEVICE_INITIALIZATION, "Write-Characteristic for " + vehicle + (vehicle == null ? " not " : "") + " set");

					}
					if (vehicle.readCharacteristic == null) {
						vehicle.readCharacteristic = readCharacteristicFor(vehicle.bluetoothDevice);
						log(LogType.DEVICE_INITIALIZATION, "Read-Characteristic for " + vehicle + (vehicle == null ? " not " : ""));
						if (vehicle.readCharacteristic != null) {
							vehicle.readCharacteristic.enableValueNotifications(bytes -> {
								vehicle.onValueNotification(bytes);
							});
							log(LogType.DEVICE_INITIALIZATION, "Value notifications set for " + vehicle);
						}
					}
				}
				log(LogType.DEVICE_INITIALIZATION, "initializing bluetooth devices finished");
			}
		}

		
		@SuppressWarnings("unused")
		class CarRemovalThread extends Thread {

			private static final long DEVICE_NOT_SEEN_NANO = 100_000_000_000L;

			
			// TODO muss noch gemacht werden, noch nicht getestet
			public void run() {
				System.out.println("Removal thread running");
					for (Entry<Vehicle, Long> entry : Vehicle.vehicles.entrySet()) {
						if (System.nanoTime() - entry.getValue() > DEVICE_NOT_SEEN_NANO) {
							// entfernen, falls 10 Such-Intervalle nicht da
							System.out.println("entfernt: " + entry.getKey());
							//Vehicle.vehicles.remove(entry.getKey());
						}
					}
			}
		}
		
		

	}

	/**
	 * Enum to describe the log type.
	 * 
	 * @author bernd
	 *
	 */
	public enum LogType {
		DEVICE_DISCOVERY, DEVICE_INITIALIZATION, VALUE_NOTIFICATION;
	}
}
