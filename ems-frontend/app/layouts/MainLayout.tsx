import { Outlet } from 'react-router-dom';
import { Header } from './components/Header';
import { Sidebar } from './components/Sidebar';

export const MainLayout = () => (
  <div className="flex h-screen w-screen overflow-hidden bg-background">
    <Sidebar />
    <div className="flex h-full flex-grow flex-col overflow-hidden">
      <Header />
      <main className="relative flex flex-grow flex-col overflow-y-auto bg-background">
        <Outlet />
      </main>
    </div>
  </div>
);

export default MainLayout;
