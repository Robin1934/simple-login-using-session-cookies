import { useState } from "react";
import "./App.css";

function App() {
  const [showLogin, setShowLogin] = useState(false);

  return (
    <div>
      <nav className="navbar">
        <div className="logo">
          🥥 CocoFresh
        </div>

        <div className="nav-links">
          <a href="#">Home</a>
          <a href="#products">Products</a>
          <a href="#about">About</a>

          <button onClick={() => setShowLogin(true)}>
            Login
          </button>
        </div>
      </nav>

      <section style={{ padding: "100px", textAlign: "center" }}>
        <h1>CocoFresh</h1>
        <h2>Login test</h2>

        <button onClick={() => setShowLogin(true)}>
          Open Login
        </button>
      </section>

      {showLogin && (
        <div className="modal-overlay">
          <div className="auth-box">
            <button
              className="close-button"
              onClick={() => setShowLogin(false)}
            >
              ×
            </button>

            <h2>Welcome Back</h2>

            <form>
              <input
                type="email"
                placeholder="Email"
              />

              <input
                type="password"
                placeholder="Password"
              />

              <button
                type="button"
                className="auth-button"
              >
                Login
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;