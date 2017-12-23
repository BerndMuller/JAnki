package de.pdbm.janki.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.pdbm.janki.core.notifications.ChargerInfoNotification;
import de.pdbm.janki.core.notifications.ChargerInfoNotificationListener;
import de.pdbm.janki.core.notifications.ConnectedNotification;
import de.pdbm.janki.core.notifications.ConnectedNotificationListener;
import de.pdbm.janki.core.notifications.DefaultNotification;
import de.pdbm.janki.core.notifications.ManufacturerData;
import de.pdbm.janki.core.notifications.Message;
import de.pdbm.janki.core.notifications.Notification;
import de.pdbm.janki.core.notifications.NotificationListener;
import de.pdbm.janki.core.notifications.NotificationParser;
import de.pdbm.janki.core.notifications.PositionUpdate;
import de.pdbm.janki.core.notifications.PositionUpdateListener;
import de.pdbm.janki.core.notifications.TransitionUpdate;
import de.pdbm.janki.core.notifications.TransitionUpdateListener;
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

	private Collection<NotificationListener> listeners;

	private int speed;

	private boolean connected;

	private boolean onCharger;

	private Optional<Model> model;

	static {
		AnkiBle.init();
	}

	private Vehicle(BluetoothDevice bluetoothDevice) {
		this.listeners = new ConcurrentLinkedDeque<>();
		this.bluetoothDevice = bluetoothDevice;
		this.addNotificationListener(new DefaultConnectedNotificationListener());
		this.addNotificationListener(new DefaultChargerInfoNotificationListener());
		model = ManufacturerData.modelFor(bluetoothDevice);
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
	 * @param mac vehicle's MAC address
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
			writeCharacteristic.writeValue(Message.speedMessage((short) speed));
			this.speed = speed;
		} else {
			System.out.println("not connected");
		}
	}

	public void changeLane(float offset) {
		if (bluetoothDevice.getConnected()) {
			writeCharacteristic.writeValue(Message.setOffsetFromRoadCenter()); // kalibrieren
			writeCharacteristic.writeValue(Message.changeLaneMessage((short) 1000, (short) 1000, offset));
		} else {
			System.out.println("not connected");
		}
	}

	/**
	 * Returns true, if and only if, the vehicle is connected.
	 * 
	 * @return true, if vehicle is connected, otherwise false 
	 */
	public boolean isConnected() {
		return connected;
	}

	public boolean isOnCharger() {
		return onCharger;
	}

	/**
	 * A vehicle is ready to start, if it
	 * <ul>
	 *   <li>is connected</li>
	 *   <li>and not on charger</li>
	 *   <li>and write characteristic is set</li>
	 *   <li>and read characteristic is set</li>
	 * </ul>
	 *  
	 * @return true, if vehicle ready to start, false otherwise
	 */
	public boolean isReadyToStart() {
		return connected && !onCharger && writeCharacteristic != null && readCharacteristic != null;
	}
	
	/**
	 * Add a {@link NotificationListener}.
	 * 
	 * @param listener the listener to add
	 */
	public void addNotificationListener(NotificationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a {@link NotificationListener}.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeNotificationListener(NotificationListener listener) {
		listeners.remove(listener);
	}

	public String getMacAddress() {
		return bluetoothDevice.getAddress();
	}

	public Optional<Model> getModel() {
		return model;
	}

	public int getSpeed() {
		return speed;
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
		// @formatter:off
		return toShortString()
				+ ", connected " + (connected ? "\u2718" : "-") 
				+ ", speed " + speed
				+ ", on charger " + (onCharger ? "\u2718" : "-") 
				+ ", read " + (readCharacteristic == null ? "-" : "\u2718")
				+ ", write " + (writeCharacteristic == null ? "-" : "\u2718") 
				+ ", listeners =" + listeners.stream().map(l -> l.getClass().getSimpleName()).map(Vehicle::upperCaseChars).collect(Collectors.toList());
		// @formatter:on
	}

	public String toShortString() {
		return (model.isPresent() ? model.get().toString() + "(" : "Vehicle(") + bluetoothDevice.getAddress() + ")";

	}

	/**
	 * Method called by BLE system for value notifications.
	 * 
	 * @param bytes The BLE message bytes
	 */
	private void onValueNotification(byte[] bytes) {
		Logger.log(LogType.VALUE_NOTIFICATION, "Value notification: " + Arrays.toString(bytes));

		try {
			Notification notification = NotificationParser.parse(this, bytes);
			if (notification instanceof PositionUpdate) {
				PositionUpdate pu = (PositionUpdate) notification;
				for (NotificationListener notificationListener : listeners) {
					if (notificationListener instanceof PositionUpdateListener) {
						((PositionUpdateListener) notificationListener).onPositionUpdate(pu);
					}
				}
			} else if (notification instanceof TransitionUpdate) {
				TransitionUpdate tu = (TransitionUpdate) notification;
				for (NotificationListener notificationListener : listeners) {
					if (notificationListener instanceof TransitionUpdateListener) {
						((TransitionUpdateListener) notificationListener).onTransitionUpdate(tu);
					}
				}
			} else if (notification instanceof ChargerInfoNotification) {
				ChargerInfoNotification cin = (ChargerInfoNotification) notification;
				for (NotificationListener notificationListener : listeners) {
					if (notificationListener instanceof ChargerInfoNotificationListener) {
						((ChargerInfoNotificationListener) notificationListener).onChargerInfoNotification(cin);
					}
				}
			} else if (notification instanceof DefaultNotification) {
				Logger.log(LogType.VALUE_NOTIFICATION, "Default notification: " + Arrays.toString(bytes) + ". Nothing happens.");
			} else { // TODO is it ok to throw exception in try ?
				throw new IllegalArgumentException("Unknown value notification message");
			}
		} catch (Exception e) {
			// try-catch to prevent swallowing thrown exception by TinyB, which calls this method
			e.printStackTrace();
		}
	}

	/**
	 * Method called by BLE system for connected notifications.
	 * 
	 * @param flag the connection value
	 */

	private void onConnectedNotification(boolean flag) {
		Logger.log(LogType.CONNECTED_NOTIFICATION, "Connected notification: " + flag);
		try {
			ConnectedNotification cn = new ConnectedNotification(this, flag);
			for (NotificationListener notificationListener : listeners) {
				if (notificationListener instanceof ConnectedNotificationListener) {
					((ConnectedNotificationListener) notificationListener).onConnectedNotification(cn);
				}
			}
		} catch (Exception e) {
			// try-catch to prevent swallowing thrown exception by TinyB, which calls this method
			e.printStackTrace();
		}
	}

	private static String upperCaseChars(String str) {
		StringBuilder sb = new StringBuilder();
		str.codePoints().filter(ch -> Character.isUpperCase(ch)).forEach(sb::appendCodePoint);
		return sb.toString();
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

		private static ScheduledExecutorService executor; // update devices asynchronously

		private static final Map<LogType, Boolean> logToggles = new ConcurrentHashMap<>();

		/*
		 * Lock für Tipp aus Tinyb-Dokumentation: Do not attempt to perform multiple connections simultaneously. Instead, serialize all
		 * connection attempts, so that connection, service discovery and characteristic discovery for one peripheral are completed before
		 * attempting to establish another connection.
		 * 
		 */
		private static ReentrantLock lock = new ReentrantLock();

		private static void init() {
			System.out.println("Initializing JAnki. Please wait ...");
			Stream.of(LogType.values()).forEach(value -> logToggles.put(value, Boolean.FALSE));
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				AnkiBle.disconnectAll();
			}));
			AnkiBle.discoverDevices();
			AnkiBle.initializeDevices();
			System.out.println("Initializing JAnki finished");
			executor = Executors.newScheduledThreadPool(1);
			executor.scheduleAtFixedRate(() -> AnkiBle.updateDevices(), 5, 10, TimeUnit.SECONDS);
		}

		/**
		 * Returns the write characteristics of a hardware vehicle.
		 * 
		 * @param device device to get write characteristics for
		 * @return the write characteristics of the device
		 */
		static BluetoothGattCharacteristic writeCharacteristicFor(BluetoothDevice device) {
			return characteristicFor(ANKI_WRITE_CHARACTERISTIC_UUID, device);
		}

		/**
		 * Returns the read characteristics of a hardware vehicle.
		 * 
		 * @param device device to get read characteristics for
		 * @return the read characteristics of the device
		 */
		static BluetoothGattCharacteristic readCharacteristicFor(BluetoothDevice device) {
			return characteristicFor(ANKI_READ_CHARACTERISTIC_UUID, device);
		}

		/**
		 * Returns the read or write characteristics of a hardware vehicle.
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
						System.out.println("no connection in 'characteristicFor()'");
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

		/**
		 * Disconnect all Bluetooth devices.
		 * 
		 */
		private static void disconnectAll() {
			System.out.println("Disconnecting all devices...");
			// Vehicle.vehicles.entrySet().forEach(entry -> entry.getKey().bluetoothDevice.disconnect());
			Vehicle.vehicles.entrySet().parallelStream().forEach(entry -> entry.getKey().bluetoothDevice.disconnect());
		}

		/**
		 * Discover Anki devices.
		 * 
		 * @return the number of discovered devices
		 */
		public static Integer discoverDevices() {
			BluetoothManager manager = BluetoothManager.getBluetoothManager();
			Logger.log(LogType.DEVICE_DISCOVERY, "discovering bluetooth devices ...");
			lock.lock();
			try {
				manager.startDiscovery();
				List<BluetoothDevice> list = manager.getDevices();
				for (BluetoothDevice device : list) {
					if (Arrays.asList(device.getUUIDs()).contains(ANKI_SERVICE_UUID.toLowerCase())) {
						if (Vehicle.getVehicles().stream().map(v -> v.bluetoothDevice.getAddress()).anyMatch(mac -> mac.equals(device.getAddress()))) {
							Logger.log(LogType.DEVICE_DISCOVERY, "vehicle " + device.getAddress() + " already known");
							Vehicle.vehicles.replace(Vehicle.get(device.getAddress()), System.nanoTime());
						} else {
							Vehicle vehicle = new Vehicle(device);
							Vehicle.vehicles.put(vehicle, System.nanoTime());
							Logger.log(LogType.DEVICE_DISCOVERY, "vehicle " + device.getAddress() + " added");
							vehicle.bluetoothDevice.enableConnectedNotifications(flag -> {
								vehicle.onConnectedNotification(flag);
							});
						}
					}
				}
				Thread.sleep(1000); // stopDiscovery() should not be called to early
				manager.stopDiscovery();
				return list.size();
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			} finally {
				lock.unlock();
				Logger.log(LogType.DEVICE_DISCOVERY, "discovering bluetooth devices finished");
			}
		}

		/**
		 * Initialize all devices - at least try to.
		 * 
		 * <p>
		 * This incldes
		 * <ul>
		 * 	<li> set read characteristic</li>
		 * 	<li> set write characteristicsetzen </li>
		 *  <li> set SDK modesetzen </li>
		 * 	<li> register device for value notifications</li>
		 * </ul>
		 * 
		 * @return number of initialized devices
		 */
		public static Integer initializeDevices() {
			int numberOfInitializations = 0;
			try {
				Logger.log(LogType.DEVICE_INITIALIZATION, "initializing bluetooth devices ...");
				Set<Entry<Vehicle, Long>> vehicles = Vehicle.vehicles.entrySet();
				for (Entry<Vehicle, Long> entry : vehicles) {
					Vehicle vehicle = entry.getKey();
					if (!vehicle.connected) {
						vehicle.bluetoothDevice.connect();
					}
					if (vehicle.writeCharacteristic == null) {
						vehicle.writeCharacteristic = writeCharacteristicFor(vehicle.bluetoothDevice);
						Logger.log(LogType.DEVICE_INITIALIZATION, "Write-Characteristic for " + vehicle + (vehicle == null ? " not " : "") + " set");
						numberOfInitializations++;
						if (vehicle.writeCharacteristic != null) {
							vehicle.writeCharacteristic.writeValue(Message.getSdkMode());
						}
					}
					if (vehicle.readCharacteristic == null) {
						vehicle.readCharacteristic = readCharacteristicFor(vehicle.bluetoothDevice);
						Logger.log(LogType.DEVICE_INITIALIZATION, "Read-Characteristic for " + vehicle + (vehicle == null ? " not " : ""));
						numberOfInitializations++;
						if (vehicle.readCharacteristic != null) {
							vehicle.readCharacteristic.enableValueNotifications(bytes -> {
								vehicle.onValueNotification(bytes);
							});
							Logger.log(LogType.DEVICE_INITIALIZATION, "Value notifications set for " + vehicle);
						}
					}
				}
				Logger.log(LogType.DEVICE_INITIALIZATION, "initializing bluetooth devices finished");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return numberOfInitializations;
		}

		/**
		 * Update Devices.
		 * <p>
		 * 
		 * This method should be called repeatedly to
		 * <ul>
		 * 	<li> discover new devices</li>
		 * 	<li> initialize known devices</li>
		 *  <li> remove devices which haven't be seen for a long time</li>
		 * </ul>
		 * 
		 * @author bernd
		 *
		 */
		public static void updateDevices() {
			Logger.log(LogType.DEVICE_UPDATE, "updating bluetooth devices");
			// TODO Verhältnis discovered/initialized prüfen und sinnvoll darauf reagieren
			try {
				Integer numberOfDiscoveredDevices = CompletableFuture.supplyAsync(AnkiBle::discoverDevices).get(2000, TimeUnit.MILLISECONDS);
				Logger.log(LogType.DEVICE_UPDATE, "number of discovered devices: " + numberOfDiscoveredDevices);
				Integer numberOfInitializedDevices = CompletableFuture.supplyAsync(AnkiBle::initializeDevices).get(5000, TimeUnit.MILLISECONDS);
				Logger.log(LogType.DEVICE_UPDATE, "number of initialized devices: " + numberOfInitializedDevices);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
			// TODO check: do we need this:
			final long DEVICE_NOT_SEEN_NANO = 100_000_000_000L;
			for (Entry<Vehicle, Long> entry : Vehicle.vehicles.entrySet()) {
				if (System.nanoTime() - entry.getValue() > DEVICE_NOT_SEEN_NANO) {
					System.out.println("should be removed (not productive): " + entry.getKey());
					// Vehicle.vehicles.remove(entry.getKey());
				}
			}
			Logger.log(LogType.DEVICE_UPDATE, "updating bluetooth devices finished");
		}

	}

	public enum Model {

		KOURAI(0x01), BOSON(0x02), RHO(0x03), KATAL(0x04), HADION(0x05), SPEKTRIX(0x06), CORAX(0x07), GROUNDSHOCK(0x08), SKULL(0x09), THERMO(0x0a), NUKE(
				0x0b), GUARDIAN(0x0d), BIGBANG(0x0e);

		private int id;

		private Model(int id) {
			this.id = id;
		}

		public static Model getModel(int id) {
			for (int i = 0; i < values().length; i++) {
				if (values()[i].id == id) {
					return values()[i];
				}
			}
			throw new IllegalArgumentException("Unbekannte Auto-Id");
		}
	}

	private class DefaultConnectedNotificationListener implements ConnectedNotificationListener {

		@Override
		public void onConnectedNotification(ConnectedNotification connectedNotification) {
			Vehicle.this.connected = connectedNotification.isConnected();
			Logger.log(LogType.CONNECTED_NOTIFICATION, Vehicle.this.toShortString() + (Vehicle.this.connected ? " " : " dis") + "connected");
		}

	}

	private class DefaultChargerInfoNotificationListener implements ChargerInfoNotificationListener {

		@Override
		public void onChargerInfoNotification(ChargerInfoNotification chargerInfoNotification) {
			Vehicle.this.onCharger = chargerInfoNotification.isOnCharger();
			Logger.log(LogType.VALUE_NOTIFICATION, Vehicle.this.toShortString() + (Vehicle.this.onCharger ? " on " : " not on ") + "charger");
		}

	}

}
