import { NavLink, Outlet } from 'react-router-dom';

export function App() {
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
          <NavLink to="/qualification" className={({ isActive }) => (isActive ? 'actif' : '')}>
            Qualification
          </NavLink>
        </nav>
      </header>
      <main className="contenu">
        <Outlet />
      </main>
    </>
  );
}
