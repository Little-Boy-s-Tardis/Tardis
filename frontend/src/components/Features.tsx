import './Features.css';

export function Features() {
  const features = [
    {
      title: "Generate Leads",
      description: "Build your email list and get more qualified leads.",
      bgColor: "#EACDC2", /* Primary Warm Beige */
      linkText: "Learn more"
    },
    {
      title: "Qualify Applicants",
      description: "Find the best talent for your company automatically.",
      bgColor: "#E25858", /* Error Red used as accent in original doodle */
      linkText: "Learn more"
    },
    {
      title: "Sell Products",
      description: "Increase your revenue with high-converting sales funnels.",
      bgColor: "#E09E38", /* Warning Amber used as accent */
      linkText: "Learn more"
    }
  ];

  return (
    <section className="features-section section-gap">
      <div className="container">
        <div className="features-header text-center">
          <h2>Results in All Business-Critical Areas</h2>
          <p className="text-body text-muted">
            Whether you want to generate leads, qualify applicants, or sell products — Perspective is the right tool for you.
          </p>
        </div>

        <div className="features-grid">
          {features.map((feature, index) => (
            <div key={index} className="card-default feature-card">
              <div 
                className="feature-doodle-img doodle-border" 
                style={{ backgroundColor: feature.bgColor, display: 'flex', alignItems: 'flex-end', justifyContent: 'center' }}
              >
                {/* SVG Doodle character */}
                <svg width="100" height="120" viewBox="0 0 100 120" fill="none" stroke="var(--c-charcoal)" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" style={{marginBottom: '-4px'}}>
                  {/* Head */}
                  <circle cx="50" cy="40" r="25" fill="var(--c-white)" />
                  {/* Face details */}
                  <circle cx="40" cy="35" r="3" fill="var(--c-charcoal)" />
                  <circle cx="60" cy="35" r="3" fill="var(--c-charcoal)" />
                  <path d="M 45 45 Q 50 50 55 45" />
                  {/* Body */}
                  <path d="M 25 120 L 25 80 Q 25 65 50 65 Q 75 65 75 80 L 75 120 Z" fill="var(--c-white)" />
                  {/* Arm lines */}
                  <path d="M 25 85 L 15 110" />
                  <path d="M 75 85 L 85 110" />
                </svg>
              </div>
              <h3 className="feature-title">{feature.title}</h3>
              <p className="text-small text-muted feature-desc">{feature.description}</p>
              <a href="#" className="link-bold feature-link">
                {feature.linkText}
              </a>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
