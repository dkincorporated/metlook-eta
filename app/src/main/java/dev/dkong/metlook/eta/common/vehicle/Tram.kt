package dev.dkong.metlook.eta.common.vehicle

/**
 * Template for Trams
 * @param name display name of the vehicle
 * @param classId the class of the vehicle
 * @param id the fleet number of the vehicle
 */
sealed class Tram(name: String, val classId: String, id: String) : VehicleType(name, id) {
    class Z3(id: String) : Tram("Comeng", "Z3", id)
    class A1(id: String) : Tram("Comeng", "A1", id)
    class A2(id: String) : Tram("Comeng", "A2", id)
    class B2(id: String) : Tram("Comeng", "B2", id)
    class C(id: String) : Tram("Alstom Citadis", "C", id)
    class D1(id: String) : Tram("Siemens Combino", "D1", id)
    class D2(id: String) : Tram("Siemens Combino", "D2", id)
    class C2(id: String) : Tram("Alstom Citadis", "C2", id)
    class E(id: String) : Tram("Bombardier Flexity Swift", "E", id)
    class E2(id: String) : Tram("Bombardier Flexity Swift", "E2", id)
    class W8(id: String) : Tram("MMTB", "W8", id)
}