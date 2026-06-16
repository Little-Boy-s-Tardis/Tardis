import './Nav.css';

export function Nav() {
  return (
    <nav className="primary-nav">
      <div className="nav-container">
        <div className="nav-brand">
          <span className="brand-icon">P</span>
          <span className="brand-text">Perspective</span>
        </div>
        
        <div className="nav-links">
          <a href="#" className="nav-link">Product</a>
          <a href="#" className="nav-link">Templates</a>
          <a href="#" className="nav-link">Pricing</a>
          <a href="#" className="nav-link">Resources</a>
        </div>

        <div className="nav-actions">
          <a href="#" className="nav-link">Log In</a>
          <button className="btn btn-sm btn-primary">Start Free Trial</button>
        </div>
      </div>
    </nav>
  );
}
