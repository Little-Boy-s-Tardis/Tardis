import { Play } from 'lucide-react';
import './Hero.css';

export function Hero() {
  return (
    <section className="hero-section">
      <div className="container hero-container">
        <div className="hero-content text-center">
          <h1 className="text-display">
            Double Your Business with<br />Perspective Funnels
          </h1>
          <p className="hero-subtitle text-body text-muted">
            Create high-converting funnels in minutes. No coding required. Build your brand, capture leads, and sell products effortlessly.
          </p>
          
          <div className="hero-cta-group">
            <button className="btn btn-lg btn-primary">Start Free Trial</button>
            <button className="btn btn-lg btn-secondary">
              <Play size={20} fill="currentColor" /> Watch Video
            </button>
          </div>
        </div>

        <div className="hero-visual">
          <div className="doodle-box browser-window">
            <div className="doodle-header">
              <span className="doodle-dot"></span>
              <span className="doodle-dot"></span>
              <span className="doodle-dot"></span>
            </div>
            <div className="browser-body">
              <div className="newsletter-mockup doodle-border">
                <svg width="64" height="64" viewBox="0 0 64 64" fill="none" stroke="var(--c-charcoal)" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" className="circle-avatar doodle-border">
                  <path d="M32 10 A 15 15 0 1 0 32 40 A 15 15 0 1 0 32 10 Z" fill="var(--c-primary)" />
                  <path d="M16 60 Q 32 40 48 60" />
                </svg>
                <h3>Join our Newsletter</h3>
                <svg width="200" height="20" viewBox="0 0 200 20" fill="none" stroke="var(--c-charcoal)" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M10 10 Q 100 5 190 15" strokeDasharray="5, 10" />
                </svg>
                <div className="mockup-input doodle-border"></div>
                <div className="mockup-btn doodle-border"></div>
              </div>
            </div>
          </div>
        </div>

        <div className="trusted-by">
          <p>Trusted by over 5,000 businesses across all industries</p>
          <div className="brands-grid">
            <span className="brand-logo">Google</span>
            <span className="brand-logo">Amazon</span>
            <span className="brand-logo">Spotify</span>
            <span className="brand-logo">Tesla</span>
            <span className="brand-logo">Apple</span>
          </div>
        </div>
      </div>
    </section>
  );
}
