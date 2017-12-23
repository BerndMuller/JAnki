package de.pdbm.janki.tracking;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.pdbm.janki.core.LogType;
import de.pdbm.janki.core.Logger;

public class Main {

	public static void main(String[] args) throws Exception {
		Logger.switchLogTypeOn(LogType.values());
		List<CoordinatesRoadPieceTuple> tuples = CompletableFuture.supplyAsync(Tracking::gatherRoadPieces).get();
		System.out.println("Tracking finished with:");
		tuples.stream().forEach(System.out::print);
		System.out.println("");
		char[][] raceTrack = Tracking.raceTrackAsAsciiArt(tuples);
		System.out.println("race track: \n");
		for (int row = raceTrack.length - 1; row >= 0; row--) {
			System.out.println(raceTrack[row]);
		}
		System.exit(0);
	}

}
