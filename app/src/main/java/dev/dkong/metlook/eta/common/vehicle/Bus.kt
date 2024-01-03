package dev.dkong.metlook.eta.common.vehicle

/**
 * Template for Buses
 *
 * Because of the large number of combinations of chasis and body, [Bus]es should be instantiated anonymously.
 * @param chasis the chasis of the bus
 * @param body the body of the bus
 * @param id the fleet number of the bus
 */
abstract class Bus(chasis: String, body: String, id: String) : VehicleType("$chasis, $body", id, null)