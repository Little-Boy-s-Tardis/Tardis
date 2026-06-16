import { Play } from 'lucide-react';
import './Stats.css';

export function Stats() {
  return (
    <section className="stats-section section-gap">
      <div className="container">
        <div className="stats-header text-center">
          <h2>Why Do You Need a Perspective Funnel?</h2>
          <p className="text-body text-muted">
            Traditional websites are dead. Funnels guide your visitors step-by-step to the exact action you want them to take.
          </p>
        </div>

        <div className="stats-visual-container">
          <div className="doodle-box video-placeholder">
            <div className="video-content">
              <svg width="300" height="200" viewBox="0 0 300 200" fill="none" stroke="var(--c-charcoal)" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" style={{transform: 'rotate(-5deg)', position: 'absolute'}}>
                {/* Spilled liquid */}
                <path d="M 120 150 Q 80 180 50 140 T 30 90 T 70 50 Q 120 20 180 50 T 250 120 T 180 170 Z" fill="var(--c-slate)" opacity="0.8"/>
                <path d="M 60 120 Q 30 160 80 180" strokeDasharray="5 5" />
                {/* Coffee Cup body */}
                <path d="M 100 130 C 100 90, 180 90, 180 130 C 180 180, 100 180, 100 130 Z" fill="var(--c-white)"/>
                {/* Cup shadow/detail */}
                <path d="M 110 130 C 110 160, 170 160, 170 130" fill="var(--c-primary)" />
                {/* Coffee Cup handle */}
                <path d="M 180 120 C 210 120, 210 150, 175 145" fill="none"/>
              </svg>
              <button className="play-button doodle-border">
                <Play size={24} fill="currentColor" />
              </button>
            </div>
          </div>
        </div>

        <div className="stats-grid">
          <div className="stat-item">
            <h3>200%</h3>
            <p className="text-small text-muted">More Conversion</p>
          </div>
          
          <div className="stat-item">
            <h3>42x</h3>
            <p className="text-small text-muted">Higher ROI</p>
          </div>
          
          <div className="stat-item">
            <h3>300%</h3>
            <p className="text-small text-muted">More Sales</p>
          </div>
        </div>
      </div>
    </section>
  );
}
