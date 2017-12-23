package de.pdbm.janki.tracking;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import de.pdbm.janki.core.RoadPiece;
import de.pdbm.janki.core.Vehicle;
import de.pdbm.janki.core.notifications.PositionUpdate;
import de.pdbm.janki.core.notifications.PositionUpdateListener;


/**
 * Example of how to construct the actual race track.
 * <p>
 * Design decisions are as follow:
 * <ul>
 * 	 <li>race track is on square based field</li>
 *   <li>tracking starts at coordinates 0/0</li>
 *   <li>no intersection road piece is used</li>
 *   <li>there is no intersection at different leves, i.e. nothing like bridges</li>
 *   <li>we do not differentiate between START and FINISH</li>
 * </ul>
 * 
 * @author bernd
 *
 */
public class Tracking {

	private boolean gathering; // do we gather road pieces from vehicle events?
	final Deque<CoordinatesRoadPieceTuple> coordinatesRoadPieceTuples; // the basket to gather

	public Tracking() {
		gathering = false;
		coordinatesRoadPieceTuples = new ConcurrentLinkedDeque<>();
	}

	public static List<CoordinatesRoadPieceTuple> gatherRoadPieces() {
		Tracking tracking = new Tracking();
		Vehicle vehicle = findVehicleReadyToStart();
		GatheringListener listener = tracking.new GatheringListener();
		vehicle.addNotificationListener(listener);
		tracking.gathering = true;
		vehicle.setSpeed(500);
		while (tracking.gathering) {
			// wait till all road pieces are gathered
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		vehicle.setSpeed(0);
		vehicle.removeNotificationListener(listener);
		return tracking.coordinatesRoadPieceTuples.stream().collect(Collectors.toList());
	}

	
	/**
	 * Find one vehicle which is ready to start.
	 * 
	 * @return vehicle which is ready to start
	 */
	private static Vehicle findVehicleReadyToStart() {
		List<Vehicle> vehicles = Vehicle.getVehicles();
		while (true) {
			for (Vehicle vehicle : vehicles) {
				if (vehicle.isReadyToStart()) {
					System.out.println("Start Tracking with " + vehicle);
					return vehicle;
				}
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the race track as ASCII art.
	 * <p>
	 * The first dimension is y, the second x to easy row wise output.
	 * 
	 * @return the race track as ASCII art
	 */
	public static char[][] raceTrackAsAsciiArt(List<CoordinatesRoadPieceTuple> tuples) {
		int minX = tuples.stream().mapToInt(tuple -> tuple.x).min().getAsInt();
		int minY = tuples.stream().mapToInt(tuple -> tuple.y).min().getAsInt();
		List<CoordinatesRoadPieceTuple> normalizedTuples = 
				tuples.stream().map(t -> t.shift(-minX, -minY)).collect(Collectors.toList());
		int maxX = normalizedTuples.stream().mapToInt(tuple -> tuple.x).max().getAsInt();
		int maxY = normalizedTuples.stream().mapToInt(tuple -> tuple.y).max().getAsInt();
		char[][] area = new char[maxY + 1][maxX + 1];
		normalizedTuples.stream().forEach(tuple -> {area[tuple.y][tuple.x] = tuple.c;});
		return area;
	}
	
	
	
	private class GatheringListener implements PositionUpdateListener {

		private static final int X_START = 0; // x position for the first road piece
		private static final int Y_START = 0; // y position for the first road piece
		
		private VehicleInfo oldVehicleInfo;
		private PositionUpdate lastPositionUpdate = null;

		@Override
		public void onPositionUpdate(PositionUpdate positionUpdate) {
			VehicleInfo newVehicleInfo = null;
			RoadPiece roadPiece = positionUpdate.getRoadPiece();
			if (isNewRoadPiece(positionUpdate)) {
				if (Tracking.this.coordinatesRoadPieceTuples.isEmpty()) {
					char c;
					if (roadPiece == RoadPiece.INTERSECTION) {
						throw new RuntimeException("Fail Fast: intersection not supported in Tracking.");
					} else if (roadPiece == RoadPiece.STRAIGHT || roadPiece == RoadPiece.START || roadPiece == RoadPiece.FINISH) {
						c = '-';
						oldVehicleInfo = new VehicleInfo(X_START, Y_START, Direction.POSITIVE_X);
					} else if (roadPiece == RoadPiece.CORNER) {
						if (positionUpdate.isAscendingLocations()) {
							c = '/';
							oldVehicleInfo = new VehicleInfo(X_START, Y_START, Direction.POSITIVE_Y);
						} else {
							c = '\\';
							oldVehicleInfo = new VehicleInfo(X_START, Y_START, Direction.NEGATIVE_Y);
						}
					} else {
						c = ' '; // should not happen, initialized to unknown
					}
					Tracking.this.coordinatesRoadPieceTuples.addLast(
							new CoordinatesRoadPieceTuple(oldVehicleInfo.xPosition, oldVehicleInfo.yPosition, roadPiece, c));
				} else {
					newVehicleInfo = newVehicleInfo(oldVehicleInfo, positionUpdate);
					if (!gathering || isRaceTrackComplete(newVehicleInfo)) {
						gathering = false;
					} else {
						if (roadPiece == RoadPiece.INTERSECTION) {
							throw new RuntimeException("Fail Fast: intersection not supported in Tracking.");
						} else if (roadPiece == RoadPiece.START || roadPiece == RoadPiece.FINISH) {
							if (coordinatesRoadPieceTuples.getLast().rp == RoadPiece.START || coordinatesRoadPieceTuples.getLast().rp == RoadPiece.FINISH) {
								newVehicleInfo = oldVehicleInfo;
							} else {
								Tracking.this.coordinatesRoadPieceTuples.addLast(
										new CoordinatesRoadPieceTuple(newVehicleInfo.xPosition, newVehicleInfo.yPosition, roadPiece, '-'));
							}
						} else {
							char c;
							if (roadPiece == RoadPiece.STRAIGHT) {
								if (oldVehicleInfo.direction == Direction.POSITIVE_X || oldVehicleInfo.direction == Direction.NEGATIVE_X) {
									c = '-';
								} else {
									c = '|';
								}
							} else {
								// must be CORNER
								if (positionUpdate.isAscendingLocations()) {
									if (oldVehicleInfo.direction == Direction.POSITIVE_X || oldVehicleInfo.direction == Direction.NEGATIVE_X) {
										c = '/';
									} else {
										c = '\\';
									}
								} else {
									if (oldVehicleInfo.direction == Direction.POSITIVE_X || oldVehicleInfo.direction == Direction.NEGATIVE_X) {
										c = '\\';
									} else {
										c = '/';
									}
								}
							}
							Tracking.this.coordinatesRoadPieceTuples.addLast(
									new CoordinatesRoadPieceTuple(newVehicleInfo.xPosition, newVehicleInfo.yPosition, roadPiece, c));
						}
						oldVehicleInfo = newVehicleInfo;
					}
				}
			}
		}

		

		/**
		 * Updates the vehicle information wrt. the new direction.
		 * 
		 * @param vehicleInfo the last vehicle info
		 * @param positionUpdate the position update event data
		 * @return the new vehicle info
		 * 
		 */
		private VehicleInfo newVehicleInfo(VehicleInfo vehicleInfo, PositionUpdate positionUpdate) {
			VehicleInfo newVehicleInfo = null;
			switch (vehicleInfo.direction) {
			case POSITIVE_X:
				if (positionUpdate.getRoadPiece() == RoadPiece.STRAIGHT || positionUpdate.getRoadPiece() == RoadPiece.START || positionUpdate.getRoadPiece() == RoadPiece.FINISH) {
					newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition + 1, vehicleInfo.yPosition, vehicleInfo.direction);
				} else if (positionUpdate.getRoadPiece() == RoadPiece.CORNER) {
					if (positionUpdate.isAscendingLocations()) { // left corner
						newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition + 1, vehicleInfo.yPosition, vehicleInfo.direction.decrement());
					} else { // right corner
						newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition + 1, vehicleInfo.yPosition, vehicleInfo.direction.increment());
					}
				} else {
					throw new RuntimeException("Fail Fast: intersection not supported in Tracking.");
				}
				break;
			case NEGATIVE_Y:
				if (positionUpdate.getRoadPiece() == RoadPiece.STRAIGHT || positionUpdate.getRoadPiece() == RoadPiece.START || positionUpdate.getRoadPiece() == RoadPiece.FINISH) {
					newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition, vehicleInfo.yPosition - 1, vehicleInfo.direction);
				} else if (positionUpdate.getRoadPiece() == RoadPiece.CORNER) {
					if (positionUpdate.isAscendingLocations()) {
						newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition, vehicleInfo.yPosition - 1, vehicleInfo.direction.decrement());
					} else {
						newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition, vehicleInfo.yPosition - 1, vehicleInfo.direction.increment());
					}
				} else {
					throw new RuntimeException("Fail Fast: intersection not supported in Tracking.");
				}
				break;
			case NEGATIVE_X:
				if (positionUpdate.getRoadPiece() == RoadPiece.STRAIGHT || positionUpdate.getRoadPiece() == RoadPiece.START || positionUpdate.getRoadPiece() == RoadPiece.FINISH) {
					newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition - 1, vehicleInfo.yPosition, vehicleInfo.direction);
				} else if (positionUpdate.getRoadPiece() == RoadPiece.CORNER) {
					if (positionUpdate.isAscendingLocations()) {
						newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition - 1, vehicleInfo.yPosition, vehicleInfo.direction.decrement());
					} else {
						newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition - 1, vehicleInfo.yPosition, vehicleInfo.direction.increment());
					}
				} else {
					throw new RuntimeException("Fail Fast: intersection not supported in Tracking.");
				}
				break;
			case POSITIVE_Y:
				if (positionUpdate.getRoadPiece() == RoadPiece.STRAIGHT || positionUpdate.getRoadPiece() == RoadPiece.START || positionUpdate.getRoadPiece() == RoadPiece.FINISH) {
					newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition, vehicleInfo.yPosition + 1, vehicleInfo.direction);
				} else if (positionUpdate.getRoadPiece() == RoadPiece.CORNER) {
					if (positionUpdate.isAscendingLocations()) {
						newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition, vehicleInfo.yPosition + 1, vehicleInfo.direction.decrement());
					} else {
						newVehicleInfo = new VehicleInfo(vehicleInfo.xPosition, vehicleInfo.yPosition + 1, vehicleInfo.direction.increment());
					}
				} else {
					throw new RuntimeException("Fail Fast: intersection not supported in Tracking.");
				}
				break;
			}
			return newVehicleInfo;
		}

		
		/**
		 * Is this position update from a new road piece? 
		 * 
		 * @param positionUpdate the actual position update
		 * @return true, if postion update is from a new road piece
		 */
		private boolean isNewRoadPiece(PositionUpdate positionUpdate) {
			// TODO refactor to remove the identical lines
			if (lastPositionUpdate == null) {
				lastPositionUpdate = positionUpdate;
				return true;
			} else if (lastPositionUpdate.getRoadPiece() != positionUpdate.getRoadPiece()) {
				// different road piece is of course new road piece
				lastPositionUpdate = positionUpdate;
				return true;
			} else {
				if (lastPositionUpdate.isAscendingLocations()) {
					// locations on same road piece must increase by 1
					if (lastPositionUpdate.getLocation() + 1 == positionUpdate.getLocation()) {
						lastPositionUpdate = positionUpdate;
						return false;
					} else {
						lastPositionUpdate = positionUpdate;
						return true;
					}
				} else {
					// locations on same road piece must decrease by 1
					if (lastPositionUpdate.getLocation() - 1 == positionUpdate.getLocation()) {
						lastPositionUpdate = positionUpdate;
						return false;
					} else {
						lastPositionUpdate = positionUpdate;
						return true;
					}
				}
			}
		}

		
		/**
		 * Check for ring closure of race track.
		 * <p>
		 * Because we start tracking at cooridnates 0/0 the race track is complete 
		 * if we find 0/0 again
		 *
		 * @param vehicleInfo the actual vehicle info
		 * @return true, if race track is complete, false otherwise
		 */

		private boolean isRaceTrackComplete(VehicleInfo vehicleInfo) {
			return vehicleInfo.xPosition == X_START && vehicleInfo.yPosition == Y_START;
		}


	}
	
	
}
