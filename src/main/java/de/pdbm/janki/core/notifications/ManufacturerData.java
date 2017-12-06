package de.pdbm.janki.core.notifications;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import de.pdbm.janki.core.Vehicle;
import tinyb.BluetoothDevice;

/**
 * Class for hiding TinyB and Anki SDK implementation details.
 * <p>
 * 
 * @author bernd
 *
 */
public class ManufacturerData {
	
	public static Optional<Vehicle.Model> modelFor(BluetoothDevice bluetoothDevice) {
		Map<Short, byte[]> manufacturerDataMap = bluetoothDevice.getManufacturerData();
		/*
		 * in my tests the map contains always only key -4162 with example values
		 * [0, 8, 15, 49, -76, -48]
		 * [0, 9, 15, 49, -70, -106]
		 * [0, 10, 15, 49, 76, 77]
		 * [0, 11, 6, -15, 120, 2]
		 * 
		 * suppose now second byte is model Id, not compatible with Anki documentation below
		 * 
		 */
		
		if (manufacturerDataMap.entrySet().size() > 1) {
			// if something changes, for example firmware update
			System.out.println("Manufacturer Data Map hasn't size 1 anymore!");
			for (Map.Entry<Short, byte[]> entry : manufacturerDataMap.entrySet()) {
				System.out.println("Key: " + entry.getKey() + ", value: " + Arrays.toString(entry.getValue()));
			}
		}
		byte[] bytes = manufacturerDataMap.get(new Integer(-4162).shortValue());
		return bytes == null ? Optional.empty() : Optional.of(Vehicle.Model.getModel(bytes[1]));
	}



	/*
	 * Taken from
	 * <a href="https://anki.github.io/drive-sdk/docs/programming-guide">Ankis Programming Guide</a>:
	 * 
	 * The manufacturer data is a uint64_t value that uniquely identifies each vehicle. 
	 * This value specifies the 'make/model' of the vehicle (model_id) and a unique 
	 * identifier for each vehicle of the specified model (identifier).
	 * 
	 */

	/*
	 * Vehicle hardware information encoded in the MANUFACTURER_DATA
	 * record of an advertising packet.
	 *
	 * - identifier: Unique identifier for a physical vehicle
	 * - model_id: The model type of a vehicle
	 * - product_id: Value identifying the vehicle as Anki Drive hardware
	 */
	/*
	typedef struct anki_vehicle_adv_mfg {
	    uint32_t    identifier;
	    uint8_t     model_id;
	    uint8_t     _reserved;
	    uint16_t    product_id;
	} anki_vehicle_adv_mfg_t;
	 */
	
	/*
	 * Taken from TinyB documentation:
	 * native Map<Short, byte[]> tinyb.BluetoothDevice.getManufacturerData() 	 
	 * Returns a map containing manufacturer specific advertisement data. An entry has a uint16_t key and an array of bytes. 
	 */
	
	
}
