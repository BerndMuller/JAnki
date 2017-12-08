package de.pdbm.janki.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import de.pdbm.janki.core.LogType;
import de.pdbm.janki.core.Logger;
import de.pdbm.janki.core.Vehicle;

public class Main {

	public static void main(String[] args) throws Exception {
		Logger.switchLogTypeOn(LogType.values());
		Vehicle.getVehicles().forEach(v -> v.addNotificationListener(new PositionUpdateListenerExample()));
		//Vehicle.getVehicles().forEach(v -> v.addNotificationListener(new TransitionUpdateListenerExample()));

		Scanner input = new Scanner(System.in);
		List<String> macs = new ArrayList<>();
		Vehicle vehicle = null; // selected vehicle

		System.out.print("> ");
		while (input.hasNextLine()) {
			macs = Vehicle.getVehicles().stream().map(v -> v.getMacAddress()).collect(Collectors.toList());
			String line = input.nextLine();
			if (line.equals("devices")) {
				Vehicle.getVehicles().forEach(System.out::println);
			} else if (macs.contains(line)) {
				vehicle = Vehicle.get(line);
				System.out.println("Current vehicle: " + vehicle);
			} else if (line.startsWith("speed ")) {
				String[] tokens = line.split(" ");
				if (vehicle == null) {
					System.out.println("No vehicle selected");
				} else if (tokens.length == 2) {
					vehicle.setSpeed(new Integer(tokens[1]));
				}
			} else if (line.equals("clr")) {
				vehicle.changeLane(20.0f);
			} else if (line.equals("cll")) {
				vehicle.changeLane(-20.0f);
			} else {
				System.out.println("unknown command " + line);
				System.out.println("possible commands:");
				System.out.println("  devices - list all devices");
				System.out.println("  <MAC> - use device with this MAC address");
				System.out.println("  speed <int value> - sets speed");
				System.out.println("  cll - change lane left");
				System.out.println("  clr - change lane right");
			}
			System.out.print("\n> ");
		}
		input.close();
	}

}
