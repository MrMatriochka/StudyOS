import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';

export function App() {
  const navigate = useNavigate();
  const [q, setQ] = useState('');

  function lancerRecherche(e: React.FormEvent) {
    e.preventDefault();
    if (q.trim()) navigate(`/recherche?q=${encodeURIComponent(q.trim())}`);
  }

  return (
    <>
      <header className="entete">
        <span className="marque">StudyOS</span>
        <nav>
          <NavLink to="/accueil" className={({ isActive }) => (isActive ? 'actif' : '')}>
            Accueil
          </NavLink>
          <NavLink to="/semaine" className={({ isActive }) => (isActive ? 'actif' : '')}>
            Semaine
          </NavLink>
          <NavLink to="/matieres" className={({ isActive }) => (isActive ? 'actif' : '')}>
            Matières
          </NavLink>
          <NavLink to="/revision" className={({ isActive }) => (isActive ? 'actif' : '')}>
            Réviser
          </NavLink>
          <NavLink to="/qualification" className={({ isActive }) => (isActive ? 'actif' : '')}>
            Qualification
          </NavLink>
        </nav>
        <form onSubmit={lancerRecherche} style={{ marginLeft: 'auto' }}>
          <input
            type="search"
            placeholder="Rechercher dans les documents…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            style={{ width: 240 }}
          />
        </form>
      </header>
      <main className="contenu">
        <Outlet />
      </main>
    </>
  );
}
