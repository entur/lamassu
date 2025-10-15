import { useState } from 'react';
import { AppBar, Toolbar } from '@mui/material';
import Menu from '../Menu.tsx';
import SettingsDialog from '../dialogs/SettingsDialog.tsx';
import HeaderBranding from './HeaderBranding.tsx';
import HeaderActions from './HeaderActions.tsx';

export default function Header() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);

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
            onSettingsIconClick={() => setSettingsOpen(true)}
            onMenuIconClick={() => setDrawerOpen(o => !o)}
          />
        </Toolbar>
      </AppBar>

      <Menu open={drawerOpen} onClose={() => setDrawerOpen(false)} />
      <SettingsDialog open={settingsOpen} onClose={() => setSettingsOpen(false)} />
    </>
  );
}
