package dev.dkong.metlook.eta.common.vehicle

/**
 * Template for Trains
 * @param name display name of the vehicle
 * @param id the fleet number of the vehicle
 * @param carCount the number of cars of the vehicle
 */
sealed class Train(name: String, id: String, carCount: Int) : VehicleType(name, id, null) {
    class Hcmt(id: String, carCount: Int) : Train("High-Capacity Metro Train", id, carCount)
    class Xtrapolis(id: String, carCount: Int) : Train("Alstom X'Trapolis", id, carCount)
    class Siemens(id: String, carCount: Int) : Train("Siemens Nexas", id, carCount)
    class Comeng(id: String, carCount: Int) : Train("Comeng", id, carCount)
}