import './Testimonials.css';

export function Testimonials() {
  const reviews = Array(6).fill({
    name: "User Name",
    handle: "@username",
    text: "Perspective has completely transformed how we generate leads. The conversion rates are insane compared to our old website!",
    avatar: "https://api.dicebear.com/7.x/notionists/svg?seed="
  });

  return (
    <section className="testimonials-section section-gap">
      <div className="container">
        <div className="testimonials-header text-center">
          <h2>Marketers Favourite Growth Platform</h2>
          <p className="text-body text-muted">
            Join thousands of marketers who have switched to Perspective and never looked back.
          </p>
        </div>

        <div className="testimonials-grid">
          {reviews.map((review, index) => (
            <div 
              key={index} 
              className="card-default testimonial-card"
              style={{ transform: `rotate(${index % 2 === 0 ? '-1deg' : '1deg'})` }}
            >
              <div className="testimonial-author">
                <img src={`${review.avatar}${index}`} alt="User Avatar" className="avatar" />
                <div className="author-info">
                  <h4 className="author-name">{review.name}</h4>
                  <span className="author-handle text-muted">{review.handle}</span>
                </div>
              </div>
              <p className="testimonial-text">"{review.text}"</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
