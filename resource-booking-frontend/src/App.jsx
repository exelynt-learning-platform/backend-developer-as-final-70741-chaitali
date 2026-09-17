import { useState } from 'react'
import './App.css'

import Login from './components/Login'
import ResourceList from './components/ResourceList'
import BookingForm from './components/BookingForm'
import ReservationList from './components/ReservationList'

function App() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  const [resources, setResources] = useState([])
  const [reservations, setReservations] = useState([])

  const [loggedIn, setLoggedIn] = useState(false)

  const [role, setRole] = useState(
    localStorage.getItem('role') || ''
  )

  const [selectedResource, setSelectedResource] = useState(null)

  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [price, setPrice] = useState('')

  const [showReservations, setShowReservations] = useState(false)

  // =========================
  // EDIT RESERVATION STATES
  // =========================

  const [editingReservation, setEditingReservation] =
    useState(null)

  const [editStartTime, setEditStartTime] = useState('')
  const [editEndTime, setEditEndTime] = useState('')
  const [editPrice, setEditPrice] = useState('')
  const [editStatus, setEditStatus] = useState('')

  // =========================
  // LOGIN
  // =========================

  const handleLogin = async (e) => {
    e.preventDefault()

    try {
      const response = await fetch(
        '/auth/login',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            username,
            password,
          }),
        }
      )

      const data = await response.json()

      if (response.ok) {
        localStorage.setItem('token', data.token)
        localStorage.setItem('role', data.role)

        setRole(data.role)

        setMessage('Login successful! ✅')
        setLoggedIn(true)

        await getResources(data.token)
      }
       else {
        setMessage(
          data.message || 'Login failed ❌'
        )
      }
    } catch (error) {
      console.error(error)
      setMessage(
        'Cannot connect to server ❌'
      )
    }
  }

  // =========================
  // GET RESOURCES
  // =========================

  const getResources = async (token) => {
    try {
      const response = await fetch(
        '/resources',
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      )

      if (response.ok) {
        const data = await response.json()
        setResources(data)
      } else {
        setMessage(
          'Unable to load resources ❌'
        )
      }
    } catch (error) {
      console.error(error)
      setMessage(
        'Cannot load resources ❌'
      )
    }
  }

  // =========================
  // RESOURCE DETAILS
  // =========================

  const getResourceDetails = async (id) => {
    try {
      const token =
        localStorage.getItem('token')

      const response = await fetch(
        `/resources/${id}`,
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      )

      const data = await response.json()

      if (response.ok) {
        alert(
          `Resource Details\n\n` +
          `Name: ${data.name}\n` +
          `Description: ${data.description}\n` +
          `Available: ${
            data.available ? 'Yes' : 'No'
          }`
        )
      } else {
        alert(
          data.message ||
          'Unable to load resource details ❌'
        )
      }
    } catch (error) {
      console.error(error)
      alert(
        'Cannot connect to server ❌'
      )
    }
  }

  // =========================
  // GET RESERVATIONS
  // =========================

  const getReservations = async () => {
    try {
      const token =
        localStorage.getItem('token')

      const response = await fetch(
        '/reservations?page=0&size=10&sortBy=id&direction=asc',
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      )

      if (response.ok) {
        const data = await response.json()

        setReservations(
          data.content || []
        )
      } else {
        const data =
          await response.json()

        alert(
          data.message ||
          'Unable to load reservations ❌'
        )
      }
    } catch (error) {
      console.error(error)
      alert(
        'Cannot connect to server ❌'
      )
    }
  }

  // =========================
  // DELETE RESERVATION
  // ADMIN ONLY
  // =========================

  const deleteReservation = async (id) => {
    if (
      !window.confirm(
        'Are you sure you want to delete this reservation?'
      )
    ) {
      return
    }

    try {
      const token =
        localStorage.getItem('token')

      const response = await fetch(
        `/reservations/${id}`,
        {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      )

      if (response.ok) {
        alert(
          'Reservation deleted successfully! ✅'
        )

        await getReservations()
      } else {
        const data =
          await response.json()

        alert(
          data.message ||
          'Unable to delete reservation ❌'
        )
      }
    } catch (error) {
      console.error(error)
      alert(
        'Cannot connect to server ❌'
      )
    }
  }

  // =========================
  // START EDITING RESERVATION
  // ADMIN ONLY
  // =========================

  const handleEdit = (reservation) => {
    setEditingReservation(
      reservation
    )

    setEditStartTime(
      reservation.startTime
        ? reservation.startTime.slice(0, 16)
        : ''
    )

    setEditEndTime(
      reservation.endTime
        ? reservation.endTime.slice(0, 16)
        : ''
    )

    setEditPrice(
      reservation.price
    )

    setEditStatus(
      reservation.status
    )
  }

  // =========================
  // UPDATE RESERVATION
  // ADMIN ONLY
  // =========================

  const updateReservation = async () => {
    if (
      !editingReservation ||
      !editStartTime ||
      !editEndTime ||
      !editPrice ||
      !editStatus
    ) {
      alert(
        'Please fill all edit details ❌'
      )
      return
    }

    if (
      new Date(editEndTime) <=
      new Date(editStartTime)
    ) {
      alert(
        'End time must be after start time ❌'
      )
      return
    }

    if (Number(editPrice) <= 0) {
      alert(
        'Price must be greater than 0 ❌'
      )
      return
    }

    try {
      const token =
        localStorage.getItem('token')

      const response = await fetch(
        `/reservations/${editingReservation.id}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type':
              'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            startTime: editStartTime,
            endTime: editEndTime,
            price: Number(editPrice),
            status: editStatus,
          }),
        }
      )

      const data =
        await response.json()

      if (response.ok) {
        alert(
          'Reservation updated successfully! ✅'
        )

        setEditingReservation(null)
        setEditStartTime('')
        setEditEndTime('')
        setEditPrice('')
        setEditStatus('')

        await getReservations()
      } else {
        alert(
          data.message ||
          'Unable to update reservation ❌'
        )
      }
    } catch (error) {
      console.error(error)
      alert(
        'Cannot connect to server ❌'
      )
    }
  }

  // =========================
  // BOOK RESOURCE
  // =========================

  const bookResource = async () => {
    if (
      !selectedResource ||
      !startTime ||
      !endTime ||
      !price
    ) {
      alert(
        'Please fill all booking details ❌'
      )
      return
    }

    if (Number(price) <= 0) {
      alert(
        'Price must be greater than 0 ❌'
      )
      return
    }

    try {
      const token =
        localStorage.getItem('token')
      console.log("Selected resource:", selectedResource)
      console.log("Sending resourceId:", selectedResource.id)
      const response = await fetch(
        '/reservations',
        {
          method: 'POST',
          headers: {
            'Content-Type':
              'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            resourceId:
              selectedResource.id,
            startTime,
            endTime,
            price: Number(price),
          }),
        }
      )

      const data =
        await response.json()

      if (response.ok) {
        alert(
          'Resource booked successfully! ✅'
        )

        setSelectedResource(null)
        setStartTime('')
        setEndTime('')
        setPrice('')

        if (showReservations) {
          await getReservations()
        }
      } else {
        alert(
          data.message ||
          'Booking failed ❌'
        )
      }
    } catch (error) {
      console.error(error)
      alert(
        'Cannot connect to server ❌'
      )
    }
  }

  // =========================
  // CANCEL BOOKING
  // =========================

  const cancelBooking = () => {
    setSelectedResource(null)
    setStartTime('')
    setEndTime('')
    setPrice('')
  }

  // =========================
  // CANCEL EDIT
  // =========================

  const cancelEdit = () => {
    setEditingReservation(null)
    setEditStartTime('')
    setEditEndTime('')
    setEditPrice('')
    setEditStatus('')
  }

  // =========================
  // LOGOUT
  // =========================

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')

    setLoggedIn(false)
    setRole('')

    setResources([])
    setReservations([])

    setSelectedResource(null)

    setStartTime('')
    setEndTime('')
    setPrice('')

    setShowReservations(false)

    setEditingReservation(null)
    setEditStartTime('')
    setEditEndTime('')
    setEditPrice('')
    setEditStatus('')

    setUsername('')
    setPassword('')
    setMessage('')
  }

  // =========================
  // UI
  // =========================

  return (
    <div className="app">

      {!loggedIn ? (
        <Login
          username={username}
          password={password}
          setUsername={setUsername}
          setPassword={setPassword}
          handleLogin={handleLogin}
          message={message}
        />
      ) : (

        <div className="dashboard">

          {/* =========================
              HEADER
          ========================= */}

          <div className="dashboard-header">

            <div>
              <h1>
                Resource Booking System
              </h1>

              <p>
                Manage and book available resources
              </p>
            </div>

            <div className="header-actions">

              <span className="user-role">
                {role}
              </span>

              <button
                onClick={handleLogout}
              >
                Logout
              </button>

            </div>

          </div>

          {/* =========================
              RESERVATIONS
          ========================= */}

          <div className="reservation-section">

            <button
              onClick={async () => {
                await getReservations()
                setShowReservations(true)
              }}
            >
              My Reservations
            </button>

            {showReservations && (
              <ReservationList
                reservations={reservations}
                showReservations={
                  showReservations
                }
                isAdmin={
                  role?.toUpperCase() ===
                  'ADMIN'
                }
                onEdit={handleEdit}
                onDelete={
                  deleteReservation
                }
              />
            )}

          </div>

          {/* =========================
              EDIT RESERVATION
          ========================= */}

          {editingReservation && (
            <div className="booking-form">

              <h2>
                Edit Reservation #
                {editingReservation.id}
              </h2>

              <label>
                Start Time
              </label>

              <input
                type="datetime-local"
                value={editStartTime}
                onChange={(e) =>
                  setEditStartTime(
                    e.target.value
                  )
                }
              />

              <label>
                End Time
              </label>

              <input
                type="datetime-local"
                value={editEndTime}
                onChange={(e) =>
                  setEditEndTime(
                    e.target.value
                  )
                }
              />

              <label>
                Price
              </label>

              <input
                type="number"
                value={editPrice}
                onChange={(e) =>
                  setEditPrice(
                    e.target.value
                  )
                }
              />

              <label>
                Status
              </label>

              <select
                value={editStatus}
                onChange={(e) =>
                  setEditStatus(
                    e.target.value
                  )
                }
              >
                <option value="PENDING">
                  PENDING
                </option>

                <option value="CONFIRMED">
                  CONFIRMED
                </option>

                <option value="CANCELLED">
                  CANCELLED
                </option>
              </select>

              <button
                onClick={
                  updateReservation
                }
              >
                Update Reservation
              </button>

              <button
                onClick={cancelEdit}
              >
                Cancel
              </button>

            </div>
          )}

          {/* =========================
              RESOURCES
          ========================= */}

          <h2 className="section-title">
            Available Resources
          </h2>

          <ResourceList
            resources={resources}
            getResourceDetails={
              getResourceDetails
            }
            setSelectedResource={
              setSelectedResource
            }
          />

          {/* =========================
              BOOKING FORM
          ========================= */}

          <BookingForm
            selectedResource={
              selectedResource
            }
            startTime={startTime}
            endTime={endTime}
            price={price}
            setStartTime={
              setStartTime
            }
            setEndTime={
              setEndTime
            }
            setPrice={setPrice}
            bookResource={
              bookResource
            }
            cancelBooking={
              cancelBooking
            }
          />

        </div>
      )}

    </div>
  )
}

export default App