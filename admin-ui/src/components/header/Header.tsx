import { useState, useMemo } from 'react';
import { AppBar, Toolbar, useTheme, useMediaQuery } from '@mui/material';
import Menu from '../Menu.tsx';
import SettingsDialog from '../dialogs/SettingsDialog.tsx';
import UserDialog from '../dialogs/UserDialog.tsx';
import { useAuth } from '../../auth';
import { useTranslation } from 'react-i18next';
import HeaderBranding from './HeaderBranding.tsx';
import HeaderActions from './HeaderActions.tsx';

export default function Header() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [userOpen, setUserOpen] = useState(false);
  const auth = useAuth();
  const { t } = useTranslation();

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const initials = useMemo(() => {
    const profile = auth.user;
    if (!profile) return '';
    if ('name' in profile && typeof profile.name === 'string') {
      const parts = profile.name.trim().split(' ');
      if (parts.length >= 2) {
        return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
      }
      return profile.name.slice(0, 2).toUpperCase();
    }
    return '';
  }, [auth.user]);

  return (
    <>
      <AppBar position="fixed">
        <Toolbar
          sx={{
            display: 'flex',
            alignItems: 'center',
            width: '100%',
          }}
        >
          <HeaderBranding />
          <HeaderActions
            isMobile={isMobile}
            onSearchIconClick={() => {}} // No search functionality
            onUserIconClick={() => (auth.isAuthenticated ? setUserOpen(true) : auth.login())}
            onSettingsIconClick={() => setSettingsOpen(true)}
            onMenuIconClick={() => setDrawerOpen(o => !o)}
            isAuthenticated={auth.isAuthenticated}
            userInitials={initials}
          />
        </Toolbar>
      </AppBar>

      <Menu open={drawerOpen} onClose={() => setDrawerOpen(false)} />
      <SettingsDialog open={settingsOpen} onClose={() => setSettingsOpen(false)} />
      <UserDialog
        open={userOpen}
        onLogout={() =>
          auth
            .logout({ returnTo: `${window.location.origin}${import.meta.env.BASE_URL}` })
            .then(() => {
              setUserOpen(false);
            })
        }
        onClose={() => setUserOpen(false)}
      />
    </>
  );
}
