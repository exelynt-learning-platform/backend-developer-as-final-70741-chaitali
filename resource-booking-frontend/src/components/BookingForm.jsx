function BookingForm({
  selectedResource,
  startTime,
  endTime,
  price,
  setStartTime,
  setEndTime,
  setPrice,
  bookResource,
  cancelBooking,
}) {
  if (!selectedResource) {
    return null
  }

  return (
    <div className="booking-form">
      <h2>Book {selectedResource.name}</h2>

      <label>Start Time</label>

      <input
        type="datetime-local"
        value={startTime}
        onChange={(e) => setStartTime(e.target.value)}
      />

      <label>End Time</label>

      <input
        type="datetime-local"
        value={endTime}
        onChange={(e) => setEndTime(e.target.value)}
      />

      <label>Price</label>

      <input
        type="number"
        placeholder="Enter price"
        value={price}
        onChange={(e) => setPrice(e.target.value)}
      />

      <button onClick={bookResource}>
        Confirm Booking
      </button>

      <button onClick={cancelBooking}>
        Cancel
      </button>
    </div>
  )
}

export default BookingForm