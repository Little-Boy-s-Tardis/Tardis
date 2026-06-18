import './Nav.css';

export function Nav() {
  return (
    <nav className="primary-nav">
      <div className="nav-container">
        <div className="nav-brand">
          <img src="/logo.png" alt="Tardis Logo" className="tardis-logo" />
          <span className="brand-text">Tardis</span>
          <span className="brand-subtext text-muted text-small" style={{ fontSize: '12px', marginLeft: '4px' }}>Judges Aggregator</span>
        </div>
        
        <div className="nav-links">
          <div className="live-status-indicator">
            <span className="pulse-dot"></span>
            <span className="status-text text-small">Real-time Webhook Stream Active</span>
          </div>
        </div>
      </div>
    </nav>
  );
}


