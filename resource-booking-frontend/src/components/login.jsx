function Login({
  username,
  password,
  setUsername,
  setPassword,
  handleLogin,
  message,
}) {
  return (
    <div className="login-card">
      <h1>Resource Booking System</h1>

      <p className="subtitle">Login to continue</p>

      <form onSubmit={handleLogin}>
        <label>Username</label>

        <input
          type="text"
          placeholder="Enter username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />

        <label>Password</label>

        <input
          type="password"
          placeholder="Enter password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <button type="submit">Login</button>
      </form>

      {message && <p>{message}</p>}
    </div>
  )
}

export default Login