import { Box, Drawer, useMediaQuery, IconButton, Toolbar } from '@mui/material';
import WorkAreaContent from './WorkAreaContent.tsx';
import type { Theme } from '@mui/material/styles';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';

interface SidebarProps {
  width: number;
  collapsed: boolean;
  onMouseDownResize: () => void;
  theme: Theme;
  toggleCollapse: () => void;
}

export function Sidebar({
  width,
  collapsed,
  onMouseDownResize,
  theme,
  toggleCollapse,
}: SidebarProps) {
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  if (isMobile) {
    return (
      <Drawer
        anchor="left"
        open={!collapsed}
        onClose={toggleCollapse}
        variant="temporary"
        ModalProps={{
          keepMounted: true,
        }}
        slotProps={{
          paper: {
            sx: {
              width: '100%',
              boxSizing: 'border-box',
              backgroundColor: theme.palette.background.paper,
            },
          },
        }}
      >
        <Toolbar
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
          }}
        >
          <IconButton onClick={toggleCollapse} color="inherit" aria-label="close sidebar">
            <ChevronLeftIcon />
          </IconButton>
        </Toolbar>
        <WorkAreaContent />
      </Drawer>
    );
  }

  return (
    <>
      <Box
        className="sidebar-desktop"
        sx={{
          position: 'absolute',
          top: 0,
          left: 0,
          bottom: 0,
          width: collapsed ? 0 : width,
          minWidth: collapsed ? 0 : 100,
          backgroundColor: theme.palette.background.paper,
          borderRight: collapsed ? 'none' : `1px solid ${theme.palette.divider}`,
          zIndex: 20,
          overflow: 'hidden',
        }}
      >
        {!collapsed && <WorkAreaContent />}
      </Box>

      {!collapsed && (
        <Box
          onMouseDown={onMouseDownResize}
          className="resizer-desktop"
          sx={{
            position: 'absolute',
            top: 0,
            left: width,
            bottom: 0,
            width: '3px',
            cursor: 'ew-resize',
            backgroundColor: theme.palette.divider,
            zIndex: 20,
          }}
        />
      )}
    </>
  );
}
