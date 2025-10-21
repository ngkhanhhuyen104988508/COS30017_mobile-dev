package com.example.musicstudiorental.repositories

import com.example.renting.R
import com.example.renting.models.Instrument
object InstrumentRepository {

    // In-memory storage - will be reset when app closes
    private val instruments = mutableListOf<Instrument>()


    fun initialize() {
        if (instruments.isEmpty()) {
            instruments.addAll(createSampleInstruments())
        }
    }


    //Creates sample instruments

    private fun createSampleInstruments(): List<Instrument> {
        return listOf(
            Instrument(
                id = 1,
                name = "Acoustic Guitar Pro",
                rating = 4.5f,
                condition = "Excellent",
                pricePerMonth = 50,
                imageResourceId = R.drawable.guitar,
                description = "Professional 6-string acoustic guitar with solid spruce top. " +
                        "Perfect for studio recording and live performances. " +
                        "Includes premium hard case and extra strings.",
                category = "String Instrument"
            ),
            Instrument(
                id = 2,
                name = "Digital Piano 88-Key",
                rating = 4.8f,
                condition = "Excellent",
                pricePerMonth = 80,
                imageResourceId = R.drawable.piano,
                description = "Full-size 88-key digital piano with weighted keys and hammer action. " +
                        "Features multiple instrument voices and MIDI connectivity. " +
                        "Ideal for practice and composition.",
                category = "Keyboard"
            ),
            Instrument(
                id = 3,
                name = "Professional Drum Kit",
                rating = 4.3f,
                condition = "Good",
                pricePerMonth = 100,
                imageResourceId = R.drawable.drum,
                description = "5-piece drum kit with cymbals, hardware, and drum throne. " +
                        "Quality shells with excellent resonance. " +
                        "Perfect for rock, jazz, and pop recordings.",
                category = "Percussion"
            ),
            Instrument(
                id = 4,
                name = "Studio Condenser Microphone",
                rating = 4.7f,
                condition = "Excellent",
                pricePerMonth = 45,
                imageResourceId = R.drawable.micro,
                description = "Large-diaphragm condenser microphone with cardioid pattern. " +
                        "Crystal clear vocal and instrument recording. " +
                        "Includes shock mount and pop filter.",
                category = "Recording Equipment"
            )
        )
    }

     //Get all instruments in the catalog

    fun getAllInstruments(): List<Instrument> {
        return instruments.toList() // Return immutable copy
    }

     //Get a specific instrument by ID
    fun getInstrumentById(id: Int): Instrument? {
        return instruments.find { it.id == id }
    }

     //Get instrument at specific index position
    fun getInstrumentAtIndex(index: Int): Instrument? {
        return if (index in instruments.indices) {
            instruments[index]
        } else {
            null
        }
    }

     // Get total number of instruments in catalog
    fun getInstrumentCount(): Int {
        return instruments.size
    }

     //Update an existing instrument's details, Used when saving booking information
    fun updateInstrument(updatedInstrument: Instrument): Boolean {
        val index = instruments.indexOfFirst { it.id == updatedInstrument.id }
        return if (index != -1) {
            instruments[index] = updatedInstrument
            true
        } else {
            false
        }
    }

     //Check if an instrument is currently booked
    fun isInstrumentBooked(instrumentId: Int): Boolean {
        return instruments.find { it.id == instrumentId }?.isBooked ?: false
    }

    /**
     * Get all available (not booked) instruments
     * @return List of available instruments
     */
    fun getAvailableInstruments(): List<Instrument> {
        return instruments.filter { !it.isBooked }
    }

     //Get all currently booked instruments
    fun getBookedInstruments(): List<Instrument> {
        return instruments.filter { it.isBooked }
    }

     //Cancel a booking for an instrument
    fun cancelBooking(instrumentId: Int): Boolean {
        val instrument = instruments.find { it.id == instrumentId }
        return if (instrument != null && instrument.isBooked) {
            instrument.cancelBooking()
            updateInstrument(instrument)
        } else {
            false
        }
    }

     //Reset all data to initial state (useful for testing)
    fun reset() {
        instruments.clear()
        initialize()
    }
     //Get index of an instrument by its ID
     //instrumentId The ID to search for
    fun getIndexOfInstrument(instrumentId: Int): Int {
        return instruments.indexOfFirst { it.id == instrumentId }
    }
}