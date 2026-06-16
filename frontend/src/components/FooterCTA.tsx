import './FooterCTA.css';

export function FooterCTA() {
  return (
    <>
      <section className="cta-section section-gap">
        <div className="container">
          <div className="cta-content text-center">
            <h2>Your Free Trial, Made Easy</h2>
            <p className="text-body text-muted">
              No credit card required. Start building your first funnel in minutes.
            </p>
            <div className="cta-buttons">
              <button className="btn btn-lg btn-primary">Start Free Trial</button>
            </div>
          </div>
        </div>
      </section>

      <footer className="footer-section">
        <div className="container footer-container">
          <div className="footer-brand">
            <span className="brand-icon">P</span>
            <span className="brand-text">Perspective</span>
          </div>
          <div className="footer-links">
            <a href="#" className="link-bold">Privacy Policy</a>
            <a href="#" className="link-bold">Terms of Service</a>
            <a href="#" className="link-bold">Contact</a>
          </div>
        </div>
      </footer>
    </>
  );
}
