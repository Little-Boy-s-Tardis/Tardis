import './BuilderPreview.css';

export function BuilderPreview() {
  return (
    <section className="builder-section section-gap">
      <div className="container">
        <div className="builder-header text-center">
          <h2>All You Need to Grow in One Place</h2>
          <p className="text-body text-muted">
            Perspective gives you all the tools you need to build, launch, and scale your funnels. No extra software required.
          </p>
        </div>

        <div className="builder-tabs">
          <button className="tab-btn active">Builder</button>
          <button className="tab-btn">CRM</button>
          <button className="tab-btn">Automations</button>
          <button className="tab-btn">Analytics</button>
          <button className="tab-btn">Domains</button>
          <button className="tab-btn">More</button>
        </div>

        <div className="builder-visual-container">
          <div className="doodle-box builder-window">
            <div className="builder-sidebar">
              <div className="sidebar-placeholder-box"></div>
            </div>
            <div className="builder-main">
              <div className="builder-content-box">
                <h3>The Easiest Way to Build a Funnel That Actually Converts</h3>
                <p className="text-small text-muted">
                  Drag and drop your way to success. Our builder is designed for marketers, not developers.
                </p>
                <a href="#" className="link-bold feature-link" style={{color: 'var(--c-warning)'}}>Learn more</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
