function ReservationList({
  reservations,
  showReservations,
  isAdmin,
  onEdit,
  onDelete,
}) {
  if (!showReservations) {
    return null
  }

  return (
    <div className="reservation-list">
      <h2>My Reservations</h2>

      {reservations.length === 0 ? (
        <p>No reservations found.</p>
      ) : (
        reservations.map((reservation) => (
          <div
            className="reservation-card"
            key={reservation.id}
          >
            <h3>{reservation.resource.name}</h3>

            <p>Reservation ID: {reservation.id}</p>
            <p>Start: {reservation.startTime}</p>
            <p>End: {reservation.endTime}</p>
            <p>Price: ₹{reservation.price}</p>
            <p>Status: {reservation.status}</p>

            {isAdmin && (
              <div className="reservation-actions">
                <button onClick={() => onEdit(reservation)}>
                  Edit Reservation ✏️
                </button>

                <button onClick={() => onDelete(reservation.id)}>
                  Delete Reservation 🗑️
                </button>
              </div>
            )}
          </div>
        ))
      )}
    </div>
  )
}

export default ReservationList