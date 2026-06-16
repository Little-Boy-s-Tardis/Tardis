import { Nav } from './components/Nav';
import { Hero } from './components/Hero';
import { Stats } from './components/Stats';
import { Features } from './components/Features';
import { BuilderPreview } from './components/BuilderPreview';
import { Testimonials } from './components/Testimonials';
import { FooterCTA } from './components/FooterCTA';

function App() {
  return (
    <div className="app-wrapper">
      <Nav />
      <main>
        <Hero />
        <Stats />
        <Features />
        <BuilderPreview />
        <Testimonials />
        <FooterCTA />
      </main>
    </div>
  );
}

export default App;
